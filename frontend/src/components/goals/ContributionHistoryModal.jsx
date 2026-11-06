import React, { useState, useEffect } from 'react';
import { Modal } from '../common/Modal';
import { Button } from '../common/Button';
import { ArrowUpRight, ArrowDownLeft } from 'lucide-react';
import { goalService } from '../../services/goalService';
import { formatCurrency } from '../../utils/currencyFormatter';
import { formatDate } from '../../utils/dateFormatter';

export const ContributionHistoryModal = ({
  isOpen,
  onClose,
  goal,
  currency = 'USD',
}) => {
  const [contributions, setContributions] = useState([]);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (isOpen && goal?.id) {
      setIsLoading(true);
      goalService
        .getContributions(goal.id)
        .then((res) => {
          const list = res?.data || res || [];
          setContributions(Array.isArray(list) ? list : []);
        })
        .catch(() => setContributions([]))
        .finally(() => setIsLoading(false));
    }
  }, [isOpen, goal]);

  if (!goal) return null;

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={`Contribution History – ${goal.title}`}
      description="Audit log of all deposits, withdrawals, and updates to this goal."
      maxWidth="max-w-xl"
    >
      <div className="mt-3 space-y-3">
        {isLoading ? (
          <div className="py-8 text-center text-xs text-slate-500">
            Loading contribution audit logs...
          </div>
        ) : contributions.length === 0 ? (
          <div className="py-8 text-center text-xs text-slate-500">
            No contributions recorded yet.
          </div>
        ) : (
          <div className="max-h-80 overflow-y-auto space-y-2 pr-1 divide-y divide-slate-100">
            {contributions.map((c) => {
              const isDeposit = c.type === 'DEPOSIT';
              const amt = parseFloat(c.amount) || 0;
              return (
                <div key={c.id} className="pt-2 first:pt-0 flex items-center justify-between text-xs">
                  <div className="flex items-center gap-2.5">
                    <div
                      className={`p-1.5 rounded-lg ${
                        isDeposit ? 'bg-emerald-50 text-emerald-600' : 'bg-rose-50 text-rose-600'
                      }`}
                    >
                      {isDeposit ? (
                        <ArrowDownLeft className="w-4 h-4" />
                      ) : (
                        <ArrowUpRight className="w-4 h-4" />
                      )}
                    </div>
                    <div>
                      <p className="font-semibold text-slate-800">
                        {isDeposit ? 'Deposit' : 'Withdrawal'}
                      </p>
                      <p className="text-[11px] text-slate-400">
                        {c.notes || 'No description'}
                      </p>
                    </div>
                  </div>

                  <div className="text-right">
                    <p
                      className={`font-bold ${
                        isDeposit ? 'text-emerald-600' : 'text-rose-600'
                      }`}
                    >
                      {isDeposit ? '+' : '-'}
                      {formatCurrency(amt, currency)}
                    </p>
                    <p className="text-[10px] text-slate-400">
                      {formatDate(c.createdAt)}
                    </p>
                  </div>
                </div>
              );
            })}
          </div>
        )}

        <div className="flex justify-end pt-3 border-t border-slate-100">
          <Button variant="outline" size="sm" onClick={onClose}>
            Close
          </Button>
        </div>
      </div>
    </Modal>
  );
};
