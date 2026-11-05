import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import authService from '../services/authService';
import { setApiAccessToken } from '../services/api';

const REFRESH_TOKEN_STORAGE_KEY = 'expense_tracker_refresh_token';

export const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [accessToken, setAccessToken] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  const applyAuthData = useCallback((authData) => {
    if (!authData) return;
    const token = authData.accessToken;
    const refreshToken = authData.refreshToken;
    const userData = authData.user;

    setAccessToken(token);
    setApiAccessToken(token);

    if (refreshToken) {
      sessionStorage.setItem(REFRESH_TOKEN_STORAGE_KEY, refreshToken);
    }

    if (userData) {
      setUser(userData);
    }
  }, []);

  const clearSession = useCallback(() => {
    setAccessToken(null);
    setUser(null);
    setApiAccessToken(null);
    sessionStorage.removeItem(REFRESH_TOKEN_STORAGE_KEY);
  }, []);

  const logout = useCallback(async () => {
    try {
      await authService.logout();
    } catch {
      // Ignore network errors on logout
    } finally {
      clearSession();
    }
  }, [clearSession]);

  // Initialize session on mount
  useEffect(() => {
    let isMounted = true;

    const initAuth = async () => {
      const storedRefreshToken = sessionStorage.getItem(REFRESH_TOKEN_STORAGE_KEY);
      if (!storedRefreshToken) {
        if (isMounted) setIsLoading(false);
        return;
      }

      try {
        const response = await authService.refreshToken(storedRefreshToken);
        const data = response?.data || response;
        if (isMounted && data?.accessToken) {
          applyAuthData(data);
        } else {
          clearSession();
        }
      } catch {
        if (isMounted) {
          clearSession();
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    };

    initAuth();

    const handleSessionExpired = () => {
      logout();
    };
    window.addEventListener('auth:session-expired', handleSessionExpired);

    return () => {
      isMounted = false;
      window.removeEventListener('auth:session-expired', handleSessionExpired);
    };
  }, [applyAuthData, clearSession, logout]);

  const login = async (credentialsOrEmail, maybePassword) => {
    const payload =
      typeof credentialsOrEmail === 'object' && credentialsOrEmail !== null
        ? credentialsOrEmail
        : { email: credentialsOrEmail, password: maybePassword };

    const response = await authService.login(payload);
    const data = response?.data || response;

    if (data?.accessToken) {
      applyAuthData(data);
      return data;
    }
    throw new Error(response?.message || 'Login failed');
  };

  const register = async (userData) => {
    const response = await authService.register(userData);
    const data = response?.data || response;

    if (data?.accessToken) {
      applyAuthData(data);
      return data;
    }
    throw new Error(response?.message || 'Registration failed');
  };

  const updateUser = (updatedUser) => {
    setUser((prev) => ({ ...prev, ...updatedUser }));
  };

  const value = {
    user,
    accessToken,
    isAuthenticated: Boolean(accessToken && user),
    isLoading,
    login,
    register,
    logout,
    updateUser,
    clearSession,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
