'use client'

import { useState, useEffect } from 'react'
import { Plus, RotateCcw } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { ToastContainer, useToast } from '@/components/ui/toast'
import { finance } from '@/lib/api'
import type { Transaction } from '@/types'

export default function FinancePage() {
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [filter, setFilter] = useState<'ALL' | 'PAID' | 'RECEIVED'>('ALL')
  const [loading, setLoading] = useState(true)
  const [open, setOpen] = useState(false)
  const [type, setType] = useState<'PAID' | 'RECEIVED'>('RECEIVED')
  const [amount, setAmount] = useState('')
  const [saleId, setSaleId] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const { toasts, toast, removeToast } = useToast()

  useEffect(() => { load() }, [filter])

  async function load() {
    setLoading(true)
    try {
      const res = await finance.transactions.list(filter === 'ALL' ? undefined : filter)
      setTransactions(res.content)
    } catch {
      toast('Failed to load transactions', 'error')
    } finally {
      setLoading(false)
    }
  }

  function close() { setOpen(false); setAmount(''); setSaleId('') }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (!amount) return
    setSubmitting(true)
    try {
      await finance.transactions.create({
        type,
        amount: parseFloat(amount),
        ...(saleId ? { saleId } : {}),
      })
      toast('Transaction created', 'success')
      close()
      load()
    } catch {
      toast('Failed to create transaction', 'error')
    } finally {
      setSubmitting(false)
    }
  }

  async function handleReverse(id: string) {
    try {
      await finance.transactions.reverse(id)
      toast('Transaction reversed', 'success')
      load()
    } catch {
      toast('Failed to reverse transaction', 'error')
    }
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold">Transactions</h2>
          <p className="text-sm text-[var(--muted-foreground)]">Financial transaction history</p>
        </div>
        <Button onClick={() => setOpen(true)}><Plus className="mr-2 h-4 w-4" />New Transaction</Button>
      </div>

      <Dialog open={open} onOpenChange={v => { if (!v) close() }}>
        <DialogContent>
          <DialogHeader><DialogTitle>New Transaction</DialogTitle></DialogHeader>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label>Type *</Label>
              <Select value={type} onValueChange={v => setType(v as 'PAID' | 'RECEIVED')}>
                <SelectTrigger><SelectValue /></SelectTrigger>
                <SelectContent>
                  <SelectItem value="RECEIVED">Received</SelectItem>
                  <SelectItem value="PAID">Paid</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label>Amount *</Label>
              <Input type="number" step="0.01" min="0" value={amount}
                onChange={e => setAmount(e.target.value)} required />
            </div>
            <div className="space-y-2">
              <Label>Sale ID (optional)</Label>
              <Input value={saleId} onChange={e => setSaleId(e.target.value)} placeholder="Link to sale…" />
            </div>
            <DialogFooter>
              <Button variant="outline" type="button" onClick={close}>Cancel</Button>
              <Button type="submit" disabled={submitting}>{submitting ? 'Creating…' : 'Create'}</Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

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
                      <TableHead className="text-right">Actions</TableHead>
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
                        <TableCell className="text-right">
                          <Button variant="ghost" size="icon" onClick={() => handleReverse(t.id)} title="Reverse">
                            <RotateCcw className="h-4 w-4" />
                          </Button>
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
