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
import { hr } from '@/lib/api'
import type { Isle, Employee } from '@/types'

export default function IslesPage() {
  const [isles, setIsles] = useState<Isle[]>([])
  const [employees, setEmployees] = useState<Employee[]>([])
  const [loading, setLoading] = useState(true)
  const [open, setOpen] = useState(false)
  const [editing, setEditing] = useState<Isle | null>(null)
  const [code, setCode] = useState('')
  const [employeeId, setEmployeeId] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const { toasts, toast, removeToast } = useToast()

  useEffect(() => { load() }, [])

  async function load() {
    setLoading(true)
    try {
      const [islesRes, empsRes] = await Promise.all([
        hr.isles.list(0, 100),
        hr.employees.listActive(),
      ])
      setIsles(islesRes.content)
      setEmployees(empsRes.content)
    } catch {
      toast('Failed to load isles', 'error')
    } finally {
      setLoading(false)
    }
  }

  function openNew() { setEditing(null); setCode(''); setEmployeeId(''); setOpen(true) }
  function openEdit(isle: Isle) { setEditing(isle); setCode(isle.code); setEmployeeId(isle.employeeId); setOpen(true) }
  function close() { setOpen(false); setEditing(null); setCode(''); setEmployeeId('') }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (!code || !employeeId) return
    setSubmitting(true)
    try {
      if (editing) {
        await hr.isles.update(editing.id, { code, employeeId })
        toast('Isle updated', 'success')
      } else {
        await hr.isles.create({ code, employeeId })
        toast('Isle created', 'success')
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
      await hr.isles.remove(id)
      toast('Isle deleted', 'success')
      load()
    } catch {
      toast('Delete failed', 'error')
    }
  }

  const getEmployeeName = (id: string) => {
    const emp = employees.find(e => e.id === id)
    return emp ? `${emp.firstName} ${emp.lastName}` : id.slice(0, 8)
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold">Isles</h2>
          <p className="text-sm text-[var(--muted-foreground)]">Manage store isles and assignments</p>
        </div>
        <Button onClick={openNew}><Plus className="mr-2 h-4 w-4" />Add Isle</Button>
      </div>

      <Dialog open={open} onOpenChange={v => { if (!v) close() }}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{editing ? 'Edit Isle' : 'Add Isle'}</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label>Code *</Label>
              <Input value={code} onChange={e => setCode(e.target.value)} placeholder="e.g. A1, B3" required />
            </div>
            <div className="space-y-2">
              <Label>Assigned Employee *</Label>
              <Select value={employeeId} onValueChange={setEmployeeId}>
                <SelectTrigger><SelectValue placeholder="Select employee…" /></SelectTrigger>
                <SelectContent>
                  {employees.map(emp => (
                    <SelectItem key={emp.id} value={emp.id}>
                      {emp.firstName} {emp.lastName}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <DialogFooter>
              <Button variant="outline" type="button" onClick={close}>Cancel</Button>
              <Button type="submit" disabled={submitting}>{submitting ? 'Saving…' : 'Save'}</Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      <Card>
        <CardHeader><CardTitle>All Isles</CardTitle></CardHeader>
        <CardContent className="p-0">
          {loading ? (
            <div className="flex items-center justify-center h-40">
              <p className="text-sm text-[var(--muted-foreground)]">Loading…</p>
            </div>
          ) : isles.length === 0 ? (
            <div className="flex items-center justify-center h-40">
              <p className="text-sm text-[var(--muted-foreground)]">No isles found</p>
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Code</TableHead>
                  <TableHead>Employee</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {isles.map(isle => (
                  <TableRow key={isle.id}>
                    <TableCell className="font-medium font-mono">{isle.code}</TableCell>
                    <TableCell>{getEmployeeName(isle.employeeId)}</TableCell>
                    <TableCell className="text-right">
                      <div className="flex items-center justify-end gap-1">
                        <Button variant="ghost" size="icon" onClick={() => openEdit(isle)}>
                          <Pencil className="h-4 w-4" />
                        </Button>
                        <Button variant="ghost" size="icon" className="text-[var(--destructive)]"
                          onClick={() => handleDelete(isle.id)}>
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
