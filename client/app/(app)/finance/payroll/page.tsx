'use client'

import { useState, useEffect } from 'react'
import { DollarSign, ChevronLeft, ChevronRight } from 'lucide-react'
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
import { finance, hr } from '@/lib/api'
import type { Payroll, Employee } from '@/types'

export default function PayrollPage() {
  const [payrolls, setPayrolls] = useState<Payroll[]>([])
  const [employees, setEmployees] = useState<Employee[]>([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(1)
  const [loading, setLoading] = useState(true)
  const [open, setOpen] = useState(false)
  const [employeeId, setEmployeeId] = useState('')
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const { toasts, toast, removeToast } = useToast()

  useEffect(() => { load() }, [page])
  useEffect(() => {
    hr.employees.listActive().then(setEmployees).catch(() => {})
  }, [])

  async function load() {
    setLoading(true)
    try {
      const res = await finance.payroll.list(page, 20)
      setPayrolls(res.content)
      setTotalPages(res.totalPages)
    } catch {
      toast('Failed to load payroll', 'error')
    } finally {
      setLoading(false)
    }
  }

  function close() { setOpen(false); setEmployeeId(''); setStartDate(''); setEndDate('') }

  async function handleProcess(e: React.FormEvent) {
    e.preventDefault()
    if (!employeeId || !startDate || !endDate) return
    setSubmitting(true)
    try {
      await finance.payroll.process(employeeId, startDate, endDate)
      toast('Payroll processed successfully', 'success')
      close()
      load()
    } catch {
      toast('Failed to process payroll', 'error')
    } finally {
      setSubmitting(false)
    }
  }

  const getEmployeeName = (id: string) => {
    const emp = employees.find(e => e.id === id)
    return emp ? `${emp.firstName} ${emp.lastName}` : id.slice(0, 8) + '…'
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold">Payroll</h2>
          <p className="text-sm text-[var(--muted-foreground)]">Process and track employee payroll</p>
        </div>
        <Button onClick={() => setOpen(true)}>
          <DollarSign className="mr-2 h-4 w-4" />Process Payroll
        </Button>
      </div>

      <Dialog open={open} onOpenChange={v => { if (!v) close() }}>
        <DialogContent>
          <DialogHeader><DialogTitle>Process Payroll</DialogTitle></DialogHeader>
          <form onSubmit={handleProcess} className="space-y-4">
            <div className="space-y-2">
              <Label>Employee *</Label>
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
            <div className="space-y-2">
              <Label>Start Date *</Label>
              <Input type="date" value={startDate} onChange={e => setStartDate(e.target.value)} required />
            </div>
            <div className="space-y-2">
              <Label>End Date *</Label>
              <Input type="date" value={endDate} onChange={e => setEndDate(e.target.value)} required />
            </div>
            <DialogFooter>
              <Button variant="outline" type="button" onClick={close}>Cancel</Button>
              <Button type="submit" disabled={submitting}>{submitting ? 'Processing…' : 'Process'}</Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      <Card>
        <CardHeader><CardTitle>Payroll History</CardTitle></CardHeader>
        <CardContent className="p-0">
          {loading ? (
            <div className="flex items-center justify-center h-40">
              <p className="text-sm text-[var(--muted-foreground)]">Loading…</p>
            </div>
          ) : payrolls.length === 0 ? (
            <div className="flex items-center justify-center h-40">
              <p className="text-sm text-[var(--muted-foreground)]">No payroll records found</p>
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Employee</TableHead>
                  <TableHead>Salary</TableHead>
                  <TableHead>Commission</TableHead>
                  <TableHead>Total</TableHead>
                  <TableHead>Date</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {payrolls.map(p => (
                  <TableRow key={p.id}>
                    <TableCell>{getEmployeeName(p.employeeId)}</TableCell>
                    <TableCell>${p.salary.toFixed(2)}</TableCell>
                    <TableCell>${p.commission.toFixed(2)}</TableCell>
                    <TableCell className="font-medium text-[var(--primary)]">
                      ${(p.salary + p.commission).toFixed(2)}
                    </TableCell>
                    <TableCell className="text-sm text-[var(--muted-foreground)]">
                      {new Date(p.createdAt).toLocaleDateString()}
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
