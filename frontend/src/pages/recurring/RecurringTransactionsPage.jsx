import React, { useState, useEffect, useCallback } from 'react';
import {
  Repeat,
  Plus,
  Play,
  Pause,
  XCircle,
  Edit2,
  Trash2,
  Calendar,
  Clock,
  ArrowDownLeft,
  CheckCircle2,
  RefreshCw,
  Zap,
} from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { useToast } from '../../hooks/useToast';
import { Button } from '../../components/common/Button';
import { Badge } from '../../components/common/Badge';
import { Card } from '../../components/common/Card';
import { StatCard } from '../../components/common/StatCard';
import { EmptyState } from '../../components/common/EmptyState';
import { TableRowSkeleton } from '../../components/common/SkeletonLoader';
import { AddEditRecurringModal } from '../../components/recurring/AddEditRecurringModal';
import { DeleteConfirmModal } from '../../components/transactions/DeleteConfirmModal';
import { recurringService } from '../../services/recurringService';
import { categoryService } from '../../services/categoryService';
import { formatCurrency } from '../../utils/currencyFormatter';

export const RecurringTransactionsPage = () => {
  const { user } = useAuth();
  const { success, error: toastError } = useToast();
  const currency = user?.preferredCurrency || 'USD';

  const [isLoading, setIsLoading] = useState(true);
  const [isProcessing, setIsProcessing] = useState(false);
  const [recurringList, setRecurringList] = useState([]);
  const [upcomingList, setUpcomingList] = useState([]);
  const [categories, setCategories] = useState([]);
  const [statusFilter, setStatusFilter] = useState('');

  // Modals
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedRecurringForEdit, setSelectedRecurringForEdit] = useState(null);
  const [selectedRecurringForDelete, setSelectedRecurringForDelete] = useState(null);
  const [isDeleting, setIsDeleting] = useState(false);

  const fetchRecurringData = useCallback(async () => {
    setIsLoading(true);
    try {
      const params = {};
      if (statusFilter) params.status = statusFilter;

      const [recurringRes, upcomingRes, catRes] = await Promise.all([
        recurringService.getRecurringTransactions(params),
        recurringService.getUpcomingRecurring(30).catch(() => []),
        categoryService.getCategories().catch(() => []),
      ]);

      const list = recurringRes?.data || recurringRes || [];
      setRecurringList(Array.isArray(list) ? list : []);

      const upcoming = upcomingRes?.data || upcomingRes || [];
      setUpcomingList(Array.isArray(upcoming) ? upcoming : []);

      const cats = catRes?.data || catRes || [];
      setCategories(Array.isArray(cats) ? cats : []);
    } catch (err) {
      const msg = err?.response?.data?.message || err?.message || 'Failed to load recurring transactions';
      toastError(msg);
    } finally {
      setIsLoading(false);
    }
  }, [statusFilter, toastError]);

  useEffect(() => {
    fetchRecurringData();
  }, [fetchRecurringData]);

  const handleOpenCreateModal = () => {
    setSelectedRecurringForEdit(null);
    setIsModalOpen(true);
  };

  const handleOpenEditModal = (item) => {
    setSelectedRecurringForEdit(item);
    setIsModalOpen(true);
  };

  const handleSaveRecurring = async (data, id) => {
    try {
      if (id) {
        await recurringService.updateRecurringTransaction(id, data);
        success('Recurring transaction updated successfully');
      } else {
        await recurringService.createRecurringTransaction(data);
        success('Recurring transaction scheduled successfully');
      }
      fetchRecurringData();
    } catch (err) {
      const msg = err?.response?.data?.message || err?.message || 'Failed to save recurring transaction';
      toastError(msg);
      throw err;
    }
  };

  const handlePause = async (id) => {
    try {
      await recurringService.pauseRecurringTransaction(id);
      success('Recurring schedule paused');
      fetchRecurringData();
    } catch (err) {
      const msg = err?.response?.data?.message || err?.message || 'Failed to pause';
      toastError(msg);
    }
  };

  const handleResume = async (id) => {
    try {
      await recurringService.resumeRecurringTransaction(id);
      success('Recurring schedule resumed');
      fetchRecurringData();
    } catch (err) {
      const msg = err?.response?.data?.message || err?.message || 'Failed to resume';
      toastError(msg);
    }
  };

  const handleCancel = async (id) => {
    try {
      await recurringService.cancelRecurringTransaction(id);
      success('Recurring schedule cancelled');
      fetchRecurringData();
    } catch (err) {
      const msg = err?.response?.data?.message || err?.message || 'Failed to cancel';
      toastError(msg);
    }
  };

  const handleDelete = async () => {
    if (!selectedRecurringForDelete) return;
    setIsDeleting(true);
    try {
      await recurringService.deleteRecurringTransaction(selectedRecurringForDelete.id);
      success('Recurring schedule deleted');
      setSelectedRecurringForDelete(null);
      fetchRecurringData();
    } catch (err) {
      const msg = err?.response?.data?.message || err?.message || 'Failed to delete schedule';
      toastError(msg);
    } finally {
      setIsDeleting(false);
    }
  };

  const handleTriggerDue = async () => {
    setIsProcessing(true);
    try {
      const res = await recurringService.triggerProcessing();
      const count = res?.data?.generatedTransactionsCount || res?.generatedTransactionsCount || 0;
      success(`Processing completed. Generated ${count} transactions.`);
      fetchRecurringData();
    } catch (err) {
      const msg = err?.response?.data?.message || err?.message || 'Failed to process recurring transactions';
      toastError(msg);
    } finally {
      setIsProcessing(false);
    }
  };

  // Calculations for KPIs
  const activeSchedules = recurringList.filter((r) => r.status === 'ACTIVE');
  const monthlyExpenses = activeSchedules
    .filter((r) => r.type === 'EXPENSE')
    .reduce((sum, r) => {
      let multiplier = 1;
      if (r.frequency === 'DAILY') multiplier = 30 / (r.intervalValue || 1);
      else if (r.frequency === 'WEEKLY') multiplier = 4.33 / (r.intervalValue || 1);
      else if (r.frequency === 'YEARLY') multiplier = (1 / 12) / (r.intervalValue || 1);
      else multiplier = 1 / (r.intervalValue || 1);
      return sum + (parseFloat(r.amount) || 0) * multiplier;
    }, 0);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-xl sm:text-2xl font-bold tracking-tight text-slate-900">
            Recurring Transactions
          </h2>
          <p className="text-xs sm:text-sm text-slate-500 mt-1">
            Automate periodic bills, subscriptions, wages, and scheduled ledger postings.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <Button
            variant="outline"
            size="sm"
            onClick={handleTriggerDue}
            isLoading={isProcessing}
            leftIcon={<Zap className="w-4 h-4 text-amber-500" />}
          >
            Process Due Now
          </Button>
          <Button
            variant="primary"
            size="sm"
            onClick={handleOpenCreateModal}
            leftIcon={<Plus className="w-4 h-4" />}
          >
            New Schedule
          </Button>
        </div>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Active Schedules"
          value={activeSchedules.length}
          subtitle="Currently running schedules"
          icon={Repeat}
          iconColor="text-indigo-600"
          iconBg="bg-indigo-50"
        />
        <StatCard
          title="Est. Monthly Outflow"
          value={formatCurrency(monthlyExpenses, currency)}
          subtitle="Projected recurring expenses"
          icon={ArrowDownLeft}
          iconColor="text-rose-600"
          iconBg="bg-rose-50"
        />
        <StatCard
          title="Due In 30 Days"
          value={upcomingList.length}
          subtitle="Upcoming payments/receipts"
          icon={Clock}
          iconColor="text-amber-600"
          iconBg="bg-amber-50"
        />
        <StatCard
          title="Total Registered"
          value={recurringList.length}
          subtitle="All recurring records"
          icon={CheckCircle2}
          iconColor="text-emerald-600"
          iconBg="bg-emerald-50"
        />
      </div>

      {/* Upcoming Bills Forecast Section */}
      {upcomingList.length > 0 && (
        <Card className="p-5">
          <div className="flex items-center justify-between mb-3">
            <h3 className="text-sm font-bold text-slate-900 flex items-center gap-2">
              <Clock className="w-4 h-4 text-indigo-600" />
              Upcoming Bills & Schedules (Next 30 Days)
            </h3>
            <span className="text-xs text-slate-500">
              {upcomingList.length} scheduled
            </span>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
            {upcomingList.slice(0, 6).map((item) => {
              const isExpense = item.type === 'EXPENSE';
              return (
                <div
                  key={item.recurringId}
                  className="p-3 rounded-xl border border-slate-100 bg-slate-50/50 flex items-center justify-between"
                >
                  <div>
                    <p className="text-xs font-semibold text-slate-900">
                      {item.description}
                    </p>
                    <p className="text-[11px] text-slate-500">
                      Due: {item.dueDate} ({item.daysUntilDue === 0 ? 'Today' : `${item.daysUntilDue}d away`})
                    </p>
                  </div>
                  <div className="text-right">
                    <span
                      className={`text-xs font-bold ${
                        isExpense ? 'text-slate-900' : 'text-emerald-600'
                      }`}
                    >
                      {isExpense ? '-' : '+'}
                      {formatCurrency(item.amount, currency)}
                    </span>
                    <p className="text-[10px] text-slate-400 capitalize">
                      {item.frequency.toLowerCase()}
                    </p>
                  </div>
                </div>
              );
            })}
          </div>
        </Card>
      )}

      {/* Filter Toolbar */}
      <div className="flex items-center justify-between gap-3 bg-white p-3 rounded-xl border border-slate-100">
        <div className="flex items-center gap-2">
          <span className="text-xs font-medium text-slate-500">Filter Status:</span>
          {['', 'ACTIVE', 'PAUSED', 'COMPLETED', 'CANCELLED'].map((st) => (
            <button
              key={st}
              type="button"
              onClick={() => setStatusFilter(st)}
              className={`px-2.5 py-1 text-xs rounded-lg font-medium transition-colors ${
                statusFilter === st
                  ? 'bg-slate-900 text-white'
                  : 'text-slate-600 hover:bg-slate-100'
              }`}
            >
              {st || 'All'}
            </button>
          ))}
        </div>

        <Button
          variant="outline"
          size="sm"
          onClick={fetchRecurringData}
          leftIcon={<RefreshCw className="w-3.5 h-3.5" />}
        >
          Refresh
        </Button>
      </div>

      {/* Main Table */}
      <Card className="overflow-hidden">
        {isLoading ? (
          <table className="w-full text-left border-collapse">
            <tbody className="divide-y divide-slate-100">
              <TableRowSkeleton columns={6} />
              <TableRowSkeleton columns={6} />
              <TableRowSkeleton columns={6} />
            </tbody>
          </table>
        ) : recurringList.length === 0 ? (
          <EmptyState
            icon={Repeat}
            title="No Recurring Transactions Found"
            description="Automate repetitive expenses like subscriptions, utilities, rent, and monthly income."
            action={
              <Button
                variant="primary"
                size="sm"
                onClick={handleOpenCreateModal}
                leftIcon={<Plus className="w-4 h-4" />}
              >
                Schedule First Transaction
              </Button>
            }
            className="py-16"
          />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse text-xs">
              <thead>
                <tr className="border-b border-slate-200 bg-slate-50/75 text-slate-600 font-semibold uppercase tracking-wider text-[11px]">
                  <th className="py-3 px-4">Description / Merchant</th>
                  <th className="py-3 px-4">Category</th>
                  <th className="py-3 px-4">Frequency</th>
                  <th className="py-3 px-4">Amount</th>
                  <th className="py-3 px-4">Next Due Date</th>
                  <th className="py-3 px-4">Status</th>
                  <th className="py-3 px-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-slate-700">
                {recurringList.map((item) => {
                  const isExpense = item.type === 'EXPENSE';
                  let statusBadge = 'primary';
                  if (item.status === 'ACTIVE') statusBadge = 'success';
                  else if (item.status === 'PAUSED') statusBadge = 'warning';
                  else if (item.status === 'COMPLETED') statusBadge = 'info';
                  else if (item.status === 'CANCELLED') statusBadge = 'neutral';

                  return (
                    <tr key={item.id} className="hover:bg-slate-50/60 transition-colors">
                      <td className="py-3 px-4">
                        <div className="font-semibold text-slate-900">{item.description}</div>
                        {item.merchant && (
                          <div className="text-[11px] text-slate-400">{item.merchant}</div>
                        )}
                      </td>

                      <td className="py-3 px-4">
                        <span
                          className="inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full text-[11px] font-medium"
                          style={{
                            backgroundColor: `${item.category?.colorHex || '#6366F1'}15`,
                            color: item.category?.colorHex || '#6366F1',
                          }}
                        >
                          <span
                            className="w-1.5 h-1.5 rounded-full"
                            style={{ backgroundColor: item.category?.colorHex || '#6366F1' }}
                          />
                          {item.category?.name || 'Uncategorized'}
                        </span>
                      </td>

                      <td className="py-3 px-4 capitalize">
                        {item.intervalValue > 1 ? `Every ${item.intervalValue} ` : ''}
                        {item.frequency?.toLowerCase()}
                      </td>

                      <td className="py-3 px-4 font-bold">
                        <span className={isExpense ? 'text-slate-900' : 'text-emerald-600'}>
                          {isExpense ? '-' : '+'}
                          {formatCurrency(item.amount, currency)}
                        </span>
                      </td>

                      <td className="py-3 px-4">
                        <div className="flex items-center gap-1 text-slate-600">
                          <Calendar className="w-3.5 h-3.5 text-slate-400" />
                          {item.nextOccurrenceDate || 'None'}
                        </div>
                      </td>

                      <td className="py-3 px-4">
                        <Badge variant={statusBadge} size="sm">
                          {item.status}
                        </Badge>
                      </td>

                      <td className="py-3 px-4 text-right">
                        <div className="flex items-center justify-end gap-1">
                          {item.status === 'ACTIVE' && (
                            <button
                              type="button"
                              onClick={() => handlePause(item.id)}
                              className="p-1.5 text-amber-600 hover:bg-amber-50 rounded transition-colors"
                              title="Pause Schedule"
                            >
                              <Pause className="w-3.5 h-3.5" />
                            </button>
                          )}
                          {item.status === 'PAUSED' && (
                            <button
                              type="button"
                              onClick={() => handleResume(item.id)}
                              className="p-1.5 text-emerald-600 hover:bg-emerald-50 rounded transition-colors"
                              title="Resume Schedule"
                            >
                              <Play className="w-3.5 h-3.5" />
                            </button>
                          )}
                          {item.status !== 'CANCELLED' && item.status !== 'COMPLETED' && (
                            <button
                              type="button"
                              onClick={() => handleCancel(item.id)}
                              className="p-1.5 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded transition-colors"
                              title="Cancel Schedule"
                            >
                              <XCircle className="w-3.5 h-3.5" />
                            </button>
                          )}
                          <button
                            type="button"
                            onClick={() => handleOpenEditModal(item)}
                            className="p-1.5 text-slate-400 hover:text-slate-700 hover:bg-slate-100 rounded transition-colors"
                            title="Edit"
                          >
                            <Edit2 className="w-3.5 h-3.5" />
                          </button>
                          <button
                            type="button"
                            onClick={() => setSelectedRecurringForDelete(item)}
                            className="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded transition-colors"
                            title="Delete"
                          >
                            <Trash2 className="w-3.5 h-3.5" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </Card>

      {/* Add / Edit Modal */}
      <AddEditRecurringModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSave={handleSaveRecurring}
        recurring={selectedRecurringForEdit}
        categories={categories}
      />

      {/* Delete Confirmation Modal */}
      <DeleteConfirmModal
        isOpen={!!selectedRecurringForDelete}
        onClose={() => setSelectedRecurringForDelete(null)}
        onConfirm={handleDelete}
        title="Delete Recurring Schedule"
        message={`Are you sure you want to delete recurring schedule "${selectedRecurringForDelete?.description}"? Past generated transactions will remain in your transaction history.`}
        isDeleting={isDeleting}
      />
    </div>
  );
};
export default RecurringTransactionsPage;
