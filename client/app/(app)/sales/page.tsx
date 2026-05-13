'use client'

import { useState, useEffect } from 'react'
import Link from 'next/link'
import { Plus, Eye, CreditCard, RotateCcw, ChevronLeft, ChevronRight, AlertTriangle } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog'
import { ToastContainer, useToast } from '@/components/ui/toast'
import { BarChart } from '@/components/charts/bar-chart'
import { sales, stats } from '@/lib/api'
import type { Sale } from '@/types'

function truncate(id: string) {
  return id.slice(0, 8) + '…'
}

type ConfirmState = { action: 'checkout' | 'refund'; id: string } | null

export default function SalesPage() {
  const [data, setData] = useState<Sale[]>([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(1)
  const [loading, setLoading] = useState(true)
  const [confirm, setConfirm] = useState<ConfirmState>(null)
  const [chartData, setChartData] = useState<Array<{ day: string; sales: number }>>([])
  const { toasts, toast, removeToast } = useToast()

  useEffect(() => {
    loadChart()
  }, [])

  useEffect(() => {
    load()
  }, [page])

  async function loadChart() {
    try {
      const res = await stats.sales('DAILY', 0, 30)
      const sorted = [...res.content].sort((a, b) => a.period.localeCompare(b.period))
      setChartData(sorted.map(s => ({ day: s.period, sales: s.transactionCount })))
    } catch {
      // chart is non-critical; silently ignore
    }
  }

  async function load() {
    setLoading(true)
    try {
      const res = await sales.list(page, 20)
      setData(res.content)
      setTotalPages(res.totalPages)
    } catch {
      toast('Failed to load sales', 'error')
    } finally {
      setLoading(false)
    }
  }

  async function executeConfirmed() {
    if (!confirm) return
    try {
      if (confirm.action === 'checkout') {
        await sales.checkout(confirm.id)
        toast('Sale checked out successfully', 'success')
      } else {
        await sales.refund(confirm.id)
        toast('Sale refunded successfully', 'success')
      }
      load()
    } catch {
      toast(confirm.action === 'checkout' ? 'Failed to checkout sale' : 'Failed to refund sale', 'error')
    } finally {
      setConfirm(null)
    }
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold">Sales</h2>
          <p className="text-sm text-[var(--muted-foreground)]">Manage all sales transactions</p>
        </div>
        <Button asChild>
          <Link href="/sales/new">
            <Plus className="mr-2 h-4 w-4" />
            New Sale
          </Link>
        </Button>
      </div>

      {chartData.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle>Daily Sales</CardTitle>
          </CardHeader>
          <CardContent>
            <BarChart
              data={chartData}
              xKey="day"
              yKey="sales"
              color="#6366f1"
              yTickFormatter={(v) => String(v)}
              tooltipFormatter={(v) => [String(v), 'Sales']}
            />
          </CardContent>
        </Card>
      )}

      {/* Checkout / Refund confirmation dialog */}
      <Dialog open={confirm !== null} onOpenChange={v => { if (!v) setConfirm(null) }}>
        <DialogContent className="max-w-sm">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <AlertTriangle className="h-5 w-5 text-[var(--destructive)]" />
              {confirm?.action === 'checkout' ? 'Confirm Checkout' : 'Confirm Refund'}
            </DialogTitle>
          </DialogHeader>
          <p className="text-sm text-[var(--muted-foreground)]">
            {confirm?.action === 'checkout'
              ? 'Are you sure you want to checkout this sale?'
              : 'Are you sure you want to refund this sale? This action cannot be undone.'
            }
          </p>
          <DialogFooter>
            <Button variant="outline" onClick={() => setConfirm(null)}>Cancel</Button>
            <Button
              variant={confirm?.action === 'refund' ? 'destructive' : 'default'}
              onClick={executeConfirmed}
            >
              {confirm?.action === 'checkout' ? 'Checkout' : 'Refund'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Card>
        <CardHeader>
          <CardTitle>All Sales</CardTitle>
        </CardHeader>
        <CardContent className="p-0">
          {loading ? (
            <div className="flex items-center justify-center h-40">
              <div className="text-sm text-[var(--muted-foreground)]">Loading…</div>
            </div>
          ) : data.length === 0 ? (
            <div className="flex items-center justify-center h-40">
              <p className="text-sm text-[var(--muted-foreground)]">No sales found</p>
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Sale ID</TableHead>
                  <TableHead>Employee ID</TableHead>
                  <TableHead>Discount</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Created</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {data.map(sale => (
                  <TableRow key={sale.id}>
                    <TableCell className="font-mono text-xs">{truncate(sale.id)}</TableCell>
                    <TableCell className="font-mono text-xs">{truncate(sale.employeeId)}</TableCell>
                    <TableCell>{sale.discount != null ? `${sale.discount}%` : '—'}</TableCell>
                    <TableCell>
                      <Badge variant={sale.refunded ? 'destructive' : 'success'}>
                        {sale.refunded ? 'Refunded' : 'Active'}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-sm text-[var(--muted-foreground)]">
                      {new Date(sale.createdAt).toLocaleDateString()}
                    </TableCell>
                    <TableCell className="text-right">
                      <div className="flex items-center justify-end gap-1">
                        <Button variant="ghost" size="icon" asChild>
                          <Link href={`/sales/${sale.id}`}>
                            <Eye className="h-4 w-4" />
                          </Link>
                        </Button>
                        {!sale.refunded && (
                          <>
                            <Button
                              variant="ghost"
                              size="icon"
                              onClick={() => setConfirm({ action: 'checkout', id: sale.id })}
                              title="Checkout"
                            >
                              <CreditCard className="h-4 w-4" />
                            </Button>
                            <Button
                              variant="ghost"
                              size="icon"
                              onClick={() => setConfirm({ action: 'refund', id: sale.id })}
                              title="Refund"
                              className="text-[var(--destructive)]"
                            >
                              <RotateCcw className="h-4 w-4" />
                            </Button>
                          </>
                        )}
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
          {/* Pagination */}
          {totalPages > 1 && (
            <div className="flex items-center justify-center gap-2 py-4">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage(p => Math.max(0, p - 1))}
                disabled={page === 0}
              >
                <ChevronLeft className="h-4 w-4" />
              </Button>
              <span className="text-sm text-[var(--muted-foreground)]">
                Page {page + 1} of {totalPages}
              </span>
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                disabled={page >= totalPages - 1}
              >
                <ChevronRight className="h-4 w-4" />
              </Button>
            </div>
          )}
        </CardContent>
      </Card>
      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  )
}
