'use client'

import { useState, useEffect } from 'react'
import { useParams, useRouter } from 'next/navigation'
import { ArrowLeft, Eye, CreditCard, RotateCcw, ChevronLeft, ChevronRight, AlertTriangle } from 'lucide-react'
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
import Link from 'next/link'
import { sales, hr } from '@/lib/api'
import type { Sale, Employee } from '@/types'

type ConfirmState = { action: 'checkout' | 'refund'; id: string } | null

export default function EmployeeSalesPage() {
  const { id } = useParams<{ id: string }>()
  const router  = useRouter()

  const [employee, setEmployee] = useState<Employee | null>(null)
  const [data, setData]         = useState<Sale[]>([])
  const [page, setPage]         = useState(0)
  const [totalPages, setTotalPages] = useState(1)
  const [loading, setLoading]   = useState(true)
  const [confirm, setConfirm]   = useState<ConfirmState>(null)
  const { toasts, toast, removeToast } = useToast()

  useEffect(() => {
    hr.employees.get(id)
      .then(e => setEmployee(e))
      .catch(() => {})
  }, [id])

  useEffect(() => { load() }, [id, page])

  async function load() {
    setLoading(true)
    try {
      const res = await sales.byEmployee(id, page, 20)
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
      toast(confirm.action === 'checkout' ? 'Failed to checkout' : 'Failed to refund', 'error')
    } finally {
      setConfirm(null)
    }
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => router.back()}>
          <ArrowLeft className="h-4 w-4" />
        </Button>
        <div>
          <h2 className="text-2xl font-bold">
            {employee ? `${employee.firstName} ${employee.lastName}` : '…'} — Sales
          </h2>
          <p className="text-sm text-[var(--muted-foreground)]">All sales transactions for this employee</p>
        </div>
      </div>

      {/* Confirmation dialog */}
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
        <CardHeader><CardTitle>Sales</CardTitle></CardHeader>
        <CardContent className="p-0">
          {loading ? (
            <div className="flex items-center justify-center h-40">
              <p className="text-sm text-[var(--muted-foreground)]">Loading…</p>
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
                  <TableHead>Discount</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Date</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {data.map(sale => (
                  <TableRow key={sale.id}>
                    <TableCell className="font-mono text-xs">{sale.id.slice(0, 8)}…</TableCell>
                    <TableCell>{sale.discount != null ? `${sale.discount}%` : '—'}</TableCell>
                    <TableCell>
                      <Badge variant={sale.status === 'REFUNDED' ? 'destructive' : sale.status === 'COMPLETED' ? 'success' : 'secondary'}>
                        {sale.status === 'PENDING' ? 'Pending' : sale.status === 'COMPLETED' ? 'Completed' : 'Refunded'}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-sm text-[var(--muted-foreground)]">
                      {new Date(sale.createdAt).toLocaleDateString()}
                    </TableCell>
                    <TableCell className="text-right">
                      <div className="flex items-center justify-end gap-1">
                        <Button variant="ghost" size="icon" asChild>
                          <Link href={`/sales/${sale.id}`}><Eye className="h-4 w-4" /></Link>
                        </Button>
                        {sale.status !== 'REFUNDED' && (
                          <>
                            <Button variant="ghost" size="icon"
                              onClick={() => setConfirm({ action: 'checkout', id: sale.id })} title="Checkout">
                              <CreditCard className="h-4 w-4" />
                            </Button>
                            <Button variant="ghost" size="icon" className="text-[var(--destructive)]"
                              onClick={() => setConfirm({ action: 'refund', id: sale.id })} title="Refund">
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
          {totalPages > 1 && (
            <div className="flex items-center justify-center gap-2 py-4">
              <Button variant="outline" size="sm" onClick={() => setPage(p => Math.max(0, p - 1))} disabled={page === 0}>
                <ChevronLeft className="h-4 w-4" />
              </Button>
              <span className="text-sm text-[var(--muted-foreground)]">Page {page + 1} of {totalPages}</span>
              <Button variant="outline" size="sm" onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))} disabled={page >= totalPages - 1}>
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
