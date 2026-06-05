'use client'

import { useState, useEffect } from 'react'
import { Download, ChevronLeft, ChevronRight, ShieldOff } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { ToastContainer, useToast } from '@/components/ui/toast'
import { reports } from '@/lib/api'
import { useAuthorities } from '@/components/authorities-provider'
import type { Report } from '@/types'

const REQUIRED_AUTHORITY = 'READ_REPORTS'

function statusVariant(status: Report['status']): 'success' | 'secondary' | 'destructive' {
  if (status === 'DONE') return 'success'
  if (status === 'FAILED') return 'destructive'
  return 'secondary'
}

function categoryLabel(category: Report['category']) {
  if (category === 'EMPLOYEE_PERFORMANCE') return 'Employee Performance'
  if (category === 'FINANCIAL') return 'Financial'
  return 'Sales'
}

export default function ReportsPage() {
  const authorities = useAuthorities()
  const authorized  = authorities.includes(REQUIRED_AUTHORITY)

  const [reportList, setReportList] = useState<Report[]>([])
  const [page, setPage]             = useState(0)
  const [totalPages, setTotalPages] = useState(1)
  const [loading, setLoading]       = useState(true)
  const [downloading, setDownloading] = useState<string | null>(null)

  const { toasts, toast, removeToast } = useToast()

  async function handleDownload(report: Report) {
    setDownloading(report.id)
    try {
      const res = await fetch(`/api/v1/reports/${report.id}/download`, { credentials: 'include' })
      if (!res.ok) throw new Error('Download failed')
      const blob = await res.blob()
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `${report.title}.${report.type.toLowerCase()}`
      a.click()
      URL.revokeObjectURL(url)
    } catch {
      toast('Failed to download report', 'error')
    } finally {
      setDownloading(null)
    }
  }

  useEffect(() => { if (authorized) load() }, [authorized, page])

  async function load() {
    setLoading(true)
    try {
      const res = await reports.list(page, 20)
      setReportList(res.content)
      setTotalPages(res.totalPages)
    } catch {
      toast('Failed to load reports', 'error')
    } finally {
      setLoading(false)
    }
  }

  if (!authorized && authorities.length > 0) {
    return (
      <div className="flex flex-col items-center justify-center h-64 gap-3">
        <ShieldOff className="h-10 w-10 text-[var(--muted-foreground)]" />
        <h2 className="text-lg font-semibold">Access Denied</h2>
        <p className="text-sm text-[var(--muted-foreground)]">
          You need the <span className="font-mono">{REQUIRED_AUTHORITY}</span> authority to view reports.
        </p>
      </div>
    )
  }

  return (
    <div className="space-y-4">
      <div>
        <h2 className="text-2xl font-bold">Reports</h2>
        <p className="text-sm text-[var(--muted-foreground)]">Browse generated reports and download them</p>
      </div>

      <Card>
        <CardHeader><CardTitle>All Reports</CardTitle></CardHeader>
        <CardContent className="p-0">
          {loading ? (
            <div className="flex items-center justify-center h-40">
              <p className="text-sm text-[var(--muted-foreground)]">Loading…</p>
            </div>
          ) : reportList.length === 0 ? (
            <div className="flex items-center justify-center h-40">
              <p className="text-sm text-[var(--muted-foreground)]">No reports found</p>
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Title</TableHead>
                  <TableHead>Category</TableHead>
                  <TableHead>Type</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Generated At</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {reportList.map(report => (
                  <TableRow key={report.id}>
                    <TableCell className="font-medium max-w-xs truncate" title={report.title}>
                      {report.title}
                    </TableCell>
                    <TableCell>{categoryLabel(report.category)}</TableCell>
                    <TableCell>
                      <span className="font-mono text-xs">{report.type}</span>
                    </TableCell>
                    <TableCell>
                      <Badge variant={statusVariant(report.status)}>
                        {report.status}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-sm text-[var(--muted-foreground)]">
                      {report.generatedAt
                        ? new Date(report.generatedAt).toLocaleString()
                        : '—'}
                    </TableCell>
                    <TableCell className="text-right">
                      <Button
                        size="sm"
                        variant={report.status === 'DONE' ? 'default' : 'ghost'}
                        disabled={report.status !== 'DONE' || downloading === report.id}
                        onClick={() => handleDownload(report)}
                      >
                        <Download className="mr-1.5 h-4 w-4" />
                        {downloading === report.id ? 'Downloading…' : 'Download'}
                      </Button>
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
