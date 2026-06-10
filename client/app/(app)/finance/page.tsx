'use client'

import { useState, useEffect, useMemo } from 'react'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { ToastContainer, useToast } from '@/components/ui/toast'
import { finance, predictions } from '@/lib/api'
import type { Transaction, SalesPrediction } from '@/types'
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

type Period = 'week' | 'month' | '3months'

const PERIOD_DAYS: Record<Period, number> = { week: 7, month: 30, '3months': 90 }
const PERIOD_LABELS: Record<Period, string> = { week: 'Week', month: 'Month', '3months': '3 Months' }

export default function FinancePage() {
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [predData, setPredData] = useState<SalesPrediction[]>([])
  const [filter, setFilter] = useState<'ALL' | 'PAID' | 'RECEIVED'>('ALL')
  const [period, setPeriod] = useState<Period>('month')
  const [loading, setLoading] = useState(true)
  const { toasts, toast, removeToast } = useToast()

  useEffect(() => {
    const today = new Date().toISOString().slice(0, 10)
    predictions.sales(today).then(r => setPredData(r.content)).catch(() => {})
  }, [])

  useEffect(() => { load() }, [filter])

  async function load() {
    setLoading(true)
    try {
      const res = await finance.transactions.list(filter === 'ALL' ? undefined : filter, 0, 200)
      setTransactions(res.content)
    } catch {
      toast('Failed to load transactions', 'error')
    } finally {
      setLoading(false)
    }
  }

  const periodStart = useMemo(() => {
    const d = new Date()
    d.setDate(d.getDate() - PERIOD_DAYS[period])
    return d
  }, [period])

  const dateRangeLabel = useMemo(() => {
    const fmt = (d: Date) => d.toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' })
    return `${fmt(periodStart)} — ${fmt(new Date())}`
  }, [periodStart])

  const chartData = useMemo(() => {
    const daily: Record<string, { date: string; net: number; received: number; paid: number }> = {}
    transactions
      .filter(t => new Date(t.createdAt) >= periodStart)
      .forEach(t => {
        const date = new Date(t.createdAt).toISOString().slice(0, 10)
        if (!daily[date]) daily[date] = { date, net: 0, received: 0, paid: 0 }
        if (t.type === 'RECEIVED') {
          daily[date].received += t.amount
          daily[date].net      += t.amount
        } else {
          daily[date].paid += t.amount
          daily[date].net  -= t.amount
        }
      })
    return Object.values(daily)
      .sort((a, b) => a.date.localeCompare(b.date))
      .map(d => ({
        ...d,
        net:      Math.round(d.net      * 100) / 100,
        received: Math.round(d.received * 100) / 100,
        paid:     Math.round(d.paid     * 100) / 100,
      }))
  }, [transactions, periodStart])

  const todayStr = useMemo(() => new Date().toISOString().slice(0, 10), [])

  const isProfit = useMemo(
    () => chartData.reduce((s, d) => s + d.net, 0) >= 0,
    [chartData],
  )

  const chartColor = isProfit ? '#22c55e' : '#ef4444'

  const combinedChart = useMemo(() => {
    type Point = { date: string; net?: number; received?: number; paid?: number; pred?: number; lower?: number; bandH?: number }
    const map: Record<string, Point> = {}
    // Fill every calendar day in the historical window so the density matches the prediction side
    for (const d = new Date(periodStart); ; d.setDate(d.getDate() + 1)) {
      const dateStr = d.toISOString().slice(0, 10)
      map[dateStr] = { date: dateStr, net: 0, received: 0, paid: 0 }
      if (dateStr >= todayStr) break
    }
    chartData.forEach(d => { map[d.date] = { date: d.date, net: d.net, received: d.received, paid: d.paid } })
    if (!map[todayStr]) map[todayStr] = { date: todayStr }
    predData.forEach(p => {
      const lower = Math.max(0, p.netRevenueLowerBound ?? 0)
      const upper = p.netRevenueUpperBound ?? 0
      map[p.targetDate] = {
        ...map[p.targetDate],
        date: p.targetDate,
        pred:  p.predictedNetRevenue != null ? Math.round(p.predictedNetRevenue * 100) / 100 : undefined,
        lower: Math.round(lower * 100) / 100,
        bandH: Math.max(0, Math.round((upper - lower) * 100) / 100),
      }
    })
    // Bridge: copy last historical net into pred so the lines connect
    if (predData.length > 0) {
      const lastHistDate = Object.keys(map).sort().filter(d => map[d].net != null).pop()
      if (lastHistDate) {
        const val = map[lastHistDate].net!
        map[lastHistDate] = { ...map[lastHistDate], pred: val }
      }
    }
    return Object.values(map).sort((a, b) => a.date.localeCompare(b.date))
  }, [chartData, predData, todayStr, periodStart])

  const yDomain = useMemo(() => {
    const allVals = combinedChart.flatMap(d => [
      d.net   ?? 0,
      d.pred  ?? 0,
      d.lower ?? 0,
      (d.lower ?? 0) + (d.bandH ?? 0),
    ])
    if (allVals.length === 0) return [-1, 1]
    const max = Math.max(...allVals.map(Math.abs), 1)
    return [-max * 1.1, max * 1.1]
  }, [combinedChart])

  return (
    <div className="space-y-4">
      <div>
        <h2 className="text-2xl font-bold">Transactions</h2>
        <p className="text-sm text-[var(--muted-foreground)]">Financial transaction history</p>
      </div>

      {/* Daily net flow chart */}
      <Card>
        <CardHeader className="flex flex-row items-start justify-between pb-2">
          <div>
            <CardTitle>Net cash flow per day</CardTitle>
            <p className="text-xs text-[var(--muted-foreground)] mt-1">{dateRangeLabel}</p>
          </div>
          <Select value={period} onValueChange={v => setPeriod(v as Period)}>
            <SelectTrigger className="w-32">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {(Object.keys(PERIOD_LABELS) as Period[]).map(p => (
                <SelectItem key={p} value={p}>{PERIOD_LABELS[p]}</SelectItem>
              ))}
            </SelectContent>
          </Select>
        </CardHeader>
        <CardContent>
          {combinedChart.length === 0 ? (
            <div className="flex items-center justify-center h-40">
              <p className="text-sm text-[var(--muted-foreground)]">No data available</p>
            </div>
          ) : (
            <ResponsiveContainer width="100%" height={220}>
              <AreaChart data={combinedChart} margin={{ top: 4, right: 16, left: 0, bottom: 4 }}>
                <defs>
                  <linearGradient id="netGradient" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%"   stopColor={chartColor} stopOpacity={0.4} />
                    <stop offset="50%"  stopColor={chartColor} stopOpacity={0} />
                    <stop offset="100%" stopColor={chartColor} stopOpacity={0.4} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="var(--border)" />
                <XAxis dataKey="date" hide />
                <YAxis
                  domain={yDomain}
                  tick={{ fontSize: 11, fill: 'var(--muted-foreground)' }}
                  axisLine={false}
                  tickLine={false}
                  tickFormatter={v => v.toLocaleString()}
                />
                <ReferenceLine y={0} stroke="var(--border)" strokeDasharray="4 4" />
                <ReferenceLine x={todayStr} stroke="var(--muted-foreground)" strokeDasharray="4 2" strokeOpacity={0.4} />
                <Tooltip
                  content={({ active, payload }) => {
                    if (!active || !payload?.length) return null
                    const d = payload[0].payload
                    const fmt = (n: number) => n.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })
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
                        {d.net != null && (
                          <>
                            <p style={{ color: '#22c55e' }}>▲ Received &nbsp;{fmt(d.received)}</p>
                            <p style={{ color: '#ef4444' }}>▼ Paid &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{fmt(d.paid)}</p>
                            <p style={{ color: chartColor, fontWeight: 600, borderTop: '1px solid var(--border)', marginTop: 4, paddingTop: 4 }}>
                              Net &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{d.net >= 0 ? '+' : ''}{fmt(d.net)}
                            </p>
                          </>
                        )}
                        {d.pred != null && (
                          <>
                            <p style={{ color: chartColor }}>Forecast &nbsp;{d.pred >= 0 ? '+' : ''}{fmt(d.pred)}</p>
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
                {/* Actual net cash flow */}
                <Area
                  dataKey="net"
                  stroke={chartColor}
                  strokeWidth={2}
                  fill="url(#netGradient)"
                  baseValue={0}
                  dot={false}
                  activeDot={{ r: 4, fill: chartColor }}
                />
                {/* Forecast line */}
                <Area dataKey="pred" stroke={chartColor} strokeWidth={2} strokeDasharray="5 4" fill="none" dot={false} activeDot={{ r: 4, fill: chartColor }} connectNulls />
              </AreaChart>
            </ResponsiveContainer>
          )}
        </CardContent>
      </Card>

      {/* Transactions table */}
      <Tabs value={filter} onValueChange={v => setFilter(v as typeof filter)}>
        <TabsList>
          <TabsTrigger value="ALL">All</TabsTrigger>
          <TabsTrigger value="RECEIVED">Received</TabsTrigger>
          <TabsTrigger value="PAID">Paid</TabsTrigger>
        </TabsList>
        <TabsContent value={filter}>
          <Card>
            <CardHeader><CardTitle>Transactions</CardTitle></CardHeader>
            <CardContent className="p-0">
              {loading ? (
                <div className="flex items-center justify-center h-40">
                  <p className="text-sm text-[var(--muted-foreground)]">Loading…</p>
                </div>
              ) : transactions.length === 0 ? (
                <div className="flex items-center justify-center h-40">
                  <p className="text-sm text-[var(--muted-foreground)]">No transactions found</p>
                </div>
              ) : (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Type</TableHead>
                      <TableHead>Amount</TableHead>
                      <TableHead>Sale ID</TableHead>
                      <TableHead>Date</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {transactions.map(t => (
                      <TableRow key={t.id}>
                        <TableCell>
                          <Badge variant={t.type === 'RECEIVED' ? 'success' : 'destructive'}>
                            {t.type}
                          </Badge>
                        </TableCell>
                        <TableCell className="font-medium">
                          {t.type === 'RECEIVED' ? '+' : '-'}${t.amount.toFixed(2)}
                        </TableCell>
                        <TableCell className="font-mono text-xs text-[var(--muted-foreground)]">
                          {t.saleId ? t.saleId.slice(0, 12) + '…' : '—'}
                        </TableCell>
                        <TableCell className="text-sm text-[var(--muted-foreground)]">
                          {new Date(t.createdAt).toLocaleDateString()}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              )}
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  )
}
