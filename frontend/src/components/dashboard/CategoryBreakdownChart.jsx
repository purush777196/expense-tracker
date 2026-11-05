import React from 'react';
import {
  PieChart,
  Pie,
  Cell,
  Tooltip,
  ResponsiveContainer,
  Legend,
} from 'recharts';
import { Card, CardHeader, CardContent } from '../common/Card';
import { formatCurrency } from '../../utils/currencyFormatter';

const COLORS = [
  '#4f46e5', // Primary indigo
  '#06b6d4', // Cyan
  '#f59e0b', // Amber
  '#10b981', // Emerald
  '#ec4899', // Pink
  '#8b5cf6', // Purple
  '#f97316', // Orange
  '#64748b', // Slate
];

const CustomTooltip = ({ active, payload, currency = 'USD' }) => {
  if (active && payload && payload.length) {
    const data = payload[0];
    return (
      <div className="bg-slate-900 text-white p-2.5 rounded-lg shadow-xl text-xs space-y-1 border border-slate-800">
        <p className="font-semibold">{data.name}</p>
        <p className="text-slate-300">
          Amount: <span className="font-semibold text-white">{formatCurrency(data.value, currency)}</span>
        </p>
      </div>
    );
  }
  return null;
};

export const CategoryBreakdownChart = ({ data = [], currency = 'USD' }) => {
  return (
    <Card className="col-span-full lg:col-span-4">
      <CardHeader
        title="Expense Allocation"
        subtitle="Spending categorized by department"
      />
      <CardContent>
        {data.length === 0 ? (
          <div className="h-72 flex items-center justify-center text-slate-400 text-sm">
            No category spending recorded
          </div>
        ) : (
          <div className="h-72 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={data}
                  cx="50%"
                  cy="45%"
                  innerRadius={55}
                  outerRadius={85}
                  paddingAngle={3}
                  dataKey="amount"
                  nameKey="category"
                >
                  {data.map((entry, index) => (
                    <Cell
                      key={`cell-${index}`}
                      fill={COLORS[index % COLORS.length]}
                      stroke="#ffffff"
                      strokeWidth={2}
                    />
                  ))}
                </Pie>
                <Tooltip content={<CustomTooltip currency={currency} />} />
                <Legend
                  verticalAlign="bottom"
                  align="center"
                  iconType="circle"
                  wrapperStyle={{ paddingTop: 10, fontSize: 11 }}
                />
              </PieChart>
            </ResponsiveContainer>
          </div>
        )}
      </CardContent>
    </Card>
  );
};

