import { createContext, useContext, useMemo, useState, type ReactNode } from 'react';
import { apiFetch, setToken, type ApiError } from '../api/client';
import type { AuthResponse } from '../api/types';
import type { LoginForm } from '../types/forms';

type AuthContextValue = {
  token: string | null;
  role: string | null;
  displayName: string | null;
  isAuthenticated: boolean;
  login: (data: LoginForm) => Promise<void>;
  logout: () => void;
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setTokenState] = useState<string | null>(localStorage.getItem('token'));
  const [role, setRole] = useState<string | null>(localStorage.getItem('role'));
  const [displayName, setDisplayName] = useState<string | null>(localStorage.getItem('displayName'));

  const value = useMemo<AuthContextValue>(
    () => ({
      token,
      role,
      displayName,
      isAuthenticated: Boolean(token),
      login: async (data: LoginForm) => {
        const response = await apiFetch<AuthResponse>(
          '/auth/authenticate',
          { method: 'POST', body: JSON.stringify(data) },
          false,
        );
        setToken(response.token);
        localStorage.setItem('role', response.role);
        localStorage.setItem('displayName', response.displayName);
        setTokenState(response.token);
        setRole(response.role);
        setDisplayName(response.displayName);
      },
      logout: () => {
        setToken(null);
        setTokenState(null);
        setRole(null);
        setDisplayName(null);
      },
    }),
    [token, role, displayName],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return ctx;
}

export function getErrorMessage(error: unknown): string {
  if (error && typeof error === 'object' && 'description' in error) {
    return (error as ApiError).description || (error as ApiError).code;
  }
  return 'Something went wrong';
}
