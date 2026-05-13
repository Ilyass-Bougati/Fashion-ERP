'use client'

import { useState, useEffect } from 'react'
import { TrendingUp, DollarSign, Users, AlertTriangle } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Badge } from '@/components/ui/badge'
import { BarChart } from '@/components/charts/bar-chart'
import { stats } from '@/lib/api'
import type { FinancialStat, SalesStat, EmployeePerformanceStat, StockStat, PeriodType } from '@/types'

function StatCard({
  title,
  value,
  icon: Icon,
  sub,
}: {
  title: string
  value: string
  icon: React.ElementType
  sub?: string
}) {
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between pb-2">
        <CardTitle className="text-sm font-medium text-[var(--muted-foreground)]">{title}</CardTitle>
        <Icon className="h-4 w-4 text-[var(--muted-foreground)]" />
      </CardHeader>
      <CardContent>
        <div className="text-2xl font-bold">{value}</div>
        {sub && <p className="text-xs text-[var(--muted-foreground)] mt-1">{sub}</p>}
      </CardContent>
    </Card>
  )
}

function fmt(n: number) {
  return `$${n.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

export default function DashboardPage() {
  const [period, setPeriod] = useState<PeriodType>('MONTHLY')
  const [financialData, setFinancialData] = useState<FinancialStat | null>(null)
  const [salesData, setSalesData] = useState<SalesStat | null>(null)
  const [employees, setEmployees] = useState<EmployeePerformanceStat[]>([])
  const [stockAlerts, setStockAlerts] = useState<StockStat[]>([])
  const [chartData, setChartData] = useState<Array<{ period: string; revenue: number }>>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    loadData()
  }, [period])

  async function loadData() {
    setLoading(true)
    setError('')
    try {
      const today = new Date().toISOString().split('T')[0]
      const [finRes, salesRes, empRes, stockRes] = await Promise.allSettled([
        stats.financial(period, 0, 12),
        stats.sales(period, 0, 1),
        stats.employees(period, 0, 5),
        stats.stock(today, period, 0, 20),
      ])

      if (finRes.status === 'fulfilled' && finRes.value.content.length > 0) {
        const sorted = [...finRes.value.content].sort((a, b) => a.period.localeCompare(b.period))
        setFinancialData(sorted[sorted.length - 1])
        setChartData(sorted.map(s => ({ period: s.period, revenue: s.revenue })))
      }
      if (salesRes.status === 'fulfilled' && salesRes.value.content.length > 0) {
        setSalesData(salesRes.value.content[0])
      }
      if (empRes.status === 'fulfilled') {
        setEmployees(empRes.value.content)
      }
      if (stockRes.status === 'fulfilled') {
        const low = stockRes.value.content.filter(s => s.quantityOnHand < 10)
        setStockAlerts(low.slice(0, 8))
      }
    } catch {
      setError('Failed to load dashboard data.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold">Dashboard</h2>
          <p className="text-sm text-[var(--muted-foreground)]">Overview of your business metrics</p>
        </div>
        <Select value={period} onValueChange={v => setPeriod(v as PeriodType)}>
          <SelectTrigger className="w-36">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="DAILY">Daily</SelectItem>
            <SelectItem value="WEEKLY">Weekly</SelectItem>
            <SelectItem value="MONTHLY">Monthly</SelectItem>
            <SelectItem value="YEARLY">Yearly</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {error && (
        <div className="rounded-md bg-[var(--destructive)]/10 border border-[var(--destructive)] px-4 py-3 text-sm text-[var(--destructive)]">
          {error}
        </div>
      )}

      {/* Financial Stat Cards */}
      {loading ? (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          {[...Array(4)].map((_, i) => (
            <Card key={i} className="animate-pulse">
              <CardContent className="h-24" />
            </Card>
          ))}
        </div>
      ) : (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          <StatCard
            title="Revenue"
            value={financialData ? fmt(financialData.revenue) : '$0.00'}
            icon={DollarSign}
          />
          <StatCard
            title="Net Profit"
            value={financialData ? fmt(financialData.netProfit) : '$0.00'}
            icon={TrendingUp}
            sub={financialData && financialData.revenue > 0
              ? `${((financialData.netProfit / financialData.revenue) * 100).toFixed(1)}% margin`
              : undefined}
          />
          <StatCard
            title="Payroll"
            value={financialData ? fmt(financialData.payroll) : '$0.00'}
            icon={Users}
          />
          <StatCard
            title="Fixed Charges"
            value={financialData ? fmt(financialData.fixedCharges) : '$0.00'}
            icon={DollarSign}
          />
        </div>
      )}

      {/* Sales Metrics */}
      {salesData && (
        <div className="grid gap-4 md:grid-cols-3">
          <StatCard title="Transactions" value={String(salesData.transactionCount)} icon={TrendingUp} />
          <StatCard title="Gross Revenue" value={fmt(salesData.grossRevenue)} icon={DollarSign} />
          <StatCard title="Discounts" value={fmt(salesData.discounts)} icon={DollarSign} />
        </div>
      )}

      {/* Chart + Leaderboard */}
      <div className="grid gap-6 lg:grid-cols-3">
        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle>Revenue Trend</CardTitle>
            <CardDescription>Revenue over recent {period.toLowerCase()} periods</CardDescription>
          </CardHeader>
          <CardContent>
            {chartData.length > 0 ? (
              <BarChart data={chartData} xKey="period" yKey="revenue" />
            ) : (
              <div className="flex h-48 items-center justify-center text-[var(--muted-foreground)] text-sm">
                No data available
              </div>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Top Employees</CardTitle>
            <CardDescription>By gross sales</CardDescription>
          </CardHeader>
          <CardContent className="p-0">
            {employees.length === 0 ? (
              <p className="p-4 text-sm text-[var(--muted-foreground)]">No data</p>
            ) : (
              <div className="divide-y divide-[var(--border)]">
                {employees.map((emp, i) => (
                  <div key={emp.employeeId} className="flex items-center justify-between px-4 py-3">
                    <div className="flex items-center gap-3">
                      <span className="flex h-6 w-6 items-center justify-center rounded-full bg-[var(--muted)] text-xs font-medium">
                        {i + 1}
                      </span>
                      <div>
                        <p className="text-sm font-medium">{emp.firstName} {emp.lastName}</p>
                        <p className="text-xs text-[var(--muted-foreground)]">{emp.transactionCount} sales</p>
                      </div>
                    </div>
                    <span className="text-sm font-semibold text-[var(--primary)]">
                      {fmt(emp.grossSalesAmount)}
                    </span>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Stock Alerts */}
      {stockAlerts.length > 0 && (
        <Card>
          <CardHeader className="flex flex-row items-center gap-2">
            <AlertTriangle className="h-4 w-4 text-amber-500" />
            <CardTitle>Low Stock Alerts</CardTitle>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>SKU</TableHead>
                  <TableHead>Product</TableHead>
                  <TableHead>Qty on Hand</TableHead>
                  <TableHead>Value</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {stockAlerts.map(s => (
                  <TableRow key={s.productVariationId}>
                    <TableCell className="font-mono text-xs">{s.sku}</TableCell>
                    <TableCell>{s.productName}</TableCell>
                    <TableCell>
                      <Badge variant={s.quantityOnHand === 0 ? 'destructive' : 'warning'}>
                        {s.quantityOnHand}
                      </Badge>
                    </TableCell>
                    <TableCell>{fmt(s.value)}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      )}
    </div>
  )
}
