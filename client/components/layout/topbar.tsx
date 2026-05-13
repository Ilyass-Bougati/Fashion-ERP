'use client'

import { usePathname, useRouter } from 'next/navigation'
import { LogOut, User } from 'lucide-react'
import { Avatar, AvatarFallback } from '@/components/ui/avatar'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { auth } from '@/lib/api'
import { clearAuth } from '@/lib/auth'

const routeTitles: Record<string, string> = {
  '/dashboard': 'Dashboard',
  '/sales': 'Sales',
  '/sales/new': 'New Sale',
  '/inventory': 'Inventory',
  '/inventory/categories': 'Categories',
  '/inventory/vendors': 'Vendors',
  '/hr': 'Human Resources',
  '/hr/isles': 'Isles',
  '/finance': 'Finance',
  '/finance/fixed-charges': 'Fixed Charges',
  '/finance/payroll': 'Payroll',
  '/users': 'Users',
}

function getPageTitle(pathname: string): string {
  if (routeTitles[pathname]) return routeTitles[pathname]
  if (pathname.startsWith('/sales/')) return 'Sale Details'
  return 'Fashion ERP'
}

export function Topbar() {
  const pathname = usePathname()
  const router = useRouter()
  const title = getPageTitle(pathname)

  async function handleLogout() {
    try {
      await auth.logout()
    } catch {
      // ignore
    }
    clearAuth()
    router.push('/login')
  }

  return (
    <header className="flex h-14 items-center justify-between border-b border-[var(--border)] bg-[var(--card)] px-6">
      <h1 className="text-base font-semibold text-[var(--foreground)]">{title}</h1>
      <div className="flex items-center gap-3">
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <button className="flex items-center gap-2 rounded-md p-1 hover:bg-[var(--accent)] transition-colors">
              <Avatar className="h-7 w-7">
                <AvatarFallback>
                  <User className="h-4 w-4" />
                </AvatarFallback>
              </Avatar>
            </button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end" className="w-48">
            <DropdownMenuSeparator />
            <DropdownMenuItem onClick={handleLogout} className="text-[var(--destructive)] cursor-pointer">
              <LogOut className="mr-2 h-4 w-4" />
              Sign out
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </header>
  )
}
