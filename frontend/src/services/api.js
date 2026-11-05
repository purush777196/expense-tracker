import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api/v1';

let inMemoryAccessToken = null;

export const setApiAccessToken = (token) => {
  inMemoryAccessToken = token;
};

export const getApiAccessToken = () => inMemoryAccessToken;

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json',
  },
});

// Request Interceptor: Attach in-memory access token
api.interceptors.request.use(
  (config) => {
    if (inMemoryAccessToken && !config.headers.Authorization) {
      config.headers.Authorization = `Bearer ${inMemoryAccessToken}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor: Process RFC 7807 error responses and handle 401 token refresh loops
let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

api.interceptors.response.use(
  (response) => {
    // Return standard ApiResponse data body
    return response.data;
  },
  async (error) => {
    const originalRequest = error.config;

    // Handle 401 Unauthorized for expired access tokens
    if (error.response?.status === 401 && !originalRequest._retry) {
      const storedRefreshToken = sessionStorage.getItem('expense_tracker_refresh_token');

      // Do not attempt refresh on auth endpoints (login, register, refresh itself)
      const isAuthEndpoint = originalRequest.url?.includes('/auth/');
      if (storedRefreshToken && !isAuthEndpoint) {
        if (isRefreshing) {
          return new Promise((resolve, reject) => {
            failedQueue.push({ resolve, reject });
          })
            .then((token) => {
              originalRequest.headers.Authorization = `Bearer ${token}`;
              return api(originalRequest);
            })
            .catch((err) => Promise.reject(err));
        }

        originalRequest._retry = true;
        isRefreshing = true;

        try {
          const refreshResponse = await axios.post(`${API_BASE_URL}/auth/refresh`, {
            refreshToken: storedRefreshToken,
          });

          const newAccessToken = refreshResponse.data?.data?.accessToken;
          if (newAccessToken) {
            setApiAccessToken(newAccessToken);
            processQueue(null, newAccessToken);
            originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
            return api(originalRequest);
          }
        } catch (refreshErr) {
          processQueue(refreshErr, null);
          sessionStorage.removeItem('expense_tracker_refresh_token');
          setApiAccessToken(null);
          window.dispatchEvent(new CustomEvent('auth:session-expired'));
          return Promise.reject(refreshErr);
        } finally {
          isRefreshing = false;
        }
      }
    }

    // Parse RFC 7807 Problem Details
    const problemDetails = {
      status: error.response?.status || 500,
      error: error.response?.data?.error || 'Network Error',
      message: error.response?.data?.message || error.message || 'An unexpected error occurred',
      validationErrors: error.response?.data?.validationErrors || [],
      raw: error,
    };

    return Promise.reject(problemDetails);
  }
);

export default api;

