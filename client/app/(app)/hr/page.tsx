'use client'

import { useState, useEffect } from 'react'
import { Plus, Pencil, Trash2, UserX, ChevronLeft, ChevronRight } from 'lucide-react'
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
import { hr } from '@/lib/api'
import type { Employee } from '@/types'

type EmployeeForm = {
  firstName: string; lastName: string; email: string; phoneNumber: string;
  CIN: string; salary: string; commission: string; hiredAt: string
}

const emptyForm: EmployeeForm = {
  firstName: '', lastName: '', email: '', phoneNumber: '',
  CIN: '', salary: '', commission: '', hiredAt: ''
}

export default function HRPage() {
  const [employees, setEmployees] = useState<Employee[]>([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(1)
  const [loading, setLoading] = useState(true)
  const [open, setOpen] = useState(false)
  const [editing, setEditing] = useState<Employee | null>(null)
  const [form, setForm] = useState<EmployeeForm>(emptyForm)
  const [submitting, setSubmitting] = useState(false)
  const { toasts, toast, removeToast } = useToast()

  useEffect(() => { load() }, [page])

  async function load() {
    setLoading(true)
    try {
      const res = await hr.employees.list(page, 20)
      setEmployees(res.content)
      setTotalPages(res.totalPages)
    } catch {
      toast('Failed to load employees', 'error')
    } finally {
      setLoading(false)
    }
  }

  function openNew() { setEditing(null); setForm(emptyForm); setOpen(true) }
  function openEdit(emp: Employee) {
    setEditing(emp)
    setForm({
      firstName: emp.firstName, lastName: emp.lastName, email: emp.email,
      phoneNumber: emp.phoneNumber, CIN: emp.CIN,
      salary: String(emp.salary), commission: String(emp.commission),
      hiredAt: emp.hiredAt.split('T')[0]
    })
    setOpen(true)
  }
  function close() { setOpen(false); setEditing(null); setForm(emptyForm) }
  function set(key: keyof EmployeeForm, val: string) { setForm(f => ({ ...f, [key]: val })) }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setSubmitting(true)
    try {
      const payload = {
        ...form,
        salary: parseFloat(form.salary),
        commission: parseFloat(form.commission),
      }
      if (editing) {
        await hr.employees.update(editing.id, payload)
        toast('Employee updated', 'success')
      } else {
        await hr.employees.create(payload)
        toast('Employee created', 'success')
      }
      close()
      load()
    } catch {
      toast('Operation failed', 'error')
    } finally {
      setSubmitting(false)
    }
  }

  async function handleTerminate(id: string) {
    try {
      await hr.employees.terminate(id)
      toast('Employee terminated', 'success')
      load()
    } catch {
      toast('Failed to terminate', 'error')
    }
  }

  async function handleDelete(id: string) {
    try {
      await hr.employees.remove(id)
      toast('Employee deleted', 'success')
      load()
    } catch {
      toast('Delete failed', 'error')
    }
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold">Employees</h2>
          <p className="text-sm text-[var(--muted-foreground)]">Manage your workforce</p>
        </div>
        <Button onClick={openNew}><Plus className="mr-2 h-4 w-4" />Add Employee</Button>
      </div>

      <Dialog open={open} onOpenChange={v => { if (!v) close() }}>
        <DialogContent className="max-w-lg">
          <DialogHeader>
            <DialogTitle>{editing ? 'Edit Employee' : 'Add Employee'}</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleSubmit} className="space-y-3">
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1">
                <Label>First Name *</Label>
                <Input value={form.firstName} onChange={e => set('firstName', e.target.value)} required />
              </div>
              <div className="space-y-1">
                <Label>Last Name *</Label>
                <Input value={form.lastName} onChange={e => set('lastName', e.target.value)} required />
              </div>
              <div className="space-y-1 col-span-2">
                <Label>Email *</Label>
                <Input type="email" value={form.email} onChange={e => set('email', e.target.value)} required />
              </div>
              <div className="space-y-1">
                <Label>Phone *</Label>
                <Input value={form.phoneNumber} onChange={e => set('phoneNumber', e.target.value)} required />
              </div>
              <div className="space-y-1">
                <Label>CIN *</Label>
                <Input value={form.CIN} onChange={e => set('CIN', e.target.value)} required />
              </div>
              <div className="space-y-1">
                <Label>Salary *</Label>
                <Input type="number" step="0.01" value={form.salary} onChange={e => set('salary', e.target.value)} required />
              </div>
              <div className="space-y-1">
                <Label>Commission (%)</Label>
                <Input type="number" step="0.01" value={form.commission} onChange={e => set('commission', e.target.value)} />
              </div>
              <div className="space-y-1 col-span-2">
                <Label>Hired At *</Label>
                <Input type="date" value={form.hiredAt} onChange={e => set('hiredAt', e.target.value)} required />
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
        <CardHeader><CardTitle>All Employees</CardTitle></CardHeader>
        <CardContent className="p-0">
          {loading ? (
            <div className="flex items-center justify-center h-40">
              <p className="text-sm text-[var(--muted-foreground)]">Loading…</p>
            </div>
          ) : employees.length === 0 ? (
            <div className="flex items-center justify-center h-40">
              <p className="text-sm text-[var(--muted-foreground)]">No employees found</p>
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Name</TableHead>
                  <TableHead>Email</TableHead>
                  <TableHead>Salary</TableHead>
                  <TableHead>Commission</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {employees.map(emp => (
                  <TableRow key={emp.id}>
                    <TableCell className="font-medium">{emp.firstName} {emp.lastName}</TableCell>
                    <TableCell>{emp.email}</TableCell>
                    <TableCell>${emp.salary.toLocaleString()}</TableCell>
                    <TableCell>{emp.commission}%</TableCell>
                    <TableCell>
                      <Badge variant={emp.active ? 'success' : 'secondary'}>
                        {emp.active ? 'Active' : 'Terminated'}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-right">
                      <div className="flex items-center justify-end gap-1">
                        <Button variant="ghost" size="icon" onClick={() => openEdit(emp)}>
                          <Pencil className="h-4 w-4" />
                        </Button>
                        {emp.active && (
                          <Button variant="ghost" size="icon" className="text-amber-500"
                            onClick={() => handleTerminate(emp.id)} title="Terminate">
                            <UserX className="h-4 w-4" />
                          </Button>
                        )}
                        <Button variant="ghost" size="icon" className="text-[var(--destructive)]"
                          onClick={() => handleDelete(emp.id)}>
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
