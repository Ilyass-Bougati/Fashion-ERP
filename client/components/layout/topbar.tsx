'use client'

import { usePathname, useRouter } from 'next/navigation'
import { LogOut, User, Sun, Moon } from 'lucide-react'
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
import { useTheme } from '@/components/theme-provider'

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
  const router   = useRouter()
  const title    = getPageTitle(pathname)
  const { theme, toggle } = useTheme()

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
        <button
          onClick={toggle}
          className="flex h-8 w-8 items-center justify-center rounded-md hover:bg-[var(--accent)] transition-colors"
          aria-label="Toggle theme"
        >
          {theme === 'dark'
            ? <Sun  className="h-4 w-4 text-[var(--muted-foreground)]" />
            : <Moon className="h-4 w-4 text-[var(--muted-foreground)]" />
          }
        </button>
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
