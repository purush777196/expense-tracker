import React, { useState, useEffect } from 'react';
import { Modal } from '../common/Modal';
import { Input } from '../common/Input';
import { Select } from '../common/Select';
import { Button } from '../common/Button';

export const AddEditRecurringModal = ({
  isOpen,
  onClose,
  onSave,
  recurring = null,
  categories = [],
}) => {
  const [formData, setFormData] = useState({
    description: '',
    categoryId: '',
    type: 'EXPENSE',
    amount: '',
    merchant: '',
    paymentMethod: 'CARD',
    frequency: 'MONTHLY',
    intervalValue: '1',
    startDate: '',
    endDate: '',
    notes: '',
  });
  const [errors, setErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (recurring) {
      setFormData({
        description: recurring.description || '',
        categoryId: recurring.category ? recurring.category.id : '',
        type: recurring.type || 'EXPENSE',
        amount: recurring.amount ? String(recurring.amount) : '',
        merchant: recurring.merchant || '',
        paymentMethod: recurring.paymentMethod || 'CARD',
        frequency: recurring.frequency || 'MONTHLY',
        intervalValue: recurring.intervalValue ? String(recurring.intervalValue) : '1',
        startDate: recurring.startDate || '',
        endDate: recurring.endDate || '',
        notes: recurring.notes || '',
      });
    } else {
      const today = new Date().toISOString().split('T')[0];
      setFormData({
        description: '',
        categoryId: '',
        type: 'EXPENSE',
        amount: '',
        merchant: '',
        paymentMethod: 'CARD',
        frequency: 'MONTHLY',
        intervalValue: '1',
        startDate: today,
        endDate: '',
        notes: '',
      });
    }
    setErrors({});
  }, [recurring, isOpen]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => {
      const next = { ...prev, [name]: value };
      // Clear category if transaction type changed and category no longer matches
      if (name === 'type' && prev.categoryId) {
        const cat = categories.find((c) => c.id === prev.categoryId);
        if (cat && cat.type !== value) {
          next.categoryId = '';
        }
      }
      return next;
    });

    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: '' }));
    }
  };

  const validate = () => {
    const newErrors = {};
    if (!formData.description.trim()) newErrors.description = 'Description is required';
    if (!formData.categoryId) newErrors.categoryId = 'Category is required';
    if (!formData.amount || parseFloat(formData.amount) <= 0) {
      newErrors.amount = 'Amount must be greater than zero';
    }
    if (!formData.startDate) newErrors.startDate = 'Start date is required';
    if (formData.startDate && formData.endDate && formData.startDate > formData.endDate) {
      newErrors.endDate = 'End date cannot be earlier than start date';
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;

    setIsSubmitting(true);
    try {
      const payload = {
        description: formData.description.trim(),
        categoryId: formData.categoryId,
        type: formData.type,
        amount: parseFloat(formData.amount),
        merchant: formData.merchant?.trim() || null,
        paymentMethod: formData.paymentMethod,
        frequency: formData.frequency,
        intervalValue: parseInt(formData.intervalValue, 10) || 1,
        startDate: formData.startDate,
        endDate: formData.endDate || null,
        notes: formData.notes?.trim() || null,
      };

      await onSave(payload, recurring?.id);
      onClose();
    } catch {
      // Handled by parent toast
    } finally {
      setIsSubmitting(false);
    }
  };

  const filteredCategories = categories.filter((c) => c.type === formData.type);

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={recurring ? 'Edit Recurring Schedule' : 'New Recurring Schedule'}
      description="Automate recurring bills, subscriptions, salary, or loan payments."
    >
      <form onSubmit={handleSubmit} className="space-y-4 mt-2">
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <Select
            label="Transaction Type"
            name="type"
            value={formData.type}
            onChange={handleChange}
            options={[
              { value: 'EXPENSE', label: 'Expense (Bill / Subscription)' },
              { value: 'INCOME', label: 'Income (Salary / Deposit)' },
            ]}
          />

          <Select
            label="Category"
            name="categoryId"
            value={formData.categoryId}
            onChange={handleChange}
            options={[
              { value: '', label: 'Select a category...' },
              ...filteredCategories.map((c) => ({
                value: c.id,
                label: c.name,
              })),
            ]}
            error={errors.categoryId}
            required
          />
        </div>

        <Input
          label="Description / Name"
          name="description"
          placeholder="e.g. Netflix Subscription or Apartment Rent"
          value={formData.description}
          onChange={handleChange}
          error={errors.description}
          required
        />

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <Input
            label="Amount"
            name="amount"
            type="number"
            step="0.01"
            min="0.01"
            placeholder="0.00"
            value={formData.amount}
            onChange={handleChange}
            error={errors.amount}
            required
          />

          <Input
            label="Merchant / Payee"
            name="merchant"
            placeholder="e.g. Netflix Inc."
            value={formData.merchant}
            onChange={handleChange}
          />
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <Select
            label="Frequency"
            name="frequency"
            value={formData.frequency}
            onChange={handleChange}
            options={[
              { value: 'DAILY', label: 'Daily' },
              { value: 'WEEKLY', label: 'Weekly' },
              { value: 'MONTHLY', label: 'Monthly' },
              { value: 'YEARLY', label: 'Yearly' },
            ]}
          />

          <Input
            label="Interval"
            name="intervalValue"
            type="number"
            min="1"
            value={formData.intervalValue}
            onChange={handleChange}
            helperText="Every X periods (default: 1)"
          />

          <Select
            label="Payment Method"
            name="paymentMethod"
            value={formData.paymentMethod}
            onChange={handleChange}
            options={[
              { value: 'CARD', label: 'Credit/Debit Card' },
              { value: 'BANK_TRANSFER', label: 'Bank Transfer' },
              { value: 'UPI', label: 'UPI' },
              { value: 'CASH', label: 'Cash' },
              { value: 'OTHER', label: 'Other' },
            ]}
          />
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <Input
            label="Start Date"
            name="startDate"
            type="date"
            value={formData.startDate}
            onChange={handleChange}
            error={errors.startDate}
            required
          />

          <Input
            label="End Date (Optional)"
            name="endDate"
            type="date"
            value={formData.endDate}
            onChange={handleChange}
            error={errors.endDate}
          />
        </div>

        <Input
          label="Notes (Optional)"
          name="notes"
          placeholder="e.g. Auto-debits on 1st of month"
          value={formData.notes}
          onChange={handleChange}
        />

        <div className="flex justify-end gap-3 pt-4 border-t border-slate-100">
          <Button type="button" variant="outline" onClick={onClose} disabled={isSubmitting}>
            Cancel
          </Button>
          <Button type="submit" variant="primary" isLoading={isSubmitting}>
            {recurring ? 'Update Schedule' : 'Create Schedule'}
          </Button>
        </div>
      </form>
    </Modal>
  );
};
