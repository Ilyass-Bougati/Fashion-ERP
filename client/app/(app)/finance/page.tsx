'use client'

import { useState, useEffect, useMemo } from 'react'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { ToastContainer, useToast } from '@/components/ui/toast'
import { finance } from '@/lib/api'
import type { Transaction } from '@/types'
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
} from 'recharts'

export default function FinancePage() {
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [filter, setFilter] = useState<'ALL' | 'PAID' | 'RECEIVED'>('ALL')
  const [loading, setLoading] = useState(true)
  const { toasts, toast, removeToast } = useToast()

  useEffect(() => { load() }, [filter])

  async function load() {
    setLoading(true)
    try {
      // Fetch a large window so the chart has enough data
      const res = await finance.transactions.list(filter === 'ALL' ? undefined : filter, 0, 200)
      setTransactions(res.content)
    } catch {
      toast('Failed to load transactions', 'error')
    } finally {
      setLoading(false)
    }
  }

  // Aggregate transactions per day for the chart
  const chartData = useMemo(() => {
    const counts: Record<string, { date: string; PAID: number; RECEIVED: number }> = {}
    transactions.forEach(t => {
      const date = new Date(t.createdAt).toLocaleDateString('en-GB', { day: '2-digit', month: 'short' })
      if (!counts[date]) counts[date] = { date, PAID: 0, RECEIVED: 0 }
      counts[date][t.type]++
    })
    return Object.values(counts).sort(
      (a, b) => new Date(a.date).getTime() - new Date(b.date).getTime()
    )
  }, [transactions])

  return (
    <div className="space-y-4">
      <div>
        <h2 className="text-2xl font-bold">Transactions</h2>
        <p className="text-sm text-[var(--muted-foreground)]">Financial transaction history</p>
      </div>

      {/* Daily transactions chart */}
      <Card>
        <CardHeader><CardTitle>Transactions per day</CardTitle></CardHeader>
        <CardContent>
          {chartData.length === 0 ? (
            <div className="flex items-center justify-center h-40">
              <p className="text-sm text-[var(--muted-foreground)]">No data available</p>
            </div>
          ) : (
            <ResponsiveContainer width="100%" height={220}>
              <BarChart data={chartData} margin={{ top: 4, right: 16, left: 0, bottom: 4 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="var(--border)" />
                <XAxis
                  dataKey="date"
                  tick={{ fontSize: 11, fill: 'var(--muted-foreground)' }}
                  axisLine={false}
                  tickLine={false}
                />
                <YAxis
                  allowDecimals={false}
                  tick={{ fontSize: 11, fill: 'var(--muted-foreground)' }}
                  axisLine={false}
                  tickLine={false}
                />
                <Tooltip
                  contentStyle={{
                    background: 'var(--card)',
                    border: '1px solid var(--border)',
                    borderRadius: 8,
                    fontSize: 12,
                  }}
                />
                <Bar dataKey="RECEIVED" name="Received" fill="var(--success, #22c55e)" radius={[4, 4, 0, 0]} />
                <Bar dataKey="PAID"     name="Paid"     fill="var(--destructive)"          radius={[4, 4, 0, 0]} />
              </BarChart>
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
