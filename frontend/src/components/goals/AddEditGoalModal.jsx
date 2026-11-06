import React, { useState, useEffect } from 'react';
import { Modal } from '../common/Modal';
import { Input } from '../common/Input';
import { Button } from '../common/Button';

export const AddEditGoalModal = ({
  isOpen,
  onClose,
  onSave,
  goal = null,
}) => {
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    targetAmount: '',
    initialAmount: '0.00',
    targetDate: '',
  });
  const [errors, setErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (goal) {
      setFormData({
        title: goal.title || '',
        description: goal.description || '',
        targetAmount: goal.targetAmount ? String(goal.targetAmount) : '',
        initialAmount: goal.currentAmount ? String(goal.currentAmount) : '0.00',
        targetDate: goal.targetDate || '',
      });
    } else {
      // Default target date 6 months from now
      const targetDate = new Date();
      targetDate.setMonth(targetDate.getMonth() + 6);
      const formattedDate = targetDate.toISOString().split('T')[0];

      setFormData({
        title: '',
        description: '',
        targetAmount: '',
        initialAmount: '0.00',
        targetDate: formattedDate,
      });
    }
    setErrors({});
  }, [goal, isOpen]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: '' }));
    }
  };

  const validate = () => {
    const newErrors = {};
    if (!formData.title.trim()) newErrors.title = 'Goal title is required';
    if (!formData.targetAmount || parseFloat(formData.targetAmount) <= 0) {
      newErrors.targetAmount = 'Target amount must be greater than zero';
    }
    if (!goal && formData.initialAmount) {
      const initial = parseFloat(formData.initialAmount);
      if (initial < 0) {
        newErrors.initialAmount = 'Initial amount cannot be negative';
      }
      if (initial > parseFloat(formData.targetAmount || 0)) {
        newErrors.initialAmount = 'Initial amount cannot exceed target amount';
      }
    }
    if (!formData.targetDate) newErrors.targetDate = 'Target date is required';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;

    setIsSubmitting(true);
    try {
      const payload = {
        title: formData.title.trim(),
        description: formData.description?.trim() || null,
        targetAmount: parseFloat(formData.targetAmount),
        targetDate: formData.targetDate,
      };

      if (!goal) {
        payload.initialAmount = parseFloat(formData.initialAmount || 0);
      }

      await onSave(payload, goal?.id);
      onClose();
    } catch {
      // Error handled by parent toast
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={goal ? 'Edit Financial Goal' : 'Create Financial Goal'}
      description="Track emergency funds, vacation savings, or major milestone targets."
    >
      <form onSubmit={handleSubmit} className="space-y-4 mt-2">
        <Input
          label="Goal Title"
          name="title"
          placeholder="e.g. Emergency Fund (6 Months)"
          value={formData.title}
          onChange={handleChange}
          error={errors.title}
          required
        />

        <Input
          label="Description (Optional)"
          name="description"
          placeholder="Brief note or purpose of this savings milestone"
          value={formData.description}
          onChange={handleChange}
        />

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <Input
            label="Target Amount"
            name="targetAmount"
            type="number"
            step="0.01"
            min="0.01"
            placeholder="5000.00"
            value={formData.targetAmount}
            onChange={handleChange}
            error={errors.targetAmount}
            required
          />

          {!goal ? (
            <Input
              label="Starting Saved Amount"
              name="initialAmount"
              type="number"
              step="0.01"
              min="0"
              placeholder="0.00"
              value={formData.initialAmount}
              onChange={handleChange}
              error={errors.initialAmount}
            />
          ) : (
            <Input
              label="Target Date"
              name="targetDate"
              type="date"
              value={formData.targetDate}
              onChange={handleChange}
              error={errors.targetDate}
              required
            />
          )}
        </div>

        {!goal && (
          <Input
            label="Target Date"
            name="targetDate"
            type="date"
            value={formData.targetDate}
            onChange={handleChange}
            error={errors.targetDate}
            required
          />
        )}

        <div className="flex justify-end gap-3 pt-4 border-t border-slate-100">
          <Button type="button" variant="outline" onClick={onClose} disabled={isSubmitting}>
            Cancel
          </Button>
          <Button type="submit" variant="primary" isLoading={isSubmitting}>
            {goal ? 'Update Goal' : 'Create Goal'}
          </Button>
        </div>
      </form>
    </Modal>
  );
};
