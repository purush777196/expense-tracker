import api from './api';

export const transactionService = {
  async getTransactions(params = {}) {
    return api.get('/transactions', { params });
  },

  async getTransactionById(id) {
    return api.get(`/transactions/${id}`);
  },

  async createTransaction(transactionData) {
    return api.post('/transactions', transactionData);
  },

  async updateTransaction(id, transactionData) {
    return api.put(`/transactions/${id}`, transactionData);
  },

  async deleteTransaction(id) {
    return api.delete(`/transactions/${id}`);
  },

  async getTransactionSummary(params = {}) {
    return api.get('/transactions/summary', { params });
  },
};

export default transactionService;
