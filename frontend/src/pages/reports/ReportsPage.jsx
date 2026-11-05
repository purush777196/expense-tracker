import React from 'react';
import { BarChart3, Download, Calendar } from 'lucide-react';
import { Button } from '../../components/common/Button';
import { EmptyState } from '../../components/common/EmptyState';

export const ReportsPage = () => {
  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-xl sm:text-2xl font-bold tracking-tight text-slate-900">
            Financial Reports
          </h2>
          <p className="text-xs sm:text-sm text-slate-500 mt-1">
            Generate customized financial statements, tax summaries, and annual retrospectives.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm" leftIcon={<Calendar className="w-4 h-4" />}>
            Select Period
          </Button>
          <Button variant="primary" size="sm" leftIcon={<Download className="w-4 h-4" />}>
            Download PDF
          </Button>
        </div>
      </div>

      <EmptyState
        icon={BarChart3}
        title="Comprehensive Reporting Engine"
        description="Multi-period financial reporting, tax summaries, and PDF/CSV export generation will be available in upcoming releases."
        className="py-16"
      />
    </div>
  );
};

