'use client'

import { useState, useEffect, useMemo } from 'react'
import { useParams, useRouter } from 'next/navigation'
import Link from 'next/link'
import { ArrowLeft, Mail, Phone, CreditCard, Calendar, DollarSign, TrendingUp, ShoppingCart, Award } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
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
import { hr, stats, finance, predictions } from '@/lib/api'
import type { Employee, EmployeePerformanceStat, Payroll, EmployeePerformancePrediction } from '@/types'

// ── types ───────────────────────────────────────────────────────────────────

type Period = 'week' | 'month' | '3months'
const PERIOD_DAYS: Record<Period, number>   = { week: 7, month: 30, '3months': 90 }
const PERIOD_LABELS: Record<Period, string> = { week: 'Week', month: 'Month', '3months': '3 Months' }

const CHART_COLOR = '#6366f1'

// ── helpers ─────────────────────────────────────────────────────────────────

function fmt(n: number | undefined | null) {
  return (n ?? 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function InfoRow({ icon: Icon, label, value }: { icon: React.ElementType; label: string; value: string }) {
  return (
    <div className="flex items-center gap-3 py-2">
      <Icon className="h-4 w-4 text-[var(--muted-foreground)] shrink-0" />
      <span className="text-sm text-[var(--muted-foreground)] w-28 shrink-0">{label}</span>
      <span className="text-sm font-medium">{value}</span>
    </div>
  )
}

function StatCard({ title, value, icon: Icon, sub }: {
  title: string; value: string; icon: React.ElementType; sub?: string
}) {
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between pb-2">
        <CardTitle className="text-sm font-medium text-[var(--muted-foreground)]">{title}</CardTitle>
        <Icon className="h-4 w-4 text-[var(--muted-foreground)]" />
      </CardHeader>
      <CardContent>
        <div className="text-2xl font-bold">{value}</div>
        {sub && <p className="text-xs text-[var(--muted-foreground)] mt-1">{sub}</p>}
      </CardContent>
    </Card>
  )
}

// ── page ────────────────────────────────────────────────────────────────────

export default function EmployeeDetailPage() {
  const { id } = useParams<{ id: string }>()
  const router  = useRouter()

  const [employee, setEmployee]     = useState<Employee | null>(null)
  const [rawStats, setRawStats]     = useState<EmployeePerformanceStat[]>([])
  const [payrolls, setPayrolls]     = useState<Payroll[]>([])
  const [predData, setPredData]     = useState<EmployeePerformancePrediction[]>([])
  const [period, setPeriod]         = useState<Period>('month')
  const [loading, setLoading]       = useState(true)
  const [notFound, setNotFound]     = useState(false)

  useEffect(() => { loadAll() }, [id])

  useEffect(() => {
    if (!employee) return
    const today = new Date().toISOString().slice(0, 10)
    predictions.employees(today, employee.CIN, 0, 30)
      .then(res => setPredData(res.content))
      .catch(() => {})
  }, [employee])

  async function loadAll() {
    setLoading(true)
    try {
      const [empRes, statsRes, payrollRes] = await Promise.allSettled([
        hr.employees.get(id),
        stats.employees('DAILY', 0, 2000),
        finance.payroll.byEmployee(id),
      ])

      if (empRes.status === 'fulfilled') {
        setEmployee(empRes.value)
      } else {
        setNotFound(true)
        return
      }

      if (statsRes.status === 'fulfilled') {
        const empStats = statsRes.value.content.filter(
          (s: EmployeePerformanceStat) => s.employeeCin === empRes.value.CIN
        )
        setRawStats(empStats)
      }

      if (payrollRes.status === 'fulfilled') {
        setPayrolls(payrollRes.value.sort(
          (a: Payroll, b: Payroll) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
        ))
      }
    } finally {
      setLoading(false)
    }
  }

  // ── period filter ────────────────────────────────────────────────────────

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

  const filtered = useMemo(
    () => rawStats.filter(s => new Date(s.statDate) >= periodStart),
    [rawStats, periodStart],
  )

  const totals = useMemo(() => ({
    sales:      filtered.reduce((s, d) => s + (d.salesCount      ?? 0), 0),
    revenue:    filtered.reduce((s, d) => s + (d.grossSalesAmount ?? 0), 0),
    commission: filtered.reduce((s, d) => s + (d.commissionEarned ?? 0), 0),
    units:      filtered.reduce((s, d) => s + (d.itemsSold        ?? 0), 0),
  }), [filtered])

  const chartData = useMemo(() =>
    [...filtered]
      .sort((a, b) => a.statDate.localeCompare(b.statDate))
      .map(s => ({ date: s.statDate, revenue: Math.round((s.grossSalesAmount ?? 0) * 100) / 100 })),
    [filtered],
  )

  const todayStr = useMemo(() => new Date().toISOString().slice(0, 10), [])

  const combinedChart = useMemo(() => {
    type Point = { date: string; revenue?: number; pred?: number; lower?: number; bandH?: number }
    const map: Record<string, Point> = {}
    // Fill every calendar day so the chart density matches the prediction side
    for (const d = new Date(periodStart); ; d.setDate(d.getDate() + 1)) {
      const dateStr = d.toISOString().slice(0, 10)
      map[dateStr] = { date: dateStr, revenue: 0 }
      if (dateStr >= todayStr) break
    }
    chartData.forEach(d => { map[d.date] = { date: d.date, revenue: d.revenue } })
    if (!map[todayStr]) map[todayStr] = { date: todayStr }
    predData.forEach(p => {
      const lower = Math.max(0, p.grossSalesLowerBound ?? 0)
      const upper = p.grossSalesUpperBound ?? 0
      map[p.targetDate] = {
        ...map[p.targetDate],
        date: p.targetDate,
        pred: p.predictedGrossSales != null ? Math.round(p.predictedGrossSales * 100) / 100 : undefined,
        lower: Math.round(lower * 100) / 100,
        bandH: Math.max(0, Math.round((upper - lower) * 100) / 100),
      }
    })
    // Bridge: carry the last historical value so the pred line connects
    if (predData.length > 0) {
      const lastHistDate = Object.keys(map).sort().filter(d => map[d].revenue != null).pop()
      if (lastHistDate) {
        map[lastHistDate] = { ...map[lastHistDate], pred: map[lastHistDate].revenue! }
      }
    }
    return Object.values(map).sort((a, b) => a.date.localeCompare(b.date))
  }, [chartData, predData, todayStr, periodStart])

  // ── render ───────────────────────────────────────────────────────────────

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <p className="text-sm text-[var(--muted-foreground)]">Loading…</p>
      </div>
    )
  }

  if (notFound || !employee) {
    return (
      <div className="flex flex-col items-center justify-center h-64 gap-4">
        <p className="text-sm text-[var(--muted-foreground)]">Employee not found.</p>
        <Button variant="outline" onClick={() => router.back()}>Go back</Button>
      </div>
    )
  }

  return (
    <div className="space-y-6">

      {/* Header */}
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => router.back()}>
          <ArrowLeft className="h-4 w-4" />
        </Button>
        <div className="flex-1">
          <div className="flex items-center gap-3">
            <h2 className="text-2xl font-bold">{employee.firstName} {employee.lastName}</h2>
            <Badge variant={employee.active ? 'success' : 'secondary'}>
              {employee.active ? 'Active' : 'Terminated'}
            </Badge>
          </div>
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

      {/* Info + stat cards */}
      <div className="grid gap-6 lg:grid-cols-3">

        {/* Employee info */}
        <Card>
          <CardHeader><CardTitle>Profile</CardTitle></CardHeader>
          <CardContent className="divide-y divide-[var(--border)]">
            <InfoRow icon={Mail}     label="Email"      value={employee.email} />
            <InfoRow icon={Phone}    label="Phone"      value={employee.phoneNumber} />
            <InfoRow icon={CreditCard} label="CIN"     value={employee.CIN} />
            <InfoRow icon={Calendar} label="Hired"      value={new Date(employee.hiredAt).toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' })} />
            <InfoRow icon={DollarSign} label="Salary"  value={`${fmt(employee.salary)} MAD / mo`} />
            <InfoRow icon={Award}    label="Commission" value={`${(employee.commission * 100).toFixed(1)}%`} />
          </CardContent>
        </Card>

        {/* Period stats */}
        <div className="lg:col-span-2 grid gap-4 grid-cols-2">
          <Link href={`/hr/${id}/sales`} className="block hover:opacity-80 transition-opacity">
            <StatCard
              title="Sales"
              value={String(totals.sales)}
              icon={ShoppingCart}
              sub={`${totals.units} units sold`}
            />
          </Link>
          <StatCard
            title="Gross Revenue"
            value={fmt(totals.revenue)}
            icon={TrendingUp}
            sub={totals.sales > 0 ? `avg ${fmt(totals.revenue / totals.sales)} per sale` : undefined}
          />
          <StatCard
            title="Commission Earned"
            value={fmt(totals.commission)}
            icon={Award}
            sub={`Base salary: ${fmt(employee.salary)} MAD`}
          />
          <StatCard
            title="Total Earnings"
            value={fmt(employee.salary + totals.commission)}
            icon={DollarSign}
            sub={`salary + commission`}
          />
        </div>
      </div>

      {/* Sales revenue chart */}
      <Card>
        <CardHeader className="pb-2">
          <CardTitle>Sales Revenue per Day</CardTitle>
          <CardDescription>{dateRangeLabel}</CardDescription>
        </CardHeader>
        <CardContent>
          {combinedChart.length === 0 ? (
            <div className="flex h-48 items-center justify-center text-sm text-[var(--muted-foreground)]">
              No sales data for this period
            </div>
          ) : (
            <ResponsiveContainer width="100%" height={220}>
              <AreaChart data={combinedChart} margin={{ top: 4, right: 16, left: 0, bottom: 4 }}>
                <defs>
                  <linearGradient id="empRevGradient" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%"   stopColor={CHART_COLOR} stopOpacity={0.4} />
                    <stop offset="100%" stopColor={CHART_COLOR} stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="var(--border)" />
                <XAxis dataKey="date" hide />
                <YAxis
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
                        {d.revenue != null && <p style={{ color: CHART_COLOR }}>Revenue &nbsp;{fmt(d.revenue)}</p>}
                        {d.pred != null && (
                          <>
                            <p style={{ color: CHART_COLOR }}>Forecast &nbsp;{fmt(d.pred)}</p>
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
                <Area dataKey="bandH" stackId="band" stroke="none" fill={`${CHART_COLOR}22`} dot={false} activeDot={false} legendType="none" />
                {/* Actual + forecast lines */}
                <Area dataKey="revenue" stroke={CHART_COLOR} strokeWidth={2} fill="url(#empRevGradient)" dot={false} activeDot={{ r: 4, fill: CHART_COLOR }} />
                <Area dataKey="pred" stroke={CHART_COLOR} strokeWidth={2} strokeDasharray="5 4" fill="none" dot={false} activeDot={{ r: 4, fill: CHART_COLOR }} connectNulls />
              </AreaChart>
            </ResponsiveContainer>
          )}
        </CardContent>
      </Card>

      {/* Payroll history */}
      {payrolls.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle>Payroll History</CardTitle>
            <CardDescription>Most recent payroll records</CardDescription>
          </CardHeader>
          <CardContent className="p-0">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Date</TableHead>
                  <TableHead>Salary</TableHead>
                  <TableHead>Commission</TableHead>
                  <TableHead>Total</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {payrolls.map(p => (
                  <TableRow key={p.id}>
                    <TableCell className="text-sm text-[var(--muted-foreground)]">
                      {new Date(p.createdAt).toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' })}
                    </TableCell>
                    <TableCell>{fmt(p.salary)}</TableCell>
                    <TableCell>{fmt(p.commission)}</TableCell>
                    <TableCell className="font-semibold">{fmt((p.salary ?? 0) + (p.commission ?? 0))}</TableCell>
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
