import React from 'react';
import { Receipt, Plus, Filter, Download } from 'lucide-react';
import { Button } from '../../components/common/Button';
import { EmptyState } from '../../components/common/EmptyState';

export const TransactionsPage = () => {
  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-xl sm:text-2xl font-bold tracking-tight text-slate-900">
            Transactions
          </h2>
          <p className="text-xs sm:text-sm text-slate-500 mt-1">
            Manage your personal income and expense ledger entries.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm" leftIcon={<Filter className="w-4 h-4" />}>
            Filter
          </Button>
          <Button variant="outline" size="sm" leftIcon={<Download className="w-4 h-4" />}>
            Export CSV
          </Button>
          <Button variant="primary" size="sm" leftIcon={<Plus className="w-4 h-4" />}>
            Add Transaction
          </Button>
        </div>
      </div>

      <EmptyState
        icon={Receipt}
        title="Transaction Ledger Integration (Phase 3)"
        description="Comprehensive transaction CRUD, category mapping, search filters, and receipt attachments will be implemented in Phase 3."
        className="py-16"
      />
    </div>
  );
};

