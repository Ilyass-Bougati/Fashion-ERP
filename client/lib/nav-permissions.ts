// Single source of truth for which authority a screen requires.
// Keys are the hrefs used by the sidebar; values are backend authority names
// (exactly as returned by GET /user/me/authorities).
export const AUTHORITY_GATES: Record<string, string> = {
  '/dashboard': 'VIEW_DASHBOARD',
  '/sales': 'READ_SALE',
  '/inventory': 'LIST_PRODUCTS',
  '/inventory/categories': 'LIST_PRODUCT_CATEGORIES',
  '/inventory/vendors': 'LIST_VENDORS',
  '/hr': 'LIST_EMPLOYEES',
  '/hr/isles': 'LIST_ISLES',
  '/finance': 'READ_TRANSACTION',
  '/finance/fixed-charges': 'READ_FIXED_CHARGE',
  '/finance/payroll': 'READ_PAYROLL',
  '/users': 'LIST_USERS',
  '/reports': 'READ_REPORTS',
}

// Ordered list of landing destinations. Used to decide where to send a user who
// cannot access the page they targeted (e.g. the default /dashboard redirect).
const LANDING_ORDER = [
  '/dashboard',
  '/sales',
  '/inventory',
  '/inventory/categories',
  '/inventory/vendors',
  '/hr',
  '/hr/isles',
  '/finance',
  '/finance/fixed-charges',
  '/finance/payroll',
  '/users',
  '/reports',
]

// A page with no gate is visible to everyone; otherwise the user must hold the authority.
export function canView(href: string, authorities: string[]): boolean {
  const required = AUTHORITY_GATES[href]
  return !required || authorities.includes(required)
}

// First destination the user is allowed to open, in menu order. Null if none.
export function firstAllowedPath(authorities: string[]): string | null {
  return LANDING_ORDER.find(href => canView(href, authorities)) ?? null
}
