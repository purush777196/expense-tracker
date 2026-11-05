import React, { useState, useEffect } from 'react';
import { ArrowDownLeft, ArrowUpRight, DollarSign } from 'lucide-react';
import { Modal } from '../common/Modal';
import { Button } from '../common/Button';
import { Input } from '../common/Input';
import { Select } from '../common/Select';
import { useToast } from '../../hooks/useToast';
import { transactionService } from '../../services/transactionService';
import { isPositiveNumber } from '../../utils/validators';

const PAYMENT_METHODS = [
  { value: 'CARD', label: 'Credit / Debit Card' },
  { value: 'BANK_TRANSFER', label: 'Bank Transfer' },
  { value: 'UPI', label: 'UPI / Mobile Payment' },
  { value: 'CASH', label: 'Cash' },
  { value: 'OTHER', label: 'Other' },
];

export const AddEditTransactionModal = ({
  isOpen,
  onClose,
  onSuccess,
  initialData = null,
  categories = [],
  currency = 'USD',
}) => {
  const isEditing = Boolean(initialData);
  const { success, error: toastError } = useToast();

  const [formData, setFormData] = useState({
    type: 'EXPENSE',
    amount: '',
    categoryId: '',
    transactionDate: new Date().toISOString().split('T')[0],
    description: '',
    merchant: '',
    paymentMethod: 'CARD',
    notes: '',
    isRecurring: false,
  });

  const [errors, setErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Synchronize state when opening or when initialData changes
  useEffect(() => {
    if (initialData) {
      setFormData({
        type: initialData.type || 'EXPENSE',
        amount: initialData.amount ? String(initialData.amount) : '',
        categoryId: initialData.category?.id || initialData.categoryId || '',
        transactionDate: initialData.transactionDate
          ? new Date(initialData.transactionDate).toISOString().split('T')[0]
          : new Date().toISOString().split('T')[0],
        description: initialData.description || '',
        merchant: initialData.merchant || '',
        paymentMethod: initialData.paymentMethod || 'CARD',
        notes: initialData.notes || '',
        isRecurring: Boolean(initialData.isRecurring),
      });
    } else {
      setFormData({
        type: 'EXPENSE',
        amount: '',
        categoryId: '',
        transactionDate: new Date().toISOString().split('T')[0],
        description: '',
        merchant: '',
        paymentMethod: 'CARD',
        notes: '',
        isRecurring: false,
      });
    }
    setErrors({});
  }, [initialData, isOpen]);

  // Filter categories matching current transaction type
  const availableCategories = categories.filter((c) => c.type === formData.type);

  // Automatically select first category if current categoryId is not in the filtered list
  useEffect(() => {
    if (availableCategories.length > 0) {
      const exists = availableCategories.some((c) => c.id === formData.categoryId);
      if (!exists && !isEditing) {
        setFormData((prev) => ({ ...prev, categoryId: availableCategories[0].id }));
      }
    }
  }, [formData.type, availableCategories, formData.categoryId, isEditing]);

  const handleTypeChange = (newType) => {
    setFormData((prev) => ({ ...prev, type: newType, categoryId: '' }));
    if (errors.categoryId) {
      setErrors((prev) => ({ ...prev, categoryId: '' }));
    }
  };

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
    }));
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: '' }));
    }
  };

  const validate = () => {
    const newErrors = {};

    if (!formData.amount || !isPositiveNumber(formData.amount)) {
      newErrors.amount = 'Please enter a valid amount greater than 0';
    }

    if (!formData.categoryId) {
      newErrors.categoryId = 'Please select a category';
    }

    if (!formData.description.trim()) {
      newErrors.description = 'Description is required';
    } else if (formData.description.trim().length < 2) {
      newErrors.description = 'Description must be at least 2 characters';
    }

    if (!formData.transactionDate) {
      newErrors.transactionDate = 'Transaction date is required';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;

    setIsSubmitting(true);

    const payload = {
      amount: parseFloat(formData.amount),
      type: formData.type,
      categoryId: formData.categoryId,
      transactionDate: new Date(formData.transactionDate + 'T12:00:00Z').toISOString(),
      description: formData.description.trim(),
      merchant: formData.merchant.trim() || null,
      paymentMethod: formData.paymentMethod,
      notes: formData.notes.trim() || null,
      isRecurring: formData.isRecurring,
    };

    try {
      if (isEditing) {
        await transactionService.updateTransaction(initialData.id, payload);
        success('Transaction updated successfully');
      } else {
        await transactionService.createTransaction(payload);
        success('Transaction added successfully');
      }
      onSuccess();
      onClose();
    } catch (err) {
      const msg = err?.response?.data?.message || err?.message || 'Failed to save transaction';
      toastError(msg);
    } finally {
      setIsSubmitting(false);
    }
  };

  const categoryOptions = availableCategories.map((c) => ({
    value: c.id,
    label: c.name,
  }));

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={isEditing ? 'Edit Transaction' : 'Record Transaction'}
      description="Record income or expenditure against your budget categories"
      maxWidth="max-w-lg"
    >
      <form onSubmit={handleSubmit} className="space-y-4" noValidate>
        {/* Type Toggle Tabs */}
        <div>
          <label className="block text-xs font-semibold text-slate-600 uppercase tracking-wider mb-2">
            Transaction Type
          </label>
          <div className="grid grid-cols-2 gap-2 p-1 bg-slate-100 rounded-xl">
            <button
              type="button"
              onClick={() => handleTypeChange('EXPENSE')}
              className={`flex items-center justify-center gap-2 py-2 px-3 rounded-lg text-xs font-semibold transition-all ${
                formData.type === 'EXPENSE'
                  ? 'bg-rose-600 text-white shadow-sm'
                  : 'text-slate-600 hover:text-slate-900'
              }`}
            >
              <ArrowUpRight className="w-4 h-4" />
              <span>Expense</span>
            </button>

            <button
              type="button"
              onClick={() => handleTypeChange('INCOME')}
              className={`flex items-center justify-center gap-2 py-2 px-3 rounded-lg text-xs font-semibold transition-all ${
                formData.type === 'INCOME'
                  ? 'bg-emerald-600 text-white shadow-sm'
                  : 'text-slate-600 hover:text-slate-900'
              }`}
            >
              <ArrowDownLeft className="w-4 h-4" />
              <span>Income</span>
            </button>
          </div>
        </div>

        {/* Amount & Category */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <Input
            label={`Amount (${currency})`}
            id="amount"
            name="amount"
            type="number"
            step="0.01"
            min="0.01"
            required
            placeholder="0.00"
            value={formData.amount}
            onChange={handleChange}
            error={errors.amount}
            leftIcon={<DollarSign className="w-4 h-4" />}
          />

          <Select
            label="Category"
            id="categoryId"
            name="categoryId"
            required
            value={formData.categoryId}
            onChange={handleChange}
            options={categoryOptions}
            error={errors.categoryId}
            placeholder={categoryOptions.length === 0 ? 'No categories found' : 'Select Category'}
          />
        </div>

        {/* Description & Merchant */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <Input
            label="Description"
            id="description"
            name="description"
            type="text"
            required
            placeholder="e.g. Weekly Grocery Run"
            value={formData.description}
            onChange={handleChange}
            error={errors.description}
          />

          <Input
            label="Merchant / Payee (Optional)"
            id="merchant"
            name="merchant"
            type="text"
            placeholder="e.g. Whole Foods"
            value={formData.merchant}
            onChange={handleChange}
          />
        </div>

        {/* Date & Payment Method */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <Input
            label="Date"
            id="transactionDate"
            name="transactionDate"
            type="date"
            required
            value={formData.transactionDate}
            onChange={handleChange}
            error={errors.transactionDate}
          />

          <Select
            label="Payment Method"
            id="paymentMethod"
            name="paymentMethod"
            value={formData.paymentMethod}
            onChange={handleChange}
            options={PAYMENT_METHODS}
          />
        </div>

        {/* Notes */}
        <div>
          <label htmlFor="notes" className="block text-sm font-medium text-slate-700 mb-1">
            Notes (Optional)
          </label>
          <textarea
            id="notes"
            name="notes"
            rows="2"
            value={formData.notes}
            onChange={handleChange}
            placeholder="Additional context or details..."
            className="block w-full rounded-lg border border-slate-300 text-sm py-2 px-3.5 focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-100"
          />
        </div>

        {/* Form Actions */}
        <div className="flex items-center justify-end gap-3 pt-3 border-t border-slate-100">
          <Button variant="outline" size="sm" onClick={onClose} disabled={isSubmitting}>
            Cancel
          </Button>
          <Button type="submit" variant="primary" size="sm" isLoading={isSubmitting}>
            {isEditing ? 'Save Changes' : 'Record Transaction'}
          </Button>
        </div>
      </form>
    </Modal>
  );
};
