'use client'

import { useState, useEffect } from 'react'
import { Plus, Pencil, ToggleLeft, ToggleRight } from 'lucide-react'
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
  DialogFooter,
} from '@/components/ui/dialog'
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { ToastContainer, useToast } from '@/components/ui/toast'
import { finance } from '@/lib/api'
import type { FixedCharge } from '@/types'

export default function FixedChargesPage() {
  const [charges, setCharges] = useState<FixedCharge[]>([])
  const [loading, setLoading] = useState(true)
  const [open, setOpen] = useState(false)
  const [editing, setEditing] = useState<FixedCharge | null>(null)
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [amount, setAmount] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState<'all' | 'active' | 'inactive'>('all')
  const { toasts, toast, removeToast } = useToast()

  const filtered = charges
    .filter(c => statusFilter === 'all' || (statusFilter === 'active' ? c.active : !c.active))
    .filter(c => c.name.toLowerCase().includes(search.toLowerCase()))

  useEffect(() => { load() }, [])

  async function load() {
    setLoading(true)
    try {
      setCharges((await finance.fixedCharges.list()).content)
    } catch {
      toast('Failed to load fixed charges', 'error')
    } finally {
      setLoading(false)
    }
  }

  function openNew() { setEditing(null); setName(''); setDescription(''); setAmount(''); setOpen(true) }
  function openEdit(c: FixedCharge) {
    setEditing(c)
    setName(c.name)
    setDescription(c.description ?? '')
    setAmount(String(c.amount))
    setOpen(true)
  }
  function close() { setOpen(false); setEditing(null); setName(''); setDescription(''); setAmount('') }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setSubmitting(true)
    try {
      if (editing) {
        await finance.fixedCharges.update({
          ...editing,
          name,
          description: description || undefined,
          amount: parseFloat(amount)
        })
        toast('Fixed charge updated', 'success')
      } else {
        await finance.fixedCharges.create({ name, description: description || undefined, amount: parseFloat(amount) })
        toast('Fixed charge created', 'success')
      }
      close()
      load()
    } catch {
      toast('Operation failed', 'error')
    } finally {
      setSubmitting(false)
    }
  }

  async function handleToggle(id: string) {
    try {
      await finance.fixedCharges.toggle(id)
      toast('Status toggled', 'success')
      load()
    } catch {
      toast('Toggle failed', 'error')
    }
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold">Fixed Charges</h2>
          <p className="text-sm text-[var(--muted-foreground)]">Manage recurring fixed expenses</p>
        </div>
        <Button onClick={openNew}><Plus className="mr-2 h-4 w-4" />Add Charge</Button>
      </div>

      <Dialog open={open} onOpenChange={v => { if (!v) close() }}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{editing ? 'Edit Fixed Charge' : 'Add Fixed Charge'}</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label>Name *</Label>
              <Input value={name} onChange={e => setName(e.target.value)} required />
            </div>
            <div className="space-y-2">
              <Label>Description</Label>
              <Input value={description} onChange={e => setDescription(e.target.value)} />
            </div>
            <div className="space-y-2">
              <Label>Amount *</Label>
              <Input type="number" step="0.01" min="0" value={amount}
                onChange={e => setAmount(e.target.value)} required />
            </div>
            <DialogFooter>
              <Button variant="outline" type="button" onClick={close}>Cancel</Button>
              <Button type="submit" disabled={submitting}>{submitting ? 'Saving…' : 'Save'}</Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      {charges.length > 0 && (
        <Card>
          <CardHeader><CardTitle>Total Monthly Charges</CardTitle></CardHeader>
          <CardContent>
            <p className="text-3xl font-bold">
              ${charges.filter(c => c.active).reduce((sum, c) => sum + c.amount, 0).toFixed(2)}
            </p>
            <p className="text-sm text-[var(--muted-foreground)] mt-1">
              across {charges.filter(c => c.active).length} active charge{charges.filter(c => c.active).length !== 1 ? 's' : ''}
            </p>
          </CardContent>
        </Card>
      )}

      <Card>
        <CardHeader className="flex flex-row items-center justify-between pb-2">
          <CardTitle>All Fixed Charges</CardTitle>
          <div className="flex items-center gap-2">
            <Input
              placeholder="Search by name…"
              value={search}
              onChange={e => setSearch(e.target.value)}
              className="h-8 w-44 text-sm"
            />
            <Tabs value={statusFilter} onValueChange={v => setStatusFilter(v as typeof statusFilter)}>
              <TabsList>
                <TabsTrigger value="all">All</TabsTrigger>
                <TabsTrigger value="active">Active</TabsTrigger>
                <TabsTrigger value="inactive">Inactive</TabsTrigger>
              </TabsList>
            </Tabs>
          </div>
        </CardHeader>
        <CardContent className="p-0">
          {loading ? (
            <div className="flex items-center justify-center h-40">
              <p className="text-sm text-[var(--muted-foreground)]">Loading…</p>
            </div>
          ) : filtered.length === 0 ? (
            <div className="flex items-center justify-center h-40">
              <p className="text-sm text-[var(--muted-foreground)]">No fixed charges found</p>
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Name</TableHead>
                  <TableHead>Description</TableHead>
                  <TableHead>Amount</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {filtered.map(c => (
                  <TableRow key={c.id}>
                    <TableCell className="font-medium">{c.name}</TableCell>
                    <TableCell className="text-[var(--muted-foreground)]">{c.description || '—'}</TableCell>
                    <TableCell>${c.amount.toFixed(2)}</TableCell>
                    <TableCell>
                      <Badge variant={c.active ? 'success' : 'secondary'}>
                        {c.active ? 'Active' : 'Inactive'}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-right">
                      <div className="flex items-center justify-end gap-1">
                        <Button variant="ghost" size="icon" onClick={() => openEdit(c)}>
                          <Pencil className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="ghost" size="icon"
                          onClick={() => handleToggle(c.id)}
                          className={c.active ? 'text-amber-500' : 'text-emerald-500'}
                          title={c.active ? 'Deactivate' : 'Activate'}
                        >
                          {c.active ? <ToggleRight className="h-4 w-4" /> : <ToggleLeft className="h-4 w-4" />}
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  )
}
