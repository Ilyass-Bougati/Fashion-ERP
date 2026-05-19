'use client'

import { useState, useEffect } from 'react'
import { useParams, useRouter } from 'next/navigation'
import { Plus, Pencil, Trash2, ArrowLeft } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
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
import { inventory, images } from '@/lib/api'
import { EntityAvatar } from '@/components/ui/entity-avatar'
import type { Product, ProductVariation } from '@/types'

type VariationForm = { sku: string; price: string; quantity: string }
const emptyForm: VariationForm = { sku: '', price: '', quantity: '' }

export default function ProductVariationsPage() {
  const { id: productId } = useParams<{ id: string }>()
  const router = useRouter()

  const [product, setProduct]         = useState<Product | null>(null)
  const [variations, setVariations]   = useState<ProductVariation[]>([])
  const [loading, setLoading]         = useState(true)
  const [open, setOpen]               = useState(false)
  const [editing, setEditing]         = useState<ProductVariation | null>(null)
  const [form, setForm]               = useState<VariationForm>(emptyForm)
  const [imageFile, setImageFile]     = useState<File | null>(null)
  const [submitting, setSubmitting]   = useState(false)
  const { toasts, toast, removeToast } = useToast()

  useEffect(() => { load() }, [])

  async function load() {
    setLoading(true)
    try {
      const [prod, allVariations] = await Promise.all([
        inventory.products.get(productId),
        inventory.variations.list(0, 1000),
      ])
      setProduct(prod)
      setVariations(allVariations.content.filter(v => v.productId === productId))
    } catch {
      toast('Failed to load variations', 'error')
    } finally {
      setLoading(false)
    }
  }

  function openNew() { setEditing(null); setForm(emptyForm); setImageFile(null); setOpen(true) }
  function openEdit(v: ProductVariation) {
    setEditing(v)
    setForm({ sku: v.sku, price: String(v.price), quantity: String(v.quantity) })
    setImageFile(null)
    setOpen(true)
  }
  function close() { setOpen(false); setEditing(null); setForm(emptyForm); setImageFile(null) }
  function set(key: keyof VariationForm, val: string) { setForm(f => ({ ...f, [key]: val })) }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setSubmitting(true)
    try {
      let imageId: string | undefined = editing?.imageId
      if (imageFile) {
        const uploaded = await images.upload(imageFile)
        imageId = uploaded.imageId
      }
      const payload = {
        sku: form.sku,
        price: parseFloat(form.price),
        quantity: parseInt(form.quantity, 10),
        productId,
        imageId,
      }
      if (editing) {
        await inventory.variations.update({ ...editing, ...payload })
        toast('Variation updated', 'success')
      } else {
        await inventory.variations.create(payload)
        toast('Variation created', 'success')
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
      await inventory.variations.remove(id)
      toast('Variation deleted', 'success')
      load()
    } catch {
      toast('Delete failed', 'error')
    }
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <Button variant="ghost" size="icon" onClick={() => router.back()}>
            <ArrowLeft className="h-4 w-4" />
          </Button>
          <div>
            <h2 className="text-2xl font-bold">{product?.name ?? 'Product'}</h2>
            <p className="text-sm text-[var(--muted-foreground)]">Manage product variations</p>
          </div>
        </div>
        <Button onClick={openNew}><Plus className="mr-2 h-4 w-4" />Add Variation</Button>
      </div>

      <Dialog open={open} onOpenChange={v => { if (!v) close() }}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{editing ? 'Edit Variation' : 'Add Variation'}</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleSubmit} className="space-y-3">
            <div className="space-y-1">
              <Label>SKU *</Label>
              <Input value={form.sku} onChange={e => set('sku', e.target.value)} required />
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1">
                <Label>Price *</Label>
                <Input type="number" step="0.01" value={form.price} onChange={e => set('price', e.target.value)} required />
              </div>
              <div className="space-y-1">
                <Label>Quantity *</Label>
                <Input type="number" step="1" value={form.quantity} onChange={e => set('quantity', e.target.value)} required />
              </div>
            </div>
            <div className="space-y-1">
              <Label>{editing ? 'Image (leave blank to keep current)' : 'Image'}</Label>
              <Input
                type="file"
                accept="image/*"
                onChange={e => setImageFile(e.target.files?.[0] ?? null)}
              />
            </div>
            <DialogFooter>
              <Button variant="outline" type="button" onClick={close}>Cancel</Button>
              <Button type="submit" disabled={submitting}>{submitting ? 'Saving…' : 'Save'}</Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      <Card>
        <CardHeader><CardTitle>Variations</CardTitle></CardHeader>
        <CardContent className="p-0">
          {loading ? (
            <div className="flex items-center justify-center h-40">
              <p className="text-sm text-[var(--muted-foreground)]">Loading…</p>
            </div>
          ) : variations.length === 0 ? (
            <div className="flex items-center justify-center h-40">
              <p className="text-sm text-[var(--muted-foreground)]">No variations yet</p>
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead></TableHead>
                  <TableHead>SKU</TableHead>
                  <TableHead>Price</TableHead>
                  <TableHead>Quantity</TableHead>
                  <TableHead>Created</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {variations.map(v => (
                  <TableRow key={v.id}>
                    <TableCell className="w-10">
                      <EntityAvatar imageId={v.imageId} fallback={v.sku} />
                    </TableCell>
                    <TableCell className="font-medium">{v.sku}</TableCell>
                    <TableCell>{v.price.toFixed(2)}</TableCell>
                    <TableCell>{v.quantity}</TableCell>
                    <TableCell className="text-sm text-[var(--muted-foreground)]">
                      {new Date(v.createdAt).toLocaleDateString()}
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
        </CardContent>
      </Card>
      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  )
}
