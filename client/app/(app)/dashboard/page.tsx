'use client'

import { useState, useEffect, useMemo } from 'react'
import { TrendingUp, DollarSign, ShoppingCart, Users, PackageX, AlertTriangle } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Badge } from '@/components/ui/badge'
import {
  ResponsiveContainer,
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ReferenceLine,
} from 'recharts'
import Link from 'next/link'
import { stats, hr, predictions } from '@/lib/api'
import type { EmployeePerformanceStat, StockStat, SalesPrediction } from '@/types'

// ── types ──────────────────────────────────────────────────────────────────────

type Period = 'week' | 'month' | '3months'

const PERIOD_DAYS: Record<Period, number>   = { week: 7, month: 30, '3months': 90 }
const PERIOD_LABELS: Record<Period, string> = { week: 'Week', month: 'Month', '3months': '3 Months' }

interface DailySalesStat {
  statDate:          string
  totalTransactions: number
  netRevenue:        number
  grossRevenue:      number
  totalDiscounts:    number
  unitsSold:         number
  refundedCount:     number
  avgBasketValue:    number
  topCategoryName:   string | null
  topProductSku:     string | null
}

// ── helpers ────────────────────────────────────────────────────────────────────

function fmt(n: number | undefined | null) {
  return (n ?? 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function StatCard({
  title, value, icon: Icon, sub, valueColor, trend,
}: {
  title: string; value: string; icon: React.ElementType; sub?: string; valueColor?: string; trend?: number | null
}) {
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between pb-2">
        <CardTitle className="text-sm font-medium text-[var(--muted-foreground)]">{title}</CardTitle>
        <Icon className="h-4 w-4 text-[var(--muted-foreground)]" />
      </CardHeader>
      <CardContent>
        <div className="flex items-center gap-2">
          <div className="text-2xl font-bold" style={valueColor ? { color: valueColor } : undefined}>
            {value}
          </div>
          {trend != null && (
            <span className="text-sm font-semibold" style={{ color: '#22c55e' }}>
              ↑{trend.toFixed(1)}%
            </span>
          )}
        </div>
        {sub && <p className="text-xs text-[var(--muted-foreground)] mt-1">{sub}</p>}
      </CardContent>
    </Card>
  )
}

// ── page ───────────────────────────────────────────────────────────────────────

export default function DashboardPage() {
  const [period, setPeriod]               = useState<Period>('month')
  const [rawStats, setRawStats]           = useState<DailySalesStat[]>([])
  const [employees, setEmployees]         = useState<EmployeePerformanceStat[]>([])
  const [cinToId, setCinToId]             = useState<Record<string, string>>({})
  const [stockAlerts, setStockAlerts]     = useState<StockStat[]>([])
  const [predData, setPredData]           = useState<SalesPrediction[]>([])
  const [loading, setLoading]             = useState(true)

  useEffect(() => { loadData() }, [])

  async function loadData() {
    setLoading(true)
    try {
      const today = new Date().toISOString().split('T')[0]
      const [salesRes, empStatsRes, empListRes, stockRes, predRes] = await Promise.allSettled([
        stats.sales('DAILY', 0, 120),
        stats.employees('DAILY', 0, 5),
        hr.employees.list(0, 100),
        stats.stock(today, 'DAILY', 0, 20),
        predictions.sales(today, 0, 30),
      ])
      if (salesRes.status === 'fulfilled')
        setRawStats(salesRes.value.content as unknown as DailySalesStat[])
      if (empStatsRes.status === 'fulfilled')
        setEmployees(empStatsRes.value.content)
      if (empListRes.status === 'fulfilled') {
        const map: Record<string, string> = {}
        empListRes.value.content.forEach(e => { map[e.CIN] = e.id })
        setCinToId(map)
      }
      if (stockRes.status === 'fulfilled')
        setStockAlerts(stockRes.value.content.filter(s => s.quantityOnHand < 10).slice(0, 8))
      if (predRes.status === 'fulfilled')
        setPredData(predRes.value.content)
    } finally {
      setLoading(false)
    }
  }

  // ── period helpers ──────────────────────────────────────────────────────────

  const periodStart = useMemo(() => {
    const d = new Date()
    d.setDate(d.getDate() - PERIOD_DAYS[period])
    return d
  }, [period])

  const dateRangeLabel = useMemo(() => {
    const f = (d: Date) =>
      d.toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' })
    return `${f(periodStart)} — ${f(new Date())}`
  }, [periodStart])

  // ── aggregates ──────────────────────────────────────────────────────────────

  const filtered = useMemo(
    () => rawStats.filter(s => new Date(s.statDate) >= periodStart),
    [rawStats, periodStart],
  )

  const totals = useMemo(() => ({
    netRevenue:   filtered.reduce((s, d) => s + d.netRevenue,        0),
    grossRevenue: filtered.reduce((s, d) => s + d.grossRevenue,      0),
    totalSales:   filtered.reduce((s, d) => s + d.totalTransactions, 0),
    discounts:    filtered.reduce((s, d) => s + d.totalDiscounts,    0),
    unitsSold:    filtered.reduce((s, d) => s + d.unitsSold,         0),
    refunded:     filtered.reduce((s, d) => s + d.refundedCount,     0),
  }), [filtered])

  const chartData = useMemo(() =>
    [...filtered]
      .sort((a, b) => a.statDate.localeCompare(b.statDate))
      .map(s => ({ date: s.statDate, net: Math.round(s.netRevenue * 100) / 100 })),
    [filtered],
  )

  const chartColor = totals.netRevenue >= 0 ? '#22c55e' : '#ef4444'

  const todayStr = useMemo(() => new Date().toISOString().slice(0, 10), [])

  const combinedChart = useMemo(() => {
    type Point = { date: string; net?: number; pred?: number; lower?: number; bandH?: number }
    const map: Record<string, Point> = {}
    chartData.forEach(d => { map[d.date] = { date: d.date, net: d.net } })
    if (!map[todayStr]) map[todayStr] = { date: todayStr }
    predData.forEach(p => {
      const lower = Math.max(0, p.netRevenueLowerBound ?? 0)
      const upper = p.netRevenueUpperBound ?? 0
      map[p.targetDate] = {
        ...map[p.targetDate],
        date: p.targetDate,
        pred: p.predictedNetRevenue != null ? Math.round(p.predictedNetRevenue * 100) / 100 : undefined,
        lower: Math.round(lower * 100) / 100,
        bandH: Math.max(0, Math.round((upper - lower) * 100) / 100),
      }
    })
    // Bridge: carry the last historical value into pred/band so lines and band connect
    if (predData.length > 0) {
      const lastHistDate = Object.keys(map).sort().filter(d => map[d].net != null).pop()
      if (lastHistDate) {
        const val = map[lastHistDate].net!
        map[lastHistDate] = { ...map[lastHistDate], pred: val }
      }
    }
    return Object.values(map).sort((a, b) => a.date.localeCompare(b.date))
  }, [chartData, predData, todayStr])

  const next7 = useMemo(() => {
    const slice = predData.slice(0, 7)
    return {
      netRevenue:   slice.reduce((s, p) => s + (p.predictedNetRevenue   ?? 0), 0),
      transactions: slice.reduce((s, p) => s + (p.predictedTransactions ?? 0), 0),
    }
  }, [predData])

  const revTrend  = totals.netRevenue  > 0 ? (next7.netRevenue   / totals.netRevenue)  * 100 : null
  const saleTrend = totals.totalSales  > 0 ? (next7.transactions / totals.totalSales)  * 100 : null

  const yMax = useMemo(() => {
    const vals = combinedChart.flatMap(d => [d.net ?? 0, d.pred ?? 0, (d.lower ?? 0) + (d.bandH ?? 0)])
    return Math.max(...vals, 1) * 1.1
  }, [combinedChart])

  // ── render ──────────────────────────────────────────────────────────────────

  return (
    <div className="space-y-6">

      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold">Dashboard</h2>
          <p className="text-sm text-[var(--muted-foreground)]">{dateRangeLabel}</p>
        </div>
        <Select value={period} onValueChange={v => setPeriod(v as Period)}>
          <SelectTrigger className="w-36">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            {(Object.keys(PERIOD_LABELS) as Period[]).map(p => (
              <SelectItem key={p} value={p}>{PERIOD_LABELS[p]}</SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      {/* Key metric cards */}
      {loading ? (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          {[...Array(4)].map((_, i) => (
            <Card key={i} className="animate-pulse"><CardContent className="h-24" /></Card>
          ))}
        </div>
      ) : (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          <StatCard
            title="Net Revenue"
            value={fmt(totals.netRevenue)}
            icon={DollarSign}
            valueColor={totals.netRevenue >= 0 ? '#22c55e' : '#ef4444'}
            trend={revTrend}
            sub={totals.grossRevenue > 0
              ? `${((totals.netRevenue / totals.grossRevenue) * 100).toFixed(1)}% net margin`
              : undefined}
          />
          <StatCard
            title="Gross Revenue"
            value={fmt(totals.grossRevenue)}
            icon={TrendingUp}
            sub={`${fmt(totals.discounts)} in discounts`}
          />
          <StatCard
            title="Total Sales"
            value={String(totals.totalSales)}
            icon={ShoppingCart}
            trend={saleTrend}
            sub={`${totals.unitsSold} units · ${totals.refunded} refunded`}
          />
          <StatCard
            title="Avg Basket"
            value={totals.totalSales > 0 ? fmt(totals.grossRevenue / totals.totalSales) : '0.00'}
            icon={Users}
            sub={`across ${totals.totalSales} transaction${totals.totalSales !== 1 ? 's' : ''}`}
          />
        </div>
      )}

      {/* Revenue chart + employee leaderboard */}
      <div className="grid gap-6 lg:grid-cols-3">
        <Card className="lg:col-span-2">
          <CardHeader className="pb-2">
            <CardTitle>Net Revenue per day</CardTitle>
            <CardDescription>{dateRangeLabel}</CardDescription>
          </CardHeader>
          <CardContent>
            {combinedChart.length === 0 ? (
              <div className="flex h-48 items-center justify-center text-sm text-[var(--muted-foreground)]">
                No data available
              </div>
            ) : (
              <ResponsiveContainer width="100%" height={220}>
                <AreaChart data={combinedChart} margin={{ top: 4, right: 16, left: 0, bottom: 4 }}>
                  <defs>
                    <linearGradient id="dashRevGradient" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="0%"   stopColor={chartColor} stopOpacity={0.4} />
                      <stop offset="100%" stopColor={chartColor} stopOpacity={0} />
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="var(--border)" />
                  <XAxis dataKey="date" hide />
                  <YAxis
                    domain={[0, yMax]}
                    tick={{ fontSize: 11, fill: 'var(--muted-foreground)' }}
                    axisLine={false}
                    tickLine={false}
                    tickFormatter={v => v.toLocaleString()}
                  />
                  <ReferenceLine x={todayStr} stroke="var(--muted-foreground)" strokeDasharray="4 2" strokeOpacity={0.4} />
                  <Tooltip
                    content={({ active, payload }) => {
                      if (!active || !payload?.length) return null
                      const d = payload[0].payload
                      const upper = (d.lower ?? 0) + (d.bandH ?? 0)
                      return (
                        <div style={{
                          background: 'var(--card)',
                          border: '1px solid var(--border)',
                          borderRadius: 8,
                          padding: '8px 12px',
                          fontSize: 12,
                          lineHeight: '1.8',
                        }}>
                          <p style={{ fontWeight: 600, marginBottom: 4 }}>
                            {new Date(d.date).toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' })}
                          </p>
                          {d.net != null && <p style={{ color: chartColor }}>Net Revenue &nbsp;{fmt(d.net)}</p>}
                          {d.pred != null && (
                            <>
                              <p style={{ color: chartColor }}>Forecast &nbsp;{fmt(d.pred)}</p>
                              <p style={{ color: 'var(--muted-foreground)', fontSize: 11 }}>
                                Range: {fmt(d.lower ?? 0)} – {fmt(upper)}
                              </p>
                            </>
                          )}
                        </div>
                      )
                    }}
                  />
                  {/* Confidence band */}
                  <Area dataKey="lower" stackId="band" stroke="none" fill="none" dot={false} activeDot={false} legendType="none" />
                  <Area dataKey="bandH" stackId="band" stroke="none" fill={`${chartColor}22`} dot={false} activeDot={false} legendType="none" />
                  {/* Actual + forecast lines */}
                  <Area dataKey="net" stroke={chartColor} strokeWidth={2} fill="url(#dashRevGradient)" dot={false} activeDot={{ r: 4, fill: chartColor }} />
                  <Area dataKey="pred" stroke={chartColor} strokeWidth={2} strokeDasharray="5 4" fill="none" dot={false} activeDot={{ r: 4, fill: chartColor }} connectNulls />
                </AreaChart>
              </ResponsiveContainer>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Top Employees</CardTitle>
            <CardDescription>By gross sales</CardDescription>
          </CardHeader>
          <CardContent className="p-0">
            {employees.length === 0 ? (
              <p className="p-4 text-sm text-[var(--muted-foreground)]">No data</p>
            ) : (
              <div className="divide-y divide-[var(--border)]">
                {employees.map((emp, i) => {
                  const empId = cinToId[emp.employeeCin]
                  return (
                    <div key={emp.id} className="flex items-center justify-between px-4 py-3">
                      <div className="flex items-center gap-3">
                        <span className="flex h-6 w-6 items-center justify-center rounded-full bg-[var(--muted)] text-xs font-medium">
                          {i + 1}
                        </span>
                        <div>
                          {empId ? (
                            <Link
                              href={`/hr/${empId}`}
                              className="text-sm font-medium hover:text-[var(--primary)] transition-colors"
                            >
                              {emp.employeeFullName}
                            </Link>
                          ) : (
                            <p className="text-sm font-medium">{emp.employeeFullName}</p>
                          )}
                          <p className="text-xs text-[var(--muted-foreground)]">{emp.salesCount} sales · {emp.itemsSold} units</p>
                        </div>
                      </div>
                      <span className="text-sm font-semibold" style={{ color: 'var(--primary)' }}>
                        {fmt(emp.grossSalesAmount)}
                      </span>
                    </div>
                  )
                })}
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Low stock alerts */}
      {stockAlerts.length > 0 && (
        <Card>
          <CardHeader className="flex flex-row items-center gap-2">
            <AlertTriangle className="h-4 w-4 text-amber-500" />
            <CardTitle>Low Stock Alerts</CardTitle>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>SKU</TableHead>
                  <TableHead>Product</TableHead>
                  <TableHead>Qty on Hand</TableHead>
                  <TableHead>Value</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {stockAlerts.map(s => (
                  <TableRow key={s.productVariationId}>
                    <TableCell className="font-mono text-xs">{s.sku}</TableCell>
                    <TableCell>{s.productName}</TableCell>
                    <TableCell>
                      <Badge variant={s.quantityOnHand === 0 ? 'destructive' : 'warning'}>
                        {s.quantityOnHand}
                      </Badge>
                    </TableCell>
                    <TableCell>{fmt(s.value)}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      )}
    </div>
  )
}
