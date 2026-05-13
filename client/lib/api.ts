import type {
  LoginRequest, User, RegisterUserRequest, Authority,
  Sale, SaleLine, CreateSaleRequest, CreateSaleLineRequest,
  Product, ProductCategory, ProductVariation, Vendor,
  Employee, Isle, Transaction, FixedCharge, Payroll,
  FinancialStat, SalesStat, EmployeePerformanceStat, StockStat,
  Page
} from '@/types'

// Always use a relative URL so requests go through Next.js's /api rewrite proxy
// (next.config.ts: /api/* → backend).  A direct cross-origin fetch to the
// backend causes the browser to drop SameSite cookies on subsequent requests.
const BASE_URL = ''

async function request<T>(
  path: string,
  options: RequestInit = {}
): Promise<T> {
  const res = await fetch(`${BASE_URL}/api/v1${path}`, {
    ...options,
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  })
  if (!res.ok) {
    if (res.status === 401 && path !== '/auth/login') {
      const refreshed = await fetch(`${BASE_URL}/api/v1/auth/refresh`, {
        method: 'POST', credentials: 'include'
      })
      if (refreshed.ok) {
        const retry = await fetch(`${BASE_URL}/api/v1${path}`, {
          ...options, credentials: 'include',
          headers: { 'Content-Type': 'application/json', ...options.headers }
        })
        if (retry.ok) return retry.json()
      }
      throw new Error('UNAUTHORIZED')
    }
    const error = await res.text().catch(() => res.statusText)
    throw new Error(error)
  }
  if (res.status === 204) return undefined as T
  const ct = res.headers.get('content-type') ?? ''
  if (!ct.includes('application/json')) return undefined as T
  return res.json()
}

// Auth
export const auth = {
  login: (data: LoginRequest) =>
    request('/auth/login', { method: 'POST', body: JSON.stringify(data) }),
  logout: () => request('/auth/logout', { method: 'POST' }),
  refresh: () => request('/auth/refresh', { method: 'POST' }),
}

// Stats
export const stats = {
  financial: (periodType: string, page = 0, size = 10) =>
    request<Page<FinancialStat>>(
      `/stats/financial?periodType=${periodType}&page=${page}&size=${size}`
    ),
  sales: (periodType: string, page = 0, size = 10) =>
    request<Page<SalesStat>>(
      `/stats/sales?periodType=${periodType}&page=${page}&size=${size}`
    ),
  employees: (periodType: string, page = 0, size = 10) =>
    request<Page<EmployeePerformanceStat>>(
      `/stats/employees?periodType=${periodType}&page=${page}&size=${size}`
    ),
  stock: (statDate: string, periodType: string, page = 0, size = 20) =>
    request<Page<StockStat>>(
      `/stats/stock?statDate=${statDate}&periodType=${periodType}&page=${page}&size=${size}`
    ),
}

// Sales
export const sales = {
  list: (page = 0, size = 20) =>
    request<Page<Sale>>(`/sale?page=${page}&size=${size}`),
  get: (id: string) => request<Sale>(`/sale/${id}`),
  create: (data: CreateSaleRequest) =>
    request<Sale>('/sale', { method: 'POST', body: JSON.stringify(data) }),
  checkout: (id: string) => request<void>(`/sale/${id}/checkout`, { method: 'POST' }),
  refund: (id: string) => request<void>(`/sale/${id}/refund`, { method: 'POST' }),
  lines: {
    list: (saleId: string) =>
      request<SaleLine[]>(`/sale-line/sale/${saleId}`),
    add: (data: CreateSaleLineRequest) =>
      request<SaleLine>('/sale-line', { method: 'POST', body: JSON.stringify(data) }),
    update: (saleId: string, variationId: string, data: Partial<SaleLine>) =>
      request<SaleLine>(`/sale-line/${saleId}/${variationId}`, {
        method: 'PUT', body: JSON.stringify(data)
      }),
    remove: (saleId: string, variationId: string) =>
      request<void>(`/sale-line/${saleId}/${variationId}`, { method: 'DELETE' }),
  },
}

// Inventory
export const inventory = {
  products: {
    list: (page = 0, size = 20) =>
      request<Page<Product>>(`/products?page=${page}&size=${size}`),
    get: (id: string) => request<Product>(`/products/${id}`),
    create: (data: Partial<Product>) =>
      request<Product>('/products', { method: 'POST', body: JSON.stringify(data) }),
    update: (data: Product) =>
      request<Product>('/products', { method: 'PUT', body: JSON.stringify(data) }),
    remove: (id: string) => request<void>(`/products/${id}`, { method: 'DELETE' }),
  },
  categories: {
    list: (page = 0, size = 20) =>
      request<Page<ProductCategory>>(`/product-categories?page=${page}&size=${size}`),
    get: (id: string) => request<ProductCategory>(`/product-categories/${id}`),
    create: (data: Partial<ProductCategory>) =>
      request<ProductCategory>('/product-categories', {
        method: 'POST', body: JSON.stringify(data)
      }),
    update: (data: ProductCategory) =>
      request<ProductCategory>('/product-categories', {
        method: 'PUT', body: JSON.stringify(data)
      }),
    remove: (id: string) => request<void>(`/product-categories/${id}`, { method: 'DELETE' }),
  },
  variations: {
    list: (page = 0, size = 20) =>
      request<Page<ProductVariation>>(`/product-variations?page=${page}&size=${size}`),
    get: (id: string) => request<ProductVariation>(`/product-variations/${id}`),
    create: (data: Partial<ProductVariation>) =>
      request<ProductVariation>('/product-variations', {
        method: 'POST', body: JSON.stringify(data)
      }),
    update: (data: ProductVariation) =>
      request<ProductVariation>('/product-variations', {
        method: 'PUT', body: JSON.stringify(data)
      }),
    remove: (id: string) => request<void>(`/product-variations/${id}`, { method: 'DELETE' }),
  },
  vendors: {
    list: (page = 0, size = 20) =>
      request<Page<Vendor>>(`/vendors?page=${page}&size=${size}`),
    get: (id: string) => request<Vendor>(`/vendors/${id}`),
    create: (data: Partial<Vendor>) =>
      request<Vendor>('/vendors', { method: 'POST', body: JSON.stringify(data) }),
    update: (data: Vendor) =>
      request<Vendor>('/vendors', { method: 'PUT', body: JSON.stringify(data) }),
    remove: (id: string) => request<void>(`/vendors/${id}`, { method: 'DELETE' }),
  },
}

