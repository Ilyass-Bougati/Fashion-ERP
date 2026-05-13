'use client'

import {
  BarChart as RechartsBarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from 'recharts'

interface BarChartProps {
  data: Array<Record<string, string | number>>
  xKey: string
  yKey: string
  color?: string
  yTickFormatter?: (value: number) => string
  tooltipFormatter?: (value: number) => [string, string]
}

export function BarChart({
  data,
  xKey,
  yKey,
  color = '#6366f1',
  yTickFormatter = (v) => `$${(v / 1000).toFixed(0)}k`,
  tooltipFormatter = (value) => [`$${Number(value).toLocaleString()}`, 'Revenue'],
}: BarChartProps) {
  return (
    <ResponsiveContainer width="100%" height={300}>
      <RechartsBarChart data={data} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
        <CartesianGrid strokeDasharray="3 3" stroke="#27272a" />
        <XAxis
          dataKey={xKey}
          tick={{ fill: '#a1a1aa', fontSize: 12 }}
          axisLine={{ stroke: '#27272a' }}
          tickLine={false}
        />
        <YAxis
          tick={{ fill: '#a1a1aa', fontSize: 12 }}
          axisLine={false}
          tickLine={false}
          tickFormatter={yTickFormatter}
        />
        <Tooltip
          contentStyle={{
            backgroundColor: '#18181b',
            border: '1px solid #27272a',
            borderRadius: '8px',
            color: '#fafafa',
          }}
          formatter={(value) => tooltipFormatter(Number(value))}
        />
        <Bar dataKey={yKey} fill={color} radius={[4, 4, 0, 0]} />
      </RechartsBarChart>
    </ResponsiveContainer>
  )
}
