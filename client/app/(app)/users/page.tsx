'use client'

import { useState, useEffect } from 'react'
import { Plus, CheckCircle, Trash2, Shield, ChevronLeft, ChevronRight } from 'lucide-react'
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
import { users } from '@/lib/api'
import type { User, RegisterUserRequest, Authority } from '@/types'

const emptyForm: RegisterUserRequest = {
  firstName: '', lastName: '', email: '', password: '', phoneNumber: ''
}

export default function UsersPage() {
  const [userList, setUserList]     = useState<User[]>([])
  const [page, setPage]             = useState(0)
  const [totalPages, setTotalPages] = useState(1)
  const [loading, setLoading]       = useState(true)
  const [open, setOpen]             = useState(false)
  const [form, setForm]             = useState<RegisterUserRequest>(emptyForm)
  const [submitting, setSubmitting] = useState(false)

  const [authOpen, setAuthOpen]         = useState(false)
  const [selectedUser, setSelectedUser] = useState<User | null>(null)
  const [userAuths, setUserAuths]       = useState<Authority[]>([])
  const [allAuths, setAllAuths]         = useState<Authority[]>([])
  const [authLoading, setAuthLoading]   = useState(false)

  const { toasts, toast, removeToast } = useToast()

  useEffect(() => { load() }, [page])

  async function load() {
    setLoading(true)
    try {
      const res = await users.list(page, 20)
      setUserList(res.content)
      setTotalPages(res.totalPages)
    } catch {
      toast('Failed to load users', 'error')
    } finally {
      setLoading(false)
    }
  }

  function close() { setOpen(false); setForm(emptyForm) }
  function set(key: keyof RegisterUserRequest, val: string) {
    setForm(f => ({ ...f, [key]: val }))
  }

  async function handleCreate(e: React.FormEvent) {
    e.preventDefault()
    setSubmitting(true)
    try {
      await users.create(form)
      toast('User created', 'success')
      close()
      load()
    } catch {
      toast('Failed to create user', 'error')
    } finally {
      setSubmitting(false)
    }
  }

  async function handleActivate(id: string) {
    try {
      await users.activate(id)
      toast('User activated', 'success')
      setUserList(prev => prev.map(u => u.id === id ? { ...u, active: true } : u))
    } catch {
      toast('Activation failed', 'error')
    }
  }

  async function handleDelete(id: string) {
    try {
      await users.remove(id)
      toast('User deleted', 'success')
      load()
    } catch {
      toast('Delete failed', 'error')
    }
  }

  async function openAuthorities(user: User) {
    setSelectedUser(user)
    setAuthOpen(true)
    setAuthLoading(true)
    try {
      const [all, granted] = await Promise.all([
        users.authorities.listAll(),
        users.authorities.list(user.id),
      ])
      setAllAuths(all)
      setUserAuths(granted)
    } catch {
      toast('Failed to load authorities', 'error')
    } finally {
      setAuthLoading(false)
    }
  }

  async function handleGrant(authorityId: string) {
    if (!selectedUser) return
    try {
      await users.authorities.grant(selectedUser.id, authorityId)
      const granted = allAuths.find(a => a.id === authorityId)
      if (granted) setUserAuths(prev => [...prev, granted])
      toast('Authority granted', 'success')
    } catch {
      toast('Failed to grant authority', 'error')
    }
  }

  async function handleRevoke(authorityId: string) {
    if (!selectedUser) return
    try {
      await users.authorities.revoke(selectedUser.id, authorityId)
      setUserAuths(prev => prev.filter(a => a.id !== authorityId))
      toast('Authority revoked', 'success')
    } catch {
      toast('Failed to revoke authority', 'error')
    }
  }

  const grantedIds = new Set(userAuths.map(a => a.id))
  const available  = allAuths.filter(a => !grantedIds.has(a.id))

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold">Users</h2>
          <p className="text-sm text-[var(--muted-foreground)]">Manage system users and permissions</p>
        </div>
        <Button onClick={() => setOpen(true)}><Plus className="mr-2 h-4 w-4" />New User</Button>
      </div>

      {/* Create dialog */}
      <Dialog open={open} onOpenChange={v => { if (!v) close() }}>
        <DialogContent className="max-w-md">
          <DialogHeader><DialogTitle>Create User</DialogTitle></DialogHeader>
          <form onSubmit={handleCreate} className="space-y-3">
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
              <div className="space-y-1 col-span-2">
                <Label>Password *</Label>
                <Input type="password" value={form.password} onChange={e => set('password', e.target.value)} required />
              </div>
              <div className="space-y-1 col-span-2">
                <Label>Phone</Label>
                <Input value={form.phoneNumber} onChange={e => set('phoneNumber', e.target.value)} />
              </div>
            </div>
            <DialogFooter>
              <Button variant="outline" type="button" onClick={close}>Cancel</Button>
              <Button type="submit" disabled={submitting}>{submitting ? 'Creating…' : 'Create'}</Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      {/* Authorities dialog */}
      <Dialog open={authOpen} onOpenChange={setAuthOpen}>
        <DialogContent className="max-w-md">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <Shield className="h-4 w-4" />
              {selectedUser?.firstName} {selectedUser?.lastName}
            </DialogTitle>
          </DialogHeader>

          {authLoading ? (
            <p className="text-sm text-[var(--muted-foreground)] py-4 text-center">Loading…</p>
          ) : (
            <div className="space-y-4">
              {/* Granted authorities */}
              <div>
                <p className="text-xs font-semibold uppercase tracking-wide text-[var(--muted-foreground)] mb-2">
                  Granted ({userAuths.length})
                </p>
                {userAuths.length === 0 ? (
                  <p className="text-sm text-[var(--muted-foreground)]">None</p>
                ) : (
                  <div className="space-y-1 max-h-48 overflow-y-auto">
                    {userAuths.map(auth => (
                      <div key={auth.id} className="flex items-center justify-between rounded-md border border-[var(--border)] px-3 py-2">
                        <span className="text-sm font-mono">{auth.name}</span>
                        <Button variant="ghost" size="sm" className="text-[var(--destructive)] h-7 px-2"
                          onClick={() => handleRevoke(auth.id)}>
                          Revoke
                        </Button>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              {/* Available to grant */}
              {available.length > 0 && (
                <div>
                  <p className="text-xs font-semibold uppercase tracking-wide text-[var(--muted-foreground)] mb-2">
                    Available to grant
                  </p>
                  <div className="space-y-1 max-h-48 overflow-y-auto">
                    {available.map(auth => (
                      <div key={auth.id} className="flex items-center justify-between rounded-md border border-[var(--border)] px-3 py-2 opacity-60">
                        <span className="text-sm font-mono">{auth.name}</span>
                        <Button variant="ghost" size="sm" className="text-emerald-600 h-7 px-2"
                          onClick={() => handleGrant(auth.id)}>
                          Grant
                        </Button>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}
        </DialogContent>
      </Dialog>

      <Card>
        <CardHeader><CardTitle>Users</CardTitle></CardHeader>
        <CardContent className="p-0">
          {loading ? (
            <div className="flex items-center justify-center h-40">
              <p className="text-sm text-[var(--muted-foreground)]">Loading…</p>
            </div>
          ) : userList.length === 0 ? (
            <div className="flex items-center justify-center h-40">
              <p className="text-sm text-[var(--muted-foreground)]">No users found</p>
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Name</TableHead>
                  <TableHead>Email</TableHead>
                  <TableHead>Phone</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {userList.map(user => (
                  <TableRow key={user.id}>
                    <TableCell className="font-medium">{user.firstName} {user.lastName}</TableCell>
                    <TableCell>{user.email}</TableCell>
                    <TableCell>{user.phoneNumber}</TableCell>
                    <TableCell>
                      <Badge variant={user.active ? 'success' : 'secondary'}>
                        {user.active ? 'Active' : 'Inactive'}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-right">
                      <div className="flex items-center justify-end gap-1">
                        <Button variant="ghost" size="icon" title="Manage authorities"
                          onClick={() => openAuthorities(user)}>
                          <Shield className="h-4 w-4" />
                        </Button>
                        {!user.active && (
                          <Button variant="ghost" size="icon" className="text-emerald-500"
                            title="Activate" onClick={() => handleActivate(user.id)}>
                            <CheckCircle className="h-4 w-4" />
                          </Button>
                        )}
                        <Button variant="ghost" size="icon" className="text-[var(--destructive)]"
                          onClick={() => handleDelete(user.id)}>
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
