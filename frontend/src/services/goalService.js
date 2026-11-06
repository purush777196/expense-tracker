import api from './api';

export const goalService = {
  async getGoals(params = {}) {
    return api.get('/goals', { params });
  },

  async getGoalSummary() {
    return api.get('/goals/summary');
  },

  async getGoalById(id) {
    return api.get(`/goals/${id}`);
  },

  async createGoal(goalData) {
    return api.post('/goals', goalData);
  },

  async updateGoal(id, goalData) {
    return api.put(`/goals/${id}`, goalData);
  },

  async deleteGoal(id) {
    return api.delete(`/goals/${id}`);
  },

  async recordContribution(id, contributionData) {
    return api.post(`/goals/${id}/contributions`, contributionData);
  },

  async getContributions(id) {
    return api.get(`/goals/${id}/contributions`);
  },
};

export default goalService;
