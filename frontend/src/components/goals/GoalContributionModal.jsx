import React, { useState } from 'react';
import { Modal } from '../common/Modal';
import { Input } from '../common/Input';
import { Select } from '../common/Select';
import { Button } from '../common/Button';
import { formatCurrency } from '../../utils/currencyFormatter';

export const GoalContributionModal = ({
  isOpen,
  onClose,
  goal,
  onRecord,
  currency = 'USD',
}) => {
  const [formData, setFormData] = useState({
    amount: '',
    type: 'DEPOSIT',
    notes: '',
  });
  const [errors, setErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  if (!goal) return null;

  const currentAmount = parseFloat(goal.currentAmount) || 0;
  const targetAmount = parseFloat(goal.targetAmount) || 0;

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: '' }));
    }
  };

  const validate = () => {
    const newErrors = {};
    const amt = parseFloat(formData.amount);
    if (!formData.amount || isNaN(amt) || amt <= 0) {
      newErrors.amount = 'Amount must be greater than zero';
    }
    if (formData.type === 'WITHDRAWAL' && amt > currentAmount) {
      newErrors.amount = `Cannot withdraw more than current balance (${formatCurrency(currentAmount, currency)})`;
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;

    setIsSubmitting(true);
    try {
      await onRecord(goal.id, {
        amount: parseFloat(formData.amount),
        type: formData.type,
        notes: formData.notes?.trim() || null,
      });
      setFormData({ amount: '', type: 'DEPOSIT', notes: '' });
      onClose();
    } catch {
      // Handled by parent toast
    } finally {
      setIsSubmitting(false);
    }
  };

  const parsedAmt = parseFloat(formData.amount) || 0;
  const projectedBalance =
    formData.type === 'DEPOSIT'
      ? currentAmount + parsedAmt
      : currentAmount - parsedAmt;

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={`Contribute to "${goal.title}"`}
      description="Record a new deposit or withdrawal to update your goal progress."
    >
      <form onSubmit={handleSubmit} className="space-y-4 mt-2">
        {/* Goal summary pill */}
        <div className="p-3 bg-slate-50 rounded-xl border border-slate-100 flex justify-between items-center text-xs">
          <div>
            <span className="text-slate-500">Current Balance: </span>
            <strong className="text-slate-900 font-semibold">{formatCurrency(currentAmount, currency)}</strong>
          </div>
          <div>
            <span className="text-slate-500">Target: </span>
            <strong className="text-slate-900 font-semibold">{formatCurrency(targetAmount, currency)}</strong>
          </div>
        </div>

        <Select
          label="Action Type"
          name="type"
          value={formData.type}
          onChange={handleChange}
          options={[
            { value: 'DEPOSIT', label: 'Deposit (Add Funds)' },
            { value: 'WITHDRAWAL', label: 'Withdrawal (Remove Funds)' },
          ]}
        />

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

        {formData.amount && (
          <p className="text-xs text-slate-500">
            Projected Balance: <strong className="text-primary-600">{formatCurrency(projectedBalance, currency)}</strong>
          </p>
        )}

        <Input
          label="Notes (Optional)"
          name="notes"
          placeholder="e.g. Monthly automated transfer or bonus allocation"
          value={formData.notes}
          onChange={handleChange}
        />

        <div className="flex justify-end gap-3 pt-4 border-t border-slate-100">
          <Button type="button" variant="outline" onClick={onClose} disabled={isSubmitting}>
            Cancel
          </Button>
          <Button type="submit" variant="primary" isLoading={isSubmitting}>
            Confirm {formData.type === 'DEPOSIT' ? 'Deposit' : 'Withdrawal'}
          </Button>
        </div>
      </form>
    </Modal>
  );
};
