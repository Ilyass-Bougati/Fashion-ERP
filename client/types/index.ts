// Auth
export interface LoginRequest { email: string; password: string }

// User
export interface User {
  id: string; firstName: string; lastName: string; email: string;
  phoneNumber: string; active: boolean; createdAt: string; updatedAt: string
}
export interface RegisterUserRequest {
  firstName: string; lastName: string; email: string;
  password: string; phoneNumber: string
}
export interface Authority { id: string; name: string }

// Sales
export interface Sale {
  id: string; discount: number | null; employeeId: string;
  status: 'PENDING' | 'COMPLETED' | 'REFUNDED'; createdAt: string; updatedAt: string
}
export interface SaleLine {
  saleId: string; productVariationId: string;
  quantity: number; saleAtPrice: number
}
export interface CreateSaleRequest { discount?: number; employeeId: string }
export interface CreateSaleLineRequest {
  saleId: string; productVariationId: string;
  quantity: number; saleAtPrice: number
}

// Inventory
export interface Product {
  id: string; name: string; productCategoryId: string;
  imageId?: string; createdAt: string; updatedAt: string
}
export interface ProductCategory { id: string; name: string; description: string }
export interface ProductVariation {
  id: string; sku: string; price: number; productId: string;
  quantity: number; imageId?: string; createdAt: string; updatedAt: string
}
export interface Vendor {
  id: string; companyName: string; email: string; contactName: string;
  phoneNumber: string; paymentTerms: string; active: boolean;
  productId: string; createdAt: string; updatedAt: string
}

// HR
export interface Employee {
  id: string; imageId: string; firstName: string; lastName: string;
  phoneNumber: string; CIN: string; email: string; active: boolean;
  salary: number; commission: number; hiredAt: string;
  terminatedAt?: string; createdAt: string; updatedAt: string
}
export interface Isle {
  id: string; employeeId: string; code: string
}

// Finance
export interface Transaction {
  id: string; type: 'PAID' | 'RECEIVED'; saleId?: string;
  amount: number; createdAt: string
}
export interface FixedCharge {
  id: string; name: string; description?: string;
  amount: number; active: boolean; createdAt: string
}
export interface Payroll {
  id: string; salary: number; transactionId: string;
  employeeId: string; commission: number; createdAt: string
}

// Stats
export type PeriodType = 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'YEARLY'
export interface FinancialStat {
  revenue: number; payroll: number; fixedCharges: number; netProfit: number; period: string
}
export interface SalesStat {
  transactionCount: number; grossRevenue: number; netRevenue: number;
  discounts: number; topCategories: string[]; topProducts: string[]; period: string
}
export interface EmployeePerformanceStat {
  id: string; statDate: string; periodType: string;
  employeeCin: string; employeeFullName: string;
  salesCount: number; grossSalesAmount: number;
  commissionEarned: number; itemsSold: number;
  avgDiscountGiven: number; computedAt: string
}
export interface StockStat {
  productVariationId: string; sku: string; productName: string;
  quantityOnHand: number; value: number; velocity30Days: number
}

// Predictions
export interface SalesPrediction {
  id: string; targetDate: string; periodType: string
  predictedNetRevenue: number | null; netRevenueLowerBound: number | null; netRevenueUpperBound: number | null
  predictedUnitsSold: number | null; unitsSoldLowerBound: number | null; unitsSoldUpperBound: number | null
  predictedTransactions: number | null; transactionsLowerBound: number | null; transactionsUpperBound: number | null
  modelVersion: string; predictedAt: string
}
export interface EmployeePerformancePrediction {
  id: string; targetDate: string; periodType: string
  employeeCin: string; employeeFullName: string
  predictedGrossSales: number | null; grossSalesLowerBound: number | null; grossSalesUpperBound: number | null
  modelVersion: string; predictedAt: string
}
export interface FinancialPrediction {
  id: string; targetDate: string; periodType: string
  predictedTotalRevenue: number | null; totalRevenueLowerBound: number | null; totalRevenueUpperBound: number | null
  predictedNetProfit: number | null; netProfitLowerBound: number | null; netProfitUpperBound: number | null
  modelVersion: string; predictedAt: string
}

// Reports
export type ReportType = 'PDF' | 'EXCEL' | 'CSV'
export type ReportStatus = 'PENDING' | 'DONE' | 'FAILED'
export type ReportCategory = 'FINANCIAL' | 'SALES' | 'EMPLOYEE_PERFORMANCE'

export interface Report {
  id: string
  title: string
  type: ReportType
  status: ReportStatus
  objectKey: string
  bucketName: string
  contentType: string
  category: ReportCategory
  generatedAt: string | null
}

export interface SavedReport {
  id: string
  url: string
  expiresAt: string | null
}

// API Pagination
export interface Page<T> {
  content: T[]; totalElements: number; totalPages: number;
  size: number; number: number; first: boolean; last: boolean
}
