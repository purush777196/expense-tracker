import React, { useState, useEffect, useCallback } from 'react';
import {
  Plus,
  Search,
  Filter,
  ArrowUpRight,
  ArrowDownLeft,
  Edit2,
  Trash2,
  X,
  Receipt,
  RotateCcw,
} from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { useToast } from '../../hooks/useToast';
import { Button } from '../../components/common/Button';
import { Badge } from '../../components/common/Badge';
import { Card, CardContent } from '../../components/common/Card';
import { EmptyState } from '../../components/common/EmptyState';
import { TableRowSkeleton } from '../../components/common/SkeletonLoader';
import { AddEditTransactionModal } from '../../components/transactions/AddEditTransactionModal';
import { DeleteConfirmModal } from '../../components/transactions/DeleteConfirmModal';
import { transactionService } from '../../services/transactionService';
import { categoryService } from '../../services/categoryService';
import { formatCurrency } from '../../utils/currencyFormatter';
import { formatDate } from '../../utils/dateFormatter';

export const TransactionsPage = () => {
  const { user } = useAuth();
  const { success, error: toastError } = useToast();
  const currency = user?.preferredCurrency || 'USD';

  // Data state
  const [transactions, setTransactions] = useState([]);
  const [categories, setCategories] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [currentPage, setCurrentPage] = useState(0);
  const pageSize = 10;

  // Filters state
  const [searchQuery, setSearchQuery] = useState('');
  const [typeFilter, setTypeFilter] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [sortBy, setSortBy] = useState('transactionDate');
  const [sortDirection, setSortDirection] = useState('DESC');

  // Modal state
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedTxForEdit, setSelectedTxForEdit] = useState(null);
  const [selectedTxForDelete, setSelectedTxForDelete] = useState(null);
  const [isDeleting, setIsDeleting] = useState(false);

  // Fetch categories
  const fetchCategories = useCallback(async () => {
    try {
      const res = await categoryService.getCategories();
      const list = res?.data || res || [];
      setCategories(Array.isArray(list) ? list : []);
    } catch {
      // Ignore initial category fetch error
    }
  }, []);

  // Fetch transactions with current filters
  const fetchTransactions = useCallback(async () => {
    setIsLoading(true);
    try {
      const params = {
        page: currentPage,
        size: pageSize,
        sortBy,
        sortDirection,
      };

      if (searchQuery.trim()) params.query = searchQuery.trim();
      if (typeFilter) params.type = typeFilter;
      if (categoryFilter) params.categoryId = categoryFilter;
      if (startDate) params.startDate = new Date(startDate + 'T00:00:00Z').toISOString();
      if (endDate) params.endDate = new Date(endDate + 'T23:59:59Z').toISOString();

      const res = await transactionService.getTransactions(params);
      const data = res?.data || res;

      if (data && data.content) {
        setTransactions(data.content);
        setTotalElements(data.totalElements || 0);
        setTotalPages(data.totalPages || 1);
      } else {
        setTransactions([]);
        setTotalElements(0);
        setTotalPages(1);
      }
    } catch (err) {
      const msg = err?.response?.data?.message || err?.message || 'Failed to load transactions';
      toastError(msg);
      setTransactions([]);
    } finally {
      setIsLoading(false);
    }
  }, [currentPage, sortBy, sortDirection, searchQuery, typeFilter, categoryFilter, startDate, endDate, toastError]);

  useEffect(() => {
    fetchCategories();
  }, [fetchCategories]);

  useEffect(() => {
    fetchTransactions();
  }, [fetchTransactions]);

  const handleClearFilters = () => {
    setSearchQuery('');
    setTypeFilter('');
    setCategoryFilter('');
    setStartDate('');
    setEndDate('');
    setSortBy('transactionDate');
    setSortDirection('DESC');
    setCurrentPage(0);
  };

  const hasActiveFilters = Boolean(
    searchQuery || typeFilter || categoryFilter || startDate || endDate
  );

  const handleOpenAddModal = () => {
    setSelectedTxForEdit(null);
    setIsModalOpen(true);
  };

  const handleOpenEditModal = (tx) => {
    setSelectedTxForEdit(tx);
    setIsModalOpen(true);
  };

  const handleDeleteConfirm = async () => {
    if (!selectedTxForDelete) return;

    setIsDeleting(true);
    try {
      await transactionService.deleteTransaction(selectedTxForDelete.id);
      success('Transaction deleted successfully');
      setSelectedTxForDelete(null);
      fetchTransactions();
    } catch (err) {
      const msg = err?.response?.data?.message || err?.message || 'Failed to delete transaction';
      toastError(msg);
    } finally {
      setIsDeleting(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-xl sm:text-2xl font-bold tracking-tight text-slate-900">
            Transactions Ledger
          </h2>
          <p className="text-xs sm:text-sm text-slate-500 mt-1">
            Search, filter, track and manage your personal income and expense records.
          </p>
        </div>

        <Button
          variant="primary"
          size="md"
          leftIcon={<Plus className="w-4 h-4" />}
          onClick={handleOpenAddModal}
        >
          Add Transaction
        </Button>
      </div>

      {/* Filter and Search Bar Card */}
      <Card className="p-4 space-y-3">
        <div className="grid grid-cols-1 md:grid-cols-12 gap-3 items-center">
          {/* Search Box */}
          <div className="md:col-span-4 relative">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-3 pointer-events-none" />
            <input
              type="text"
              placeholder="Search description or merchant..."
              value={searchQuery}
              onChange={(e) => {
                setSearchQuery(e.target.value);
                setCurrentPage(0);
              }}
              className="w-full pl-9 pr-3 py-2 text-xs rounded-lg border border-slate-300 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
            />
            {searchQuery && (
              <button
                type="button"
                onClick={() => setSearchQuery('')}
                className="absolute right-2.5 top-2.5 text-slate-400 hover:text-slate-600"
              >
                <X className="w-3.5 h-3.5" />
              </button>
            )}
          </div>

          {/* Type Filter Pills */}
          <div className="md:col-span-3 flex rounded-lg p-1 bg-slate-100 text-xs">
            <button
              type="button"
              onClick={() => {
                setTypeFilter('');
                setCurrentPage(0);
              }}
              className={`flex-1 py-1 px-2 rounded-md font-medium transition-all ${
                !typeFilter
                  ? 'bg-white text-slate-900 shadow-sm font-semibold'
                  : 'text-slate-600 hover:text-slate-900'
              }`}
            >
              All Types
            </button>
            <button
              type="button"
              onClick={() => {
                setTypeFilter('EXPENSE');
                setCurrentPage(0);
              }}
              className={`flex-1 py-1 px-2 rounded-md font-medium transition-all ${
                typeFilter === 'EXPENSE'
                  ? 'bg-rose-600 text-white shadow-sm font-semibold'
                  : 'text-slate-600 hover:text-slate-900'
              }`}
            >
              Expenses
            </button>
            <button
              type="button"
              onClick={() => {
                setTypeFilter('INCOME');
                setCurrentPage(0);
              }}
              className={`flex-1 py-1 px-2 rounded-md font-medium transition-all ${
                typeFilter === 'INCOME'
                  ? 'bg-emerald-600 text-white shadow-sm font-semibold'
                  : 'text-slate-600 hover:text-slate-900'
              }`}
            >
              Income
            </button>
          </div>

          {/* Category Filter */}
          <div className="md:col-span-3">
            <select
              value={categoryFilter}
              onChange={(e) => {
                setCategoryFilter(e.target.value);
                setCurrentPage(0);
              }}
              className="w-full py-2 px-3 text-xs rounded-lg border border-slate-300 focus:outline-none focus:ring-2 focus:ring-primary-500 bg-white"
            >
              <option value="">All Categories</option>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name} ({c.type})
                </option>
              ))}
            </select>
          </div>

          {/* Sort Control */}
          <div className="md:col-span-2">
            <select
              value={`${sortBy}-${sortDirection}`}
              onChange={(e) => {
                const [sb, sd] = e.target.value.split('-');
                setSortBy(sb);
                setSortDirection(sd);
                setCurrentPage(0);
              }}
              className="w-full py-2 px-3 text-xs rounded-lg border border-slate-300 focus:outline-none focus:ring-2 focus:ring-primary-500 bg-white"
            >
              <option value="transactionDate-DESC">Date: Newest First</option>
              <option value="transactionDate-ASC">Date: Oldest First</option>
              <option value="amount-DESC">Amount: Highest First</option>
              <option value="amount-ASC">Amount: Lowest First</option>
            </select>
          </div>
        </div>

        {/* Date Range & Clear Filters */}
        <div className="flex flex-wrap items-center justify-between gap-3 pt-2 border-t border-slate-100 text-xs">
          <div className="flex items-center gap-2">
            <span className="text-slate-500 font-medium flex items-center gap-1">
              <Filter className="w-3.5 h-3.5" /> Date Range:
            </span>
            <input
              type="date"
              value={startDate}
              onChange={(e) => {
                setStartDate(e.target.value);
                setCurrentPage(0);
              }}
              className="px-2 py-1 rounded-md border border-slate-300 text-xs"
            />
            <span className="text-slate-400">to</span>
            <input
              type="date"
              value={endDate}
              onChange={(e) => {
                setEndDate(e.target.value);
                setCurrentPage(0);
              }}
              className="px-2 py-1 rounded-md border border-slate-300 text-xs"
            />
          </div>

          {hasActiveFilters && (
            <button
              type="button"
              onClick={handleClearFilters}
              className="inline-flex items-center gap-1 text-primary-600 hover:text-primary-700 font-semibold"
            >
              <RotateCcw className="w-3.5 h-3.5" />
              <span>Reset Filters</span>
            </button>
          )}
        </div>
      </Card>

      {/* Transactions Table Card */}
      <Card className="overflow-hidden">
        <CardContent className="p-0">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50/80 border-b border-slate-100 text-xs font-semibold text-slate-500 uppercase tracking-wider">
                <tr>
                  <th scope="col" className="px-6 py-3.5">
                    Transaction
                  </th>
                  <th scope="col" className="px-6 py-3.5">
                    Category
                  </th>
                  <th scope="col" className="px-6 py-3.5">
                    Method
                  </th>
                  <th scope="col" className="px-6 py-3.5">
                    Date
                  </th>
                  <th scope="col" className="px-6 py-3.5 text-right">
                    Amount
                  </th>
                  <th scope="col" className="px-6 py-3.5 text-right">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {isLoading ? (
                  Array.from({ length: 5 }).map((_, idx) => (
                    <TableRowSkeleton key={idx} columns={6} />
                  ))
                ) : transactions.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="py-12">
                      <EmptyState
                        icon={Receipt}
                        title={hasActiveFilters ? 'No matching transactions' : 'No transactions recorded yet'}
                        description={
                          hasActiveFilters
                            ? 'Try adjusting your search query, type, or date range filters.'
                            : 'Start tracking your financial progress by adding your first transaction.'
                        }
                        action={
                          !hasActiveFilters && (
                            <Button
                              variant="primary"
                              size="sm"
                              leftIcon={<Plus className="w-4 h-4" />}
                              onClick={handleOpenAddModal}
                            >
                              Add First Transaction
                            </Button>
                          )
                        }
                      />
                    </td>
                  </tr>
                ) : (
                  transactions.map((tx) => {
                    const isIncome = tx.type === 'INCOME';
                    return (
                      <tr key={tx.id} className="hover:bg-slate-50/60 transition-colors">
                        {/* Transaction Name & Merchant */}
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="flex items-center gap-3">
                            <div
                              className={`p-2 rounded-lg shrink-0 ${
                                isIncome
                                  ? 'bg-emerald-50 text-emerald-600'
                                  : 'bg-rose-50 text-rose-600'
                              }`}
                            >
                              {isIncome ? (
                                <ArrowDownLeft className="w-4 h-4" />
                              ) : (
                                <ArrowUpRight className="w-4 h-4" />
                              )}
                            </div>
                            <div>
                              <p className="font-semibold text-slate-900 text-sm">
                                {tx.description}
                              </p>
                              {tx.merchant && (
                                <p className="text-xs text-slate-400">{tx.merchant}</p>
                              )}
                            </div>
                          </div>
                        </td>

                        {/* Category Badge */}
                        <td className="px-6 py-4 whitespace-nowrap">
                          <Badge variant={isIncome ? 'income' : 'expense'}>
                            {tx.category?.name || 'General'}
                          </Badge>
                        </td>

                        {/* Payment Method */}
                        <td className="px-6 py-4 whitespace-nowrap text-xs text-slate-500 capitalize">
                          {tx.paymentMethod ? tx.paymentMethod.replace('_', ' ').toLowerCase() : 'Card'}
                        </td>

                        {/* Date */}
                        <td className="px-6 py-4 whitespace-nowrap text-xs text-slate-500">
                          {formatDate(tx.transactionDate)}
                        </td>

                        {/* Amount */}
                        <td
                          className={`px-6 py-4 whitespace-nowrap text-right font-semibold text-sm ${
                            isIncome ? 'text-emerald-600' : 'text-slate-900'
                          }`}
                        >
                          {isIncome ? '+' : '-'}
                          {formatCurrency(tx.amount, currency)}
                        </td>

                        {/* Actions */}
                        <td className="px-6 py-4 whitespace-nowrap text-right text-xs">
                          <div className="flex items-center justify-end gap-1.5">
                            <button
                              type="button"
                              onClick={() => handleOpenEditModal(tx)}
                              className="p-1.5 text-slate-400 hover:text-primary-600 hover:bg-primary-50 rounded-lg transition-colors"
                              title="Edit transaction"
                            >
                              <Edit2 className="w-3.5 h-3.5" />
                            </button>
                            <button
                              type="button"
                              onClick={() => setSelectedTxForDelete(tx)}
                              className="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition-colors"
                              title="Delete transaction"
                            >
                              <Trash2 className="w-3.5 h-3.5" />
                            </button>
                          </div>
                        </td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </table>
          </div>

          {/* Pagination Controls */}
          {!isLoading && transactions.length > 0 && (
            <div className="px-6 py-3.5 border-t border-slate-100 flex items-center justify-between text-xs text-slate-500 bg-slate-50/40">
              <div>
                Showing <span className="font-semibold text-slate-700">{currentPage * pageSize + 1}</span> to{' '}
                <span className="font-semibold text-slate-700">
                  {Math.min((currentPage + 1) * pageSize, totalElements)}
                </span>{' '}
                of <span className="font-semibold text-slate-700">{totalElements}</span> records
              </div>

              <div className="flex items-center gap-1.5">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setCurrentPage((p) => Math.max(p - 1, 0))}
                  disabled={currentPage === 0}
                >
                  Previous
                </Button>
                <span className="px-2 text-slate-600">
                  Page {currentPage + 1} of {totalPages}
                </span>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setCurrentPage((p) => Math.min(p + 1, totalPages - 1))}
                  disabled={currentPage >= totalPages - 1}
                >
                  Next
                </Button>
              </div>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Add / Edit Transaction Modal */}
      <AddEditTransactionModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSuccess={fetchTransactions}
        initialData={selectedTxForEdit}
        categories={categories}
        currency={currency}
      />

      {/* Delete Confirmation Modal */}
      <DeleteConfirmModal
        isOpen={Boolean(selectedTxForDelete)}
        onClose={() => setSelectedTxForDelete(null)}
        onConfirm={handleDeleteConfirm}
        transaction={selectedTxForDelete}
        isDeleting={isDeleting}
        currency={currency}
      />
    </div>
  );
};
