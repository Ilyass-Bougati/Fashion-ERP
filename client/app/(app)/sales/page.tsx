'use client'

import { useState, useEffect } from 'react'
import Link from 'next/link'
import { Plus, Eye, CreditCard, RotateCcw, ChevronLeft, ChevronRight } from 'lucide-react'
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
  DialogDescription,
} from '@/components/ui/dialog'
import { ToastContainer, useToast } from '@/components/ui/toast'
import { sales } from '@/lib/api'
import type { Sale } from '@/types'

type StatusFilter = Sale['status'] | 'ALL'

const PAGE_SIZE = 20

function truncate(id: string) {
  return id.slice(0, 8) + '…'
}

function StatusBadge({ status }: { status: Sale['status'] }) {
  if (status === 'REFUNDED') return <Badge variant="destructive">Refunded</Badge>
  if (status === 'COMPLETED') return <Badge variant="success">Completed</Badge>
  return <Badge variant="warning">Pending</Badge>
}

export default function SalesPage() {
  const [data, setData] = useState<Sale[]>([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(1)
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('ALL')
  const [loading, setLoading] = useState(true)
  const [refundTarget, setRefundTarget] = useState<string | null>(null)
  const { toasts, toast, removeToast } = useToast()

  useEffect(() => {
    load()
  }, [page, statusFilter])

  async function load() {
    setLoading(true)
    try {
      if (statusFilter === 'ALL') {
        const res = await sales.list(page, PAGE_SIZE)
        setData(res.content)
        setTotalPages(res.totalPages)
      } else {
        // Fetch a large batch and filter + paginate client-side since the
        // backend list endpoint has no status filter parameter.
        const res = await sales.list(0, 1000)
        const filtered = res.content.filter(s => s.status === statusFilter)
        const pages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE))
        setData(filtered.slice(page * PAGE_SIZE, (page + 1) * PAGE_SIZE))
        setTotalPages(pages)
      }
    } catch {
      toast('Failed to load sales', 'error')
    } finally {
      setLoading(false)
    }
  }

  function handleFilterChange(filter: StatusFilter) {
    setStatusFilter(filter)
    setPage(0)
  }

  async function handleCheckout(id: string) {
    try {
      await sales.checkout(id)
      toast('Sale checked out successfully', 'success')
      load()
    } catch {
      toast('Failed to checkout sale', 'error')
    }
  }

  async function handleRefund(id: string) {
    try {
      await sales.refund(id)
      toast('Sale refunded successfully', 'success')
      load()
    } catch {
      toast('Failed to refund sale', 'error')
    } finally {
      setRefundTarget(null)
    }
  }

  const filterLabels: Record<StatusFilter, string> = {
    ALL: 'All',
    PENDING: 'Pending',
    COMPLETED: 'Completed',
    REFUNDED: 'Refunded',
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

      <Card>
        <CardHeader className="flex flex-row items-center justify-between">
          <CardTitle>All Sales</CardTitle>
          <div className="flex gap-1">
            {(['ALL', 'PENDING', 'COMPLETED', 'REFUNDED'] as StatusFilter[]).map(f => (
              <Button
                key={f}
                size="sm"
                variant={statusFilter === f ? 'default' : 'outline'}
                onClick={() => handleFilterChange(f)}
              >
                {filterLabels[f]}
              </Button>
            ))}
          </div>
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
                    <TableCell><StatusBadge status={sale.status} /></TableCell>
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
                        {sale.status === 'PENDING' && (
                          <Button
                            variant="ghost"
                            size="icon"
                            onClick={() => handleCheckout(sale.id)}
                            title="Checkout"
                          >
                            <CreditCard className="h-4 w-4" />
                          </Button>
                        )}
                        {sale.status === 'COMPLETED' && (
                          <Button
                            variant="ghost"
                            size="icon"
                            onClick={() => setRefundTarget(sale.id)}
                            title="Refund"
                            className="text-[var(--destructive)]"
                          >
                            <RotateCcw className="h-4 w-4" />
                          </Button>
                        )}
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
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

      <Dialog open={refundTarget !== null} onOpenChange={open => { if (!open) setRefundTarget(null) }}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Confirm Refund</DialogTitle>
            <DialogDescription>
              Are you sure you want to refund this sale? This action cannot be undone.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setRefundTarget(null)}>Cancel</Button>
            <Button variant="destructive" onClick={() => refundTarget && handleRefund(refundTarget)}>
              Refund
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  )
}
