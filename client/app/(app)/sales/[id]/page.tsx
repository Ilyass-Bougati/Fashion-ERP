'use client'

import { useState, useEffect, use } from 'react'
import Link from 'next/link'
import { ArrowLeft, Plus, Trash2, CreditCard, RotateCcw } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
  DialogFooter,
  DialogDescription,
} from '@/components/ui/dialog'
import { ToastContainer, useToast } from '@/components/ui/toast'
import { sales } from '@/lib/api'
import type { Sale, SaleLine } from '@/types'

function StatusBadge({ status }: { status: Sale['status'] }) {
  if (status === 'REFUNDED') return <Badge variant="destructive">Refunded</Badge>
  if (status === 'COMPLETED') return <Badge variant="success">Completed</Badge>
  return <Badge variant="warning">Pending</Badge>
}

export default function SaleDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params)
  const [sale, setSale] = useState<Sale | null>(null)
  const [lines, setLines] = useState<SaleLine[]>([])
  const [loading, setLoading] = useState(true)
  const [addOpen, setAddOpen] = useState(false)
  const [refundConfirm, setRefundConfirm] = useState(false)
  const [variationId, setVariationId] = useState('')
  const [quantity, setQuantity] = useState('1')
  const [price, setPrice] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const { toasts, toast, removeToast } = useToast()

  useEffect(() => {
    loadSale()
  }, [id])

  async function loadSale() {
    setLoading(true)
    try {
      const [saleData, linesData] = await Promise.all([
        sales.get(id),
        sales.lines.list(id),
      ])
      setSale(saleData)
      setLines(linesData)
    } catch {
      toast('Failed to load sale', 'error')
    } finally {
      setLoading(false)
    }
  }

  async function handleCheckout() {
    try {
      await sales.checkout(id)
      toast('Checked out successfully', 'success')
      loadSale()
    } catch {
      toast('Checkout failed', 'error')
    }
  }

  async function handleRefund() {
    try {
      await sales.refund(id)
      toast('Refunded successfully', 'success')
      loadSale()
    } catch {
      toast('Refund failed', 'error')
    } finally {
      setRefundConfirm(false)
    }
  }

  async function handleAddLine(e: React.FormEvent) {
    e.preventDefault()
    if (!variationId || !price) return
    setSubmitting(true)
    try {
      await sales.lines.add({
        saleId: id,
        productVariationId: variationId,
        quantity: parseInt(quantity),
        saleAtPrice: parseFloat(price),
      })
      toast('Line item added', 'success')
      setAddOpen(false)
      setVariationId('')
      setQuantity('1')
      setPrice('')
      loadSale()
    } catch {
      toast('Failed to add line item', 'error')
    } finally {
      setSubmitting(false)
    }
  }

  async function handleRemoveLine(variationId: string) {
    try {
      await sales.lines.remove(id, variationId)
      toast('Line item removed', 'success')
      loadSale()
    } catch {
      toast('Failed to remove line item', 'error')
    }
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-40">
        <p className="text-sm text-[var(--muted-foreground)]">Loading…</p>
      </div>
    )
  }

  if (!sale) {
    return (
      <div className="flex items-center justify-center h-40">
        <p className="text-sm text-[var(--muted-foreground)]">Sale not found</p>
      </div>
    )
  }

  const total = lines.reduce((sum, l) => sum + l.quantity * l.saleAtPrice, 0)
  const discounted = sale.discount ? total * (1 - sale.discount / 100) : total

  return (
    <div className="space-y-6 max-w-4xl">
      <div className="flex items-center gap-3">
        <Button variant="ghost" size="icon" asChild>
          <Link href="/sales"><ArrowLeft className="h-4 w-4" /></Link>
        </Button>
        <div className="flex-1">
          <h2 className="text-2xl font-bold">Sale Details</h2>
          <p className="text-xs font-mono text-[var(--muted-foreground)]">{sale.id}</p>
        </div>
        <div className="flex gap-2">
          {sale.status === 'PENDING' && (
            <Button onClick={handleCheckout} size="sm">
              <CreditCard className="mr-2 h-4 w-4" />
              Checkout
            </Button>
          )}
          {sale.status === 'COMPLETED' && (
            <Button onClick={() => setRefundConfirm(true)} size="sm" variant="destructive">
              <RotateCcw className="mr-2 h-4 w-4" />
              Refund
            </Button>
          )}
        </div>
      </div>

      {/* Info card */}
      <div className="grid grid-cols-2 gap-4 md:grid-cols-4">
        <Card>
          <CardContent className="pt-4">
            <p className="text-xs text-[var(--muted-foreground)]">Status</p>
            <StatusBadge status={sale.status} />
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-4">
            <p className="text-xs text-[var(--muted-foreground)]">Discount</p>
            <p className="text-lg font-bold">{sale.discount != null ? `${sale.discount}%` : '—'}</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-4">
            <p className="text-xs text-[var(--muted-foreground)]">Subtotal</p>
            <p className="text-lg font-bold">${total.toFixed(2)}</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-4">
            <p className="text-xs text-[var(--muted-foreground)]">Total</p>
            <p className="text-lg font-bold text-[var(--primary)]">${discounted.toFixed(2)}</p>
          </CardContent>
        </Card>
      </div>

      {/* Line items */}
      <Card>
        <CardHeader className="flex flex-row items-center justify-between">
          <CardTitle>Line Items</CardTitle>
          {sale.status === 'PENDING' && (
            <Dialog open={addOpen} onOpenChange={setAddOpen}>
              <DialogTrigger asChild>
                <Button size="sm">
                  <Plus className="mr-2 h-4 w-4" />
                  Add Item
                </Button>
              </DialogTrigger>
              <DialogContent>
                <DialogHeader>
                  <DialogTitle>Add Line Item</DialogTitle>
                </DialogHeader>
                <form onSubmit={handleAddLine} className="space-y-4">
                  <div className="space-y-2">
                    <Label>Variation ID *</Label>
                    <Input
                      placeholder="Product Variation ID"
                      value={variationId}
                      onChange={e => setVariationId(e.target.value)}
                      required
                    />
                  </div>
                  <div className="space-y-2">
                    <Label>Quantity *</Label>
                    <Input
                      type="number"
                      min="1"
                      value={quantity}
                      onChange={e => setQuantity(e.target.value)}
                      required
                    />
                  </div>
                  <div className="space-y-2">
                    <Label>Sale Price *</Label>
                    <Input
                      type="number"
                      step="0.01"
                      min="0"
                      placeholder="0.00"
                      value={price}
                      onChange={e => setPrice(e.target.value)}
                      required
                    />
                  </div>
                  <DialogFooter>
                    <Button type="submit" disabled={submitting}>
                      {submitting ? 'Adding…' : 'Add Item'}
                    </Button>
                  </DialogFooter>
                </form>
              </DialogContent>
            </Dialog>
          )}
        </CardHeader>
        <CardContent className="p-0">
          {lines.length === 0 ? (
            <div className="flex items-center justify-center h-24">
              <p className="text-sm text-[var(--muted-foreground)]">No line items yet</p>
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Variation ID</TableHead>
                  <TableHead>Qty</TableHead>
                  <TableHead>Price</TableHead>
                  <TableHead>Line Total</TableHead>
                  {sale.status === 'PENDING' && <TableHead />}
                </TableRow>
              </TableHeader>
              <TableBody>
                {lines.map(line => (
                  <TableRow key={line.productVariationId}>
                    <TableCell className="font-mono text-xs">{line.productVariationId.slice(0, 12)}…</TableCell>
                    <TableCell>{line.quantity}</TableCell>
                    <TableCell>${line.saleAtPrice.toFixed(2)}</TableCell>
                    <TableCell>${(line.quantity * line.saleAtPrice).toFixed(2)}</TableCell>
                    {sale.status === 'PENDING' && (
                      <TableCell>
                        <Button
                          variant="ghost"
                          size="icon"
                          onClick={() => handleRemoveLine(line.productVariationId)}
                          className="text-[var(--destructive)]"
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </TableCell>
                    )}
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      <Dialog open={refundConfirm} onOpenChange={setRefundConfirm}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Confirm Refund</DialogTitle>
            <DialogDescription>
              Are you sure you want to refund this sale? Stock will be restored and a refund transaction will be created. This action cannot be undone.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setRefundConfirm(false)}>Cancel</Button>
            <Button variant="destructive" onClick={handleRefund}>Refund</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  )
}
