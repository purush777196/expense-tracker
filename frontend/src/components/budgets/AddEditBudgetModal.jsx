import React, { useState, useEffect } from 'react';
import { Modal } from '../common/Modal';
import { Input } from '../common/Input';
import { Select } from '../common/Select';
import { Button } from '../common/Button';

export const AddEditBudgetModal = ({
  isOpen,
  onClose,
  onSave,
  budget = null,
  categories = [],
}) => {
  const [formData, setFormData] = useState({
    name: '',
    categoryId: '',
    amount: '',
    period: 'MONTHLY',
    startDate: '',
    endDate: '',
    alertThreshold: '80',
  });
  const [errors, setErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Set default dates for current month
  useEffect(() => {
    if (budget) {
      setFormData({
        name: budget.name || '',
        categoryId: budget.category ? budget.category.id : '',
        amount: budget.amount ? String(budget.amount) : '',
        period: budget.period || 'MONTHLY',
        startDate: budget.startDate || '',
        endDate: budget.endDate || '',
        alertThreshold: budget.alertThreshold ? String(budget.alertThreshold) : '80',
      });
    } else {
      const now = new Date();
      const firstDay = new Date(now.getFullYear(), now.getMonth(), 1)
        .toISOString()
        .split('T')[0];
      const lastDay = new Date(now.getFullYear(), now.getMonth() + 1, 0)
        .toISOString()
        .split('T')[0];

      setFormData({
        name: '',
        categoryId: '',
        amount: '',
        period: 'MONTHLY',
        startDate: firstDay,
        endDate: lastDay,
        alertThreshold: '80',
      });
    }
    setErrors({});
  }, [budget, isOpen]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: '' }));
    }
  };

  const validate = () => {
    const newErrors = {};
    if (!formData.name.trim()) newErrors.name = 'Budget name is required';
    if (!formData.amount || parseFloat(formData.amount) <= 0) {
      newErrors.amount = 'Amount must be greater than zero';
    }
    if (!formData.startDate) newErrors.startDate = 'Start date is required';
    if (!formData.endDate) newErrors.endDate = 'End date is required';
    if (formData.startDate && formData.endDate && formData.startDate > formData.endDate) {
      newErrors.endDate = 'End date cannot be before start date';
    }
    if (formData.alertThreshold) {
      const thresh = parseFloat(formData.alertThreshold);
      if (thresh < 1 || thresh > 100) {
        newErrors.alertThreshold = 'Alert threshold must be between 1% and 100%';
      }
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
        name: formData.name.trim(),
        categoryId: formData.categoryId ? formData.categoryId : null,
        amount: parseFloat(formData.amount),
        period: formData.period,
        startDate: formData.startDate,
        endDate: formData.endDate,
        alertThreshold: formData.alertThreshold ? parseFloat(formData.alertThreshold) : 80,
      };

      await onSave(payload, budget?.id);
      onClose();
    } catch {
      // Error handled by parent toast
    } finally {
      setIsSubmitting(false);
    }
  };

  // Only expense categories are permitted for budgets
  const expenseCategories = categories.filter((c) => c.type === 'EXPENSE');

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={budget ? 'Edit Budget' : 'Create Budget'}
      description="Define spending limits and track overrun alerts for your categories or overall expenses."
    >
      <form onSubmit={handleSubmit} className="space-y-4 mt-2">
        <Input
          label="Budget Name"
          name="name"
          placeholder="e.g. Monthly Dining Limit"
          value={formData.name}
          onChange={handleChange}
          error={errors.name}
          required
        />

        <Select
          label="Category (Optional for Overall Budget)"
          name="categoryId"
          value={formData.categoryId}
          onChange={handleChange}
          options={[
            { value: '', label: 'Overall Budget (All Expense Categories)' },
            ...expenseCategories.map((c) => ({
              value: c.id,
              label: c.name,
            })),
          ]}
        />

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <Input
            label="Budget Amount"
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

          <Select
            label="Budget Period"
            name="period"
            value={formData.period}
            onChange={handleChange}
            options={[
              { value: 'MONTHLY', label: 'Monthly' },
              { value: 'WEEKLY', label: 'Weekly' },
              { value: 'YEARLY', label: 'Yearly' },
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
            label="End Date"
            name="endDate"
            type="date"
            value={formData.endDate}
            onChange={handleChange}
            error={errors.endDate}
            required
          />
        </div>

        <Input
          label="Alert Threshold (%)"
          name="alertThreshold"
          type="number"
          min="1"
          max="100"
          placeholder="80"
          value={formData.alertThreshold}
          onChange={handleChange}
          helperText="Triggers warning notification when spending reaches this percentage."
          error={errors.alertThreshold}
        />

        <div className="flex justify-end gap-3 pt-4 border-t border-slate-100">
          <Button type="button" variant="outline" onClick={onClose} disabled={isSubmitting}>
            Cancel
          </Button>
          <Button type="submit" variant="primary" isLoading={isSubmitting}>
            {budget ? 'Update Budget' : 'Save Budget'}
          </Button>
        </div>
      </form>
    </Modal>
  );
};
