'use client'

import { useState, useEffect, useMemo } from 'react'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { ToastContainer, useToast } from '@/components/ui/toast'
import { finance } from '@/lib/api'
import type { Transaction } from '@/types'
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
  const [filter, setFilter] = useState<'ALL' | 'PAID' | 'RECEIVED'>('ALL')
  const [period, setPeriod] = useState<Period>('month')
  const [loading, setLoading] = useState(true)
  const { toasts, toast, removeToast } = useToast()

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

  const isProfit = useMemo(
    () => chartData.reduce((s, d) => s + d.net, 0) >= 0,
    [chartData],
  )

  const chartColor = isProfit ? '#22c55e' : '#ef4444'

  const yDomain = useMemo(() => {
    if (chartData.length === 0) return [-1, 1]
    const max = Math.max(...chartData.map(d => Math.abs(d.net)), 1)
    return [-max, max]
  }, [chartData])

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
          {chartData.length === 0 ? (
            <div className="flex items-center justify-center h-40">
              <p className="text-sm text-[var(--muted-foreground)]">No data available</p>
            </div>
          ) : (
            <ResponsiveContainer width="100%" height={220}>
              <AreaChart data={chartData} margin={{ top: 4, right: 16, left: 0, bottom: 4 }}>
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
                <Tooltip
                  content={({ active, payload }) => {
                    if (!active || !payload?.length) return null
                    const d = payload[0].payload
                    const fmt = (n: number) => n.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })
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
                        <p style={{ color: '#22c55e' }}>▲ Received &nbsp;{fmt(d.received)}</p>
                        <p style={{ color: '#ef4444' }}>▼ Paid &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{fmt(d.paid)}</p>
                        <p style={{ color: chartColor, fontWeight: 600, borderTop: '1px solid var(--border)', marginTop: 4, paddingTop: 4 }}>
                          Net &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{d.net >= 0 ? '+' : ''}{fmt(d.net)}
                        </p>
                      </div>
                    )
                  }}
                />
                <Area
                  dataKey="net"
                  stroke={chartColor}
                  strokeWidth={2}
                  fill="url(#netGradient)"
                  baseValue={0}
                  dot={false}
                  activeDot={{ r: 4, fill: chartColor }}
                />
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
