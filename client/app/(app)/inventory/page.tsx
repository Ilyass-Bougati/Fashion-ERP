'use client'

import { useState, useEffect } from 'react'
import { Plus, Pencil, Trash2 } from 'lucide-react'
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
  DialogTrigger,
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
import { inventory } from '@/lib/api'
import type { Product, ProductCategory } from '@/types'

export default function InventoryPage() {
  const [products, setProducts] = useState<Product[]>([])
  const [categories, setCategories] = useState<ProductCategory[]>([])
  const [loading, setLoading] = useState(true)
  const [addOpen, setAddOpen] = useState(false)
  const [editProduct, setEditProduct] = useState<Product | null>(null)
  const [name, setName] = useState('')
  const [categoryId, setCategoryId] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const { toasts, toast, removeToast } = useToast()

  useEffect(() => { load() }, [])

  async function load() {
    setLoading(true)
    try {
      const [prods, cats] = await Promise.all([
        inventory.products.list(),
        inventory.categories.list(),
      ])
      setProducts(prods.content)
      setCategories(cats.content)
    } catch {
      toast('Failed to load products', 'error')
    } finally {
      setLoading(false)
    }
  }

  function openEdit(product: Product) {
    setEditProduct(product)
    setName(product.name)
    setCategoryId(product.productCategoryId)
  }

  function resetForm() {
    setName('')
    setCategoryId('')
    setEditProduct(null)
    setAddOpen(false)
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (!name || !categoryId) return
    setSubmitting(true)
    try {
      if (editProduct) {
        await inventory.products.update({ ...editProduct, name, productCategoryId: categoryId })
        toast('Product updated', 'success')
      } else {
        await inventory.products.create({ name, productCategoryId: categoryId })
        toast('Product created', 'success')
      }
      resetForm()
      load()
    } catch {
      toast('Operation failed', 'error')
    } finally {
      setSubmitting(false)
    }
  }

  async function handleDelete(id: string) {
    try {
      await inventory.products.remove(id)
      toast('Product deleted', 'success')
      load()
    } catch {
      toast('Delete failed', 'error')
    }
  }

  const getCategoryName = (id: string) => categories.find(c => c.id === id)?.name ?? id.slice(0, 8)

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold">Products</h2>
          <p className="text-sm text-[var(--muted-foreground)]">Manage your product catalog</p>
        </div>
        <Dialog open={addOpen} onOpenChange={open => { setAddOpen(open); if (!open) resetForm() }}>
          <DialogTrigger asChild>
            <Button onClick={() => { resetForm(); setAddOpen(true) }}>
              <Plus className="mr-2 h-4 w-4" />Add Product
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader><DialogTitle>{editProduct ? 'Edit Product' : 'Add Product'}</DialogTitle></DialogHeader>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="space-y-2">
                <Label>Name *</Label>
                <Input value={name} onChange={e => setName(e.target.value)} placeholder="Product name" required />
              </div>
              <div className="space-y-2">
                <Label>Category *</Label>
                <Select value={categoryId} onValueChange={setCategoryId}>
                  <SelectTrigger><SelectValue placeholder="Select category…" /></SelectTrigger>
                  <SelectContent>
                    {categories.map(c => (
                      <SelectItem key={c.id} value={c.id}>{c.name}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <DialogFooter>
                <Button variant="outline" type="button" onClick={resetForm}>Cancel</Button>
                <Button type="submit" disabled={submitting}>{submitting ? 'Saving…' : 'Save'}</Button>
              </DialogFooter>
            </form>
          </DialogContent>
        </Dialog>
      </div>

      <Dialog open={!!editProduct} onOpenChange={open => { if (!open) resetForm() }}>
        <DialogContent>
          <DialogHeader><DialogTitle>Edit Product</DialogTitle></DialogHeader>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label>Name *</Label>
              <Input value={name} onChange={e => setName(e.target.value)} required />
            </div>
            <div className="space-y-2">
              <Label>Category *</Label>
              <Select value={categoryId} onValueChange={setCategoryId}>
                <SelectTrigger><SelectValue /></SelectTrigger>
                <SelectContent>
                  {categories.map(c => (
                    <SelectItem key={c.id} value={c.id}>{c.name}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <DialogFooter>
              <Button variant="outline" type="button" onClick={resetForm}>Cancel</Button>
              <Button type="submit" disabled={submitting}>{submitting ? 'Saving…' : 'Update'}</Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      <Card>
        <CardHeader><CardTitle>All Products</CardTitle></CardHeader>
        <CardContent className="p-0">
          {loading ? (
            <div className="flex items-center justify-center h-40">
              <p className="text-sm text-[var(--muted-foreground)]">Loading…</p>
            </div>
          ) : products.length === 0 ? (
            <div className="flex items-center justify-center h-40">
              <p className="text-sm text-[var(--muted-foreground)]">No products found</p>
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Name</TableHead>
                  <TableHead>Category</TableHead>
                  <TableHead>Created</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {products.map(p => (
                  <TableRow key={p.id}>
                    <TableCell className="font-medium">{p.name}</TableCell>
                    <TableCell>{getCategoryName(p.productCategoryId)}</TableCell>
                    <TableCell className="text-sm text-[var(--muted-foreground)]">
                      {new Date(p.createdAt).toLocaleDateString()}
                    </TableCell>
                    <TableCell className="text-right">
                      <div className="flex items-center justify-end gap-1">
                        <Button variant="ghost" size="icon" onClick={() => openEdit(p)}>
                          <Pencil className="h-4 w-4" />
                        </Button>
                        <Button variant="ghost" size="icon" className="text-[var(--destructive)]"
                          onClick={() => handleDelete(p.id)}>
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
