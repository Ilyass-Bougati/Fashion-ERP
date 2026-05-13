'use client'

import { useState } from 'react'
import { Plus, CheckCircle, Trash2, Shield } from 'lucide-react'
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

type UserForm = RegisterUserRequest & { id?: string }

const emptyForm: UserForm = {
  firstName: '', lastName: '', email: '', password: '', phoneNumber: ''
}

export default function UsersPage() {
  // Note: The API doesn't have a "list users" endpoint in the spec,
  // so we manage a local list of users created in this session + allow searching by ID
  const [userList, setUserList] = useState<User[]>([])
  const [open, setOpen] = useState(false)
  const [authOpen, setAuthOpen] = useState(false)
  const [selectedUser, setSelectedUser] = useState<User | null>(null)
  const [authorities, setAuthorities] = useState<Authority[]>([])
  const [form, setForm] = useState<UserForm>(emptyForm)
  const [searchId, setSearchId] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const { toasts, toast, removeToast } = useToast()

  function close() { setOpen(false); setForm(emptyForm) }
  function set(key: keyof UserForm, val: string) { setForm(f => ({ ...f, [key]: val })) }

  async function handleCreate(e: React.FormEvent) {
    e.preventDefault()
    setSubmitting(true)
    try {
      const user = await users.create(form) as User
      setUserList(prev => [...prev, user])
      toast('User created', 'success')
      close()
    } catch {
      toast('Failed to create user', 'error')
    } finally {
      setSubmitting(false)
    }
  }

  async function handleSearch(e: React.FormEvent) {
    e.preventDefault()
    if (!searchId.trim()) return
    try {
      const user = await users.get(searchId.trim()) as User
      setUserList(prev => {
        const exists = prev.find(u => u.id === user.id)
        return exists ? prev.map(u => u.id === user.id ? user : u) : [...prev, user]
      })
      setSearchId('')
    } catch {
      toast('User not found', 'error')
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
      setUserList(prev => prev.filter(u => u.id !== id))
    } catch {
      toast('Delete failed', 'error')
    }
  }

  async function openAuthorities(user: User) {
    setSelectedUser(user)
    try {
      const auths = await users.authorities.list(user.id) as Authority[]
      setAuthorities(auths)
    } catch {
      setAuthorities([])
    }
    setAuthOpen(true)
  }

  async function handleRevokeAuth(authorityId: string) {
    if (!selectedUser) return
    try {
      await users.authorities.revoke(selectedUser.id, authorityId)
      setAuthorities(prev => prev.filter(a => a.id !== authorityId))
      toast('Authority revoked', 'success')
    } catch {
      toast('Failed to revoke authority', 'error')
    }
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold">Users</h2>
          <p className="text-sm text-[var(--muted-foreground)]">Manage system users and permissions</p>
        </div>
        <Button onClick={() => setOpen(true)}><Plus className="mr-2 h-4 w-4" />New User</Button>
      </div>

      {/* Search by ID */}
      <Card>
        <CardContent className="pt-4">
          <form onSubmit={handleSearch} className="flex gap-3">
            <Input
              placeholder="Lookup user by ID…"
              value={searchId}
              onChange={e => setSearchId(e.target.value)}
              className="max-w-sm"
            />
            <Button type="submit" variant="outline">Search</Button>
          </form>
        </CardContent>
      </Card>

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
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              Authorities — {selectedUser?.firstName} {selectedUser?.lastName}
            </DialogTitle>
          </DialogHeader>
          {authorities.length === 0 ? (
            <p className="text-sm text-[var(--muted-foreground)]">No authorities assigned</p>
          ) : (
            <div className="space-y-2">
              {authorities.map(auth => (
                <div key={auth.id} className="flex items-center justify-between rounded-md border border-[var(--border)] px-3 py-2">
                  <span className="text-sm font-medium">{auth.name}</span>
                  <Button variant="ghost" size="sm" className="text-[var(--destructive)]"
                    onClick={() => handleRevokeAuth(auth.id)}>
                    Revoke
                  </Button>
                </div>
              ))}
            </div>
          )}
        </DialogContent>
      </Dialog>

      <Card>
        <CardHeader><CardTitle>Users</CardTitle></CardHeader>
        <CardContent className="p-0">
          {userList.length === 0 ? (
            <div className="flex items-center justify-center h-40">
              <p className="text-sm text-[var(--muted-foreground)]">Create a user or search by ID to get started</p>
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
                        <Button variant="ghost" size="icon" onClick={() => openAuthorities(user)} title="Manage authorities">
                          <Shield className="h-4 w-4" />
                        </Button>
                        {!user.active && (
                          <Button variant="ghost" size="icon" className="text-emerald-500"
                            onClick={() => handleActivate(user.id)} title="Activate">
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
        </CardContent>
      </Card>
      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </div>
  )
}
