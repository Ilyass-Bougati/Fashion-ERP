'use client'

import { useState, useEffect } from 'react'
import Link from 'next/link'
import { Plus, Eye, CreditCard, RotateCcw, ChevronLeft, ChevronRight } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { ToastContainer, useToast } from '@/components/ui/toast'
import { sales } from '@/lib/api'
import type { Sale } from '@/types'

function truncate(id: string) {
  return id.slice(0, 8) + '…'
}

export default function SalesPage() {
  const [data, setData] = useState<Sale[]>([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(1)
  const [loading, setLoading] = useState(true)
  const { toasts, toast, removeToast } = useToast()

  useEffect(() => {
    load()
  }, [page])

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
                              onClick={() => handleCheckout(sale.id)}
                              title="Checkout"
                            >
                              <CreditCard className="h-4 w-4" />
                            </Button>
                            <Button
                              variant="ghost"
                              size="icon"
                              onClick={() => handleRefund(sale.id)}
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
