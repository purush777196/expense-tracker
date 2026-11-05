import React from 'react';
import { BrainCircuit, RefreshCw } from 'lucide-react';
import { Button } from '../../components/common/Button';
import { EmptyState } from '../../components/common/EmptyState';

export const InsightsPage = () => {
  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-xl sm:text-2xl font-bold tracking-tight text-slate-900">
            AI Financial Insights
          </h2>
          <p className="text-xs sm:text-sm text-slate-500 mt-1">
            Machine learning spending forecasting, anomaly detection, and automated advice.
          </p>
        </div>

        <Button variant="outline" size="sm" leftIcon={<RefreshCw className="w-4 h-4" />}>
          Run Inference
        </Button>
      </div>

      <EmptyState
        icon={BrainCircuit}
        title="Python FastAPI AI Intelligence Engine (Phase 5)"
        description="FastAPI service featuring Prophet/ARIMA spending forecasts, Isolation Forest anomaly detection, and rule-based recommendation models will be integrated in Phase 5."
        className="py-16"
      />
    </div>
  );
};

