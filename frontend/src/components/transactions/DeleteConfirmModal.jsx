import React from 'react';
import { AlertTriangle } from 'lucide-react';
import { Modal } from '../common/Modal';
import { Button } from '../common/Button';
import { formatCurrency } from '../../utils/currencyFormatter';

export const DeleteConfirmModal = ({
  isOpen,
  onClose,
  onConfirm,
  transaction,
  isDeleting,
  currency = 'USD',
}) => {
  if (!transaction) return null;

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Delete Transaction"
      maxWidth="max-w-md"
    >
      <div className="space-y-4">
        <div className="p-3 rounded-xl bg-rose-50 border border-rose-200 flex items-start gap-3">
          <AlertTriangle className="w-5 h-5 text-rose-600 shrink-0 mt-0.5" />
          <div className="text-xs text-rose-800">
            <p className="font-semibold">Are you sure you want to delete this record?</p>
            <p className="mt-1">
              This action cannot be undone and will permanently remove this transaction from your financial history.
            </p>
          </div>
        </div>

        <div className="p-3.5 rounded-xl bg-slate-50 border border-slate-200 text-xs space-y-1.5">
          <div className="flex justify-between">
            <span className="text-slate-500">Description:</span>
            <span className="font-semibold text-slate-800">{transaction.description}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-slate-500">Amount:</span>
            <span className="font-semibold text-slate-900">
              {formatCurrency(transaction.amount, currency)}
            </span>
          </div>
          <div className="flex justify-between">
            <span className="text-slate-500">Category:</span>
            <span className="font-medium text-slate-700">
              {transaction.category?.name || 'Uncategorized'}
            </span>
          </div>
        </div>

        <div className="flex justify-end gap-2 pt-2">
          <Button variant="outline" size="sm" onClick={onClose} disabled={isDeleting}>
            Cancel
          </Button>
          <Button
            variant="danger"
            size="sm"
            onClick={onConfirm}
            isLoading={isDeleting}
          >
            Delete Transaction
          </Button>
        </div>
      </div>
    </Modal>
  );
};
