import api from './api';

export const recurringService = {
  async getRecurringTransactions(params = {}) {
    return api.get('/recurring-transactions', { params });
  },

  async getRecurringTransactionById(id) {
    return api.get(`/recurring-transactions/${id}`);
  },

  async createRecurringTransaction(recurringData) {
    return api.post('/recurring-transactions', recurringData);
  },

  async updateRecurringTransaction(id, recurringData) {
    return api.put(`/recurring-transactions/${id}`, recurringData);
  },

  async deleteRecurringTransaction(id) {
    return api.delete(`/recurring-transactions/${id}`);
  },

  async pauseRecurringTransaction(id) {
    return api.patch(`/recurring-transactions/${id}/pause`);
  },

  async resumeRecurringTransaction(id) {
    return api.patch(`/recurring-transactions/${id}/resume`);
  },

  async cancelRecurringTransaction(id) {
    return api.patch(`/recurring-transactions/${id}/cancel`);
  },

  async getUpcomingRecurring(days = 30) {
    return api.get('/recurring-transactions/upcoming', { params: { days } });
  },

  async triggerProcessing(asOfDate = null) {
    return api.post('/recurring-transactions/trigger', null, {
      params: asOfDate ? { asOfDate } : {},
    });
  },
};

export default recurringService;
