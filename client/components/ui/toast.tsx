'use client'

import * as React from 'react'
import { X } from 'lucide-react'
import { cn } from '@/lib/utils'

interface ToastProps {
  message: string
  type?: 'success' | 'error' | 'info'
  onClose?: () => void
}

export function Toast({ message, type = 'info', onClose }: ToastProps) {
  const colors = {
    success: 'bg-emerald-600 text-white',
    error: 'bg-[var(--destructive)] text-[var(--destructive-foreground)]',
    info: 'bg-[var(--card)] text-[var(--foreground)] border border-[var(--border)]',
  }

  return (
    <div
      className={cn(
        'fixed bottom-4 right-4 z-50 flex items-center gap-3 rounded-lg px-4 py-3 shadow-lg',
        colors[type]
      )}
    >
      <span className="text-sm">{message}</span>
      {onClose && (
        <button onClick={onClose} className="ml-2 opacity-70 hover:opacity-100">
          <X className="h-4 w-4" />
        </button>
      )}
    </div>
  )
}

interface ToastState {
  message: string
  type: 'success' | 'error' | 'info'
  id: number
}

let toastId = 0

export function useToast() {
  const [toasts, setToasts] = React.useState<ToastState[]>([])

  const toast = React.useCallback((message: string, type: 'success' | 'error' | 'info' = 'info') => {
    const id = ++toastId
    setToasts(prev => [...prev, { message, type, id }])
    setTimeout(() => {
      setToasts(prev => prev.filter(t => t.id !== id))
    }, 3000)
  }, [])

  const removeToast = React.useCallback((id: number) => {
    setToasts(prev => prev.filter(t => t.id !== id))
  }, [])

  return { toasts, toast, removeToast }
}

export function ToastContainer({ toasts, onRemove }: { toasts: ToastState[]; onRemove: (id: number) => void }) {
  return (
    <div className="fixed bottom-4 right-4 z-50 flex flex-col gap-2">
      {toasts.map(t => (
        <Toast key={t.id} message={t.message} type={t.type} onClose={() => onRemove(t.id)} />
      ))}
    </div>
  )
}
