'use client'

import Link from 'next/link'
import { usePathname } from 'next/navigation'
import {
  LayoutDashboard,
  ShoppingCart,
  Package,
  Users,
  DollarSign,
  UserCog,
  Shirt,
  FolderOpen,
  Truck,
  MapPin,
  CreditCard,
  ReceiptText,
  FileText,
  ChevronRight,
} from 'lucide-react'
import { cn } from '@/lib/utils'
import { useAuthorities } from '@/components/authorities-provider'
import { canView } from '@/lib/nav-permissions'

interface NavItem {
  label: string
  href: string
  icon: React.ReactNode
  children?: NavItem[]
}

interface NavSection {
  title: string
  items: NavItem[]
}

const navSections: NavSection[] = [
  {
    title: 'Overview',
    items: [
      { label: 'Dashboard', href: '/dashboard', icon: <LayoutDashboard className="h-4 w-4" /> },
    ],
  },
  {
    title: 'Commerce',
    items: [
      { label: 'Sales', href: '/sales', icon: <ShoppingCart className="h-4 w-4" /> },
      {
        label: 'Inventory',
        href: '/inventory',
        icon: <Package className="h-4 w-4" />,
        children: [
          { label: 'Products', href: '/inventory', icon: <Package className="h-4 w-4" /> },
          { label: 'Categories', href: '/inventory/categories', icon: <FolderOpen className="h-4 w-4" /> },
          { label: 'Vendors', href: '/inventory/vendors', icon: <Truck className="h-4 w-4" /> },
        ],
      },
    ],
  },
  {
    title: 'People',
    items: [
      {
        label: 'HR',
        href: '/hr',
        icon: <Users className="h-4 w-4" />,
        children: [
          { label: 'Employees', href: '/hr', icon: <Users className="h-4 w-4" /> },
          { label: 'Isles', href: '/hr/isles', icon: <MapPin className="h-4 w-4" /> },
        ],
      },
    ],
  },
  {
    title: 'Money',
    items: [
      {
        label: 'Finance',
        href: '/finance',
        icon: <DollarSign className="h-4 w-4" />,
        children: [
          { label: 'Transactions', href: '/finance', icon: <CreditCard className="h-4 w-4" /> },
          { label: 'Fixed Charges', href: '/finance/fixed-charges', icon: <ReceiptText className="h-4 w-4" /> },
          { label: 'Payroll', href: '/finance/payroll', icon: <DollarSign className="h-4 w-4" /> },
        ],
      },
    ],
  },
  {
    title: 'Admin',
    items: [
      { label: 'Users', href: '/users', icon: <UserCog className="h-4 w-4" /> },
      { label: 'Reports', href: '/reports', icon: <FileText className="h-4 w-4" /> },
    ],
  },
]

function NavItemComponent({ item, depth = 0 }: { item: NavItem; depth?: number }) {
  const pathname = usePathname()
  const isActive = pathname === item.href || (item.href !== '/dashboard' && item.href !== '/' && pathname.startsWith(item.href + '/'))
  const isExactActive = pathname === item.href

  if (item.children) {
    const isParentActive = pathname.startsWith(item.href)
    return (
      <div>
        <Link
          href={item.href}
          className={cn(
            'flex items-center gap-2 rounded-md px-3 py-2 text-sm transition-colors',
            isParentActive
              ? 'bg-[var(--sidebar-accent)] text-[var(--sidebar-primary)] font-medium'
              : 'text-[var(--sidebar-foreground)] hover:bg-[var(--sidebar-accent)] hover:text-[var(--sidebar-accent-foreground)]'
          )}
        >
          {item.icon}
          <span>{item.label}</span>
          <ChevronRight className={cn('ml-auto h-3 w-3 transition-transform', isParentActive && 'rotate-90')} />
        </Link>
        {isParentActive && (
          <div className="mt-1 ml-4 space-y-0.5 border-l border-[var(--sidebar-border)] pl-3">
            {item.children.map(child => (
              <Link
                key={child.href}
                href={child.href}
                className={cn(
                  'flex items-center gap-2 rounded-md px-2 py-1.5 text-xs transition-colors',
                  pathname === child.href
                    ? 'bg-[var(--sidebar-accent)] text-[var(--sidebar-primary)] font-medium'
                    : 'text-[var(--muted-foreground)] hover:bg-[var(--sidebar-accent)] hover:text-[var(--sidebar-accent-foreground)]'
                )}
              >
                {child.icon}
                <span>{child.label}</span>
              </Link>
            ))}
          </div>
        )}
      </div>
    )
  }

  return (
    <Link
      href={item.href}
      className={cn(
        'flex items-center gap-2 rounded-md px-3 py-2 text-sm transition-colors',
        isExactActive
          ? 'bg-[var(--sidebar-accent)] text-[var(--sidebar-primary)] font-medium'
          : 'text-[var(--sidebar-foreground)] hover:bg-[var(--sidebar-accent)] hover:text-[var(--sidebar-accent-foreground)]'
      )}
    >
      {item.icon}
      <span>{item.label}</span>
    </Link>
  )
}

export function Sidebar() {
  const authorities = useAuthorities()

  const visibleSections = navSections.map(section => ({
    ...section,
    items: section.items
      .map(item => {
        if (item.children) {
          // Keep only the sub-items the user can see; hide the whole group if none remain.
          const children = item.children.filter(child => canView(child.href, authorities))
          return children.length > 0 ? { ...item, children } : null
        }
        return canView(item.href, authorities) ? item : null
      })
      .filter((item): item is NavItem => item !== null),
  })).filter(section => section.items.length > 0)

  return (
    <aside className="flex h-full w-60 flex-col border-r border-[var(--sidebar-border)] bg-[var(--sidebar)]">
      {/* Brand */}
      <div className="flex h-14 items-center gap-2 border-b border-[var(--sidebar-border)] px-4">
        <div className="flex h-7 w-7 items-center justify-center rounded-md bg-[var(--sidebar-primary)]">
          <Shirt className="h-4 w-4 text-white" />
        </div>
        <span className="text-sm font-semibold text-[var(--sidebar-foreground)]">Fashion ERP</span>
      </div>

      {/* Navigation */}
      <nav className="flex-1 overflow-y-auto py-4 px-2 space-y-4">
        {visibleSections.map(section => (
          <div key={section.title}>
            <p className="mb-1 px-3 text-xs font-semibold uppercase tracking-wider text-[var(--muted-foreground)]">
              {section.title}
            </p>
            <div className="space-y-0.5">
              {section.items.map(item => (
                <NavItemComponent key={item.href} item={item} />
              ))}
            </div>
          </div>
        ))}
      </nav>
    </aside>
  )
}
