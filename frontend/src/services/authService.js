import api from './api';

export const authService = {
  async register(data) {
    return api.post('/auth/register', data);
  },

  async login(credentials) {
    return api.post('/auth/login', credentials);
  },

  async refreshToken(refreshToken) {
    return api.post('/auth/refresh', { refreshToken });
  },

  async getCurrentUser() {
    return api.get('/users/me');
  },

  async updateProfile(profileData) {
    return api.put('/users/me', profileData);
  },

  async changePassword(passwordData) {
    return api.put('/users/me/password', passwordData);
  },

  async logout() {
    return api.post('/auth/logout').catch(() => {
      // Ignore network errors on logout
    });
  },
};

export default authService;