// HR
export const hr = {
  employees: {
    list: (page = 0, size = 20) =>
      request<Page<Employee>>(`/employee?page=${page}&size=${size}`),
    listActive: (page = 0, size = 20) =>
      request<Page<Employee>>(`/employee/active?page=${page}&size=${size}`),
    listTerminated: (page = 0, size = 20) =>
      request<Page<Employee>>(`/employee/terminated?page=${page}&size=${size}`),
    get: (id: string) => request<Employee>(`/employee/${id}`),
    create: (data: Partial<Employee>) =>
      request<Employee>('/employee', { method: 'POST', body: JSON.stringify(data) }),
    update: (id: string, data: Partial<Employee>) =>
      request<Employee>(`/employee/${id}`, {
        method: 'PUT', body: JSON.stringify(data)
      }),
    terminate: (id: string) =>
      request<void>(`/employee/${id}/terminate`, { method: 'PATCH' }),
    remove: (id: string) => request<void>(`/employee/${id}`, { method: 'DELETE' }),
  },
  isles: {
    list: (page = 0, size = 20) =>
      request<Page<Isle>>(`/isle?page=${page}&size=${size}`),
    byEmployee: (employeeId: string) =>
      request<Isle[]>(`/isle/employee/${employeeId}`),
    create: (data: { employeeId: string; code: string }) =>
      request<Isle>('/isle', { method: 'POST', body: JSON.stringify(data) }),
    update: (id: string, data: Partial<Isle>) =>
      request<Isle>(`/isle/${id}`, {
        method: 'PUT', body: JSON.stringify(data)
      }),
    remove: (id: string) => request<void>(`/isle/${id}`, { method: 'DELETE' }),
  },
}

// Finance
export const finance = {
  transactions: {
    list: (type?: 'PAID' | 'RECEIVED', page = 0, size = 20) =>
      request<Page<Transaction>>(
        `/finance/transactions?page=${page}&size=${size}${type ? `&type=${type}` : ''}`
      ),
    get: (id: string) => request<Transaction>(`/finance/transactions/${id}`),
    create: (data: { type: 'PAID' | 'RECEIVED'; amount: number; saleId?: string }) =>
      request<Transaction>('/finance/transactions', {
        method: 'POST', body: JSON.stringify(data)
      }),
    reverse: (id: string) => request<void>(`/finance/transactions/${id}/reverse`, { method: 'POST' }),
  },
  fixedCharges: {
    list: (activeOnly?: boolean) =>
      request<Page<FixedCharge>>(
        `/finance/fixed-charges${activeOnly !== undefined ? `?activeOnly=${activeOnly}` : ''}`
      ),
    get: (id: string) => request<FixedCharge>(`/finance/fixed-charges/${id}`),
    create: (data: { name: string; description?: string; amount: number }) =>
      request<FixedCharge>('/finance/fixed-charges', {
        method: 'POST', body: JSON.stringify(data)
      }),
    update: (data: FixedCharge) =>
      request<FixedCharge>('/finance/fixed-charges', {
        method: 'PUT', body: JSON.stringify(data)
      }),
    toggle: (id: string) => request<void>(`/finance/fixed-charges/${id}/toggle`, { method: 'PATCH' }),
  },
  payroll: {
    list: (page = 0, size = 20) =>
      request<Page<Payroll>>(`/finance/payroll?page=${page}&size=${size}`),
    byEmployee: (employeeId: string) =>
      request<Payroll[]>(`/finance/payroll/employee/${employeeId}`),
    process: (employeeId: string, startDate: string, endDate: string) =>
      request<Payroll>(
        `/finance/payroll/process/${employeeId}?startDate=${startDate}&endDate=${endDate}`,
        { method: 'POST' }
      ),
  },
}

// Users
export const users = {
  list: (page = 0, size = 20) =>
    request<Page<User>>(`/user?page=${page}&size=${size}`),
  get: (id: string) => request<User>(`/user/${id}`),
  create: (data: RegisterUserRequest) =>
    request<User>('/user', { method: 'POST', body: JSON.stringify(data) }),
  update: (id: string, data: Partial<User>) =>
    request<User>(`/user/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  activate: (id: string) => request<void>(`/user/${id}/activate`, { method: 'POST' }),
  remove: (id: string) => request<void>(`/user/${id}`, { method: 'DELETE' }),
  authorities: {
    listAll: () => request<Authority[]>('/authority'),
    list: (userId: string) =>
      request<Authority[]>(`/authority/${userId}`),
    grant: (granteeId: string, authorityId: string) =>
      request<void>(`/authority?granteeId=${granteeId}&authorityId=${authorityId}`, { method: 'POST' }),
    revoke: (userId: string, authorityId: string) =>
      request<void>(`/authority?userId=${userId}&authorityId=${authorityId}`, { method: 'DELETE' }),
  },
}
