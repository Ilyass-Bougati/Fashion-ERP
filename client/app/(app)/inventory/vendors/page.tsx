'use client'

import { useState, useEffect } from 'react'
import { Plus, Pencil, Trash2, ChevronLeft, ChevronRight } from 'lucide-react'
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
import { ToastContainer, useToast } from '@/components/ui/toast'
import { inventory } from '@/lib/api'
import type { Vendor } from '@/types'

const empty: Partial<Vendor> = {
  companyName: '', email: '', contactName: '', phoneNumber: '', paymentTerms: '', productId: ''
}

export default function VendorsPage() {
  const [vendors, setVendors] = useState<Vendor[]>([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(1)
  const [loading, setLoading] = useState(true)
  const [open, setOpen] = useState(false)
  const [editing, setEditing] = useState<Vendor | null>(null)
  const [form, setForm] = useState<Partial<Vendor>>(empty)
  const [submitting, setSubmitting] = useState(false)
  const { toasts, toast, removeToast } = useToast()

  useEffect(() => { load() }, [page])

  async function load() {
    setLoading(true)
    try {
      const res = await inventory.vendors.list(page, 20)
      setVendors(res.content)
      setTotalPages(res.totalPages)
    } catch {
      toast('Failed to load vendors', 'error')
    } finally {
      setLoading(false)
    }
  }

  function openNew() { setEditing(null); setForm(empty); setOpen(true) }
  function openEdit(v: Vendor) { setEditing(v); setForm(v); setOpen(true) }
  function close() { setOpen(false); setEditing(null); setForm(empty) }
  function set(key: keyof Vendor, val: string) { setForm(f => ({ ...f, [key]: val })) }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setSubmitting(true)
    try {
      if (editing) {
        await inventory.vendors.update({ ...editing, ...form } as Vendor)
        toast('Vendor updated', 'success')
      } else {
        await inventory.vendors.create(form)
        toast('Vendor created', 'success')
      }
      close()
      load()
    } catch {
      toast('Operation failed', 'error')
    } finally {
      setSubmitting(false)
    }
  }

  async function handleDelete(id: string) {
    try {
      await inventory.vendors.remove(id)
      toast('Vendor deleted', 'success')
      load()
    } catch {
      toast('Delete failed', 'error')
    }
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold">Vendors</h2>
          <p className="text-sm text-[var(--muted-foreground)]">Manage your supplier relationships</p>
        </div>
        <Button onClick={openNew}><Plus className="mr-2 h-4 w-4" />Add Vendor</Button>
      </div>

      <Dialog open={open} onOpenChange={v => { if (!v) close() }}>
        <DialogContent className="max-w-md">
          <DialogHeader>
            <DialogTitle>{editing ? 'Edit Vendor' : 'Add Vendor'}</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleSubmit} className="space-y-3">
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1 col-span-2">
                <Label>Company Name *</Label>
                <Input value={form.companyName ?? ''} onChange={e => set('companyName', e.target.value)} required />
              </div>
              <div className="space-y-1">
                <Label>Contact Name *</Label>
                <Input value={form.contactName ?? ''} onChange={e => set('contactName', e.target.value)} required />
              </div>
              <div className="space-y-1">
                <Label>Phone *</Label>
                <Input value={form.phoneNumber ?? ''} onChange={e => set('phoneNumber', e.target.value)} required />
              </div>
              <div className="space-y-1 col-span-2">
                <Label>Email *</Label>
                <Input type="email" value={form.email ?? ''} onChange={e => set('email', e.target.value)} required />
              </div>
              <div className="space-y-1 col-span-2">
                <Label>Payment Terms</Label>
                <Input value={form.paymentTerms ?? ''} onChange={e => set('paymentTerms', e.target.value)} />
              </div>
              <div className="space-y-1 col-span-2">
                <Label>Product ID</Label>
                <Input value={form.productId ?? ''} onChange={e => set('productId', e.target.value)} />
              </div>
            </div>
            <DialogFooter>
              <Button variant="outline" type="button" onClick={close}>Cancel</Button>
              <Button type="submit" disabled={submitting}>{submitting ? 'Saving…' : 'Save'}</Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      <Card>
        <CardHeader><CardTitle>All Vendors</CardTitle></CardHeader>
        <CardContent className="p-0">
          {loading ? (
            <div className="flex items-center justify-center h-40">
              <p className="text-sm text-[var(--muted-foreground)]">Loading…</p>
            </div>
          ) : vendors.length === 0 ? (
            <div className="flex items-center justify-center h-40">
              <p className="text-sm text-[var(--muted-foreground)]">No vendors found</p>
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Company</TableHead>
                  <TableHead>Contact</TableHead>
                  <TableHead>Email</TableHead>
                  <TableHead>Phone</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {vendors.map(v => (
                  <TableRow key={v.id}>
                    <TableCell className="font-medium">{v.companyName}</TableCell>
                    <TableCell>{v.contactName}</TableCell>
                    <TableCell className="text-sm">{v.email}</TableCell>
                    <TableCell>{v.phoneNumber}</TableCell>
                    <TableCell>
                      <Badge variant={v.active ? 'success' : 'secondary'}>
                        {v.active ? 'Active' : 'Inactive'}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-right">
                      <div className="flex items-center justify-end gap-1">
                        <Button variant="ghost" size="icon" onClick={() => openEdit(v)}>
                          <Pencil className="h-4 w-4" />
                        </Button>
                        <Button variant="ghost" size="icon" className="text-[var(--destructive)]"
                          onClick={() => handleDelete(v.id)}>
                          <Trash2 className="h-4 w-4" />
                        </Button>
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
