import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from 'react';
import { apiFetch, setToken, type ApiError } from '../api/client';
import type { AuthResponse } from '../api/types';
import { clearPendingOnboarding, hasPendingOnboarding } from '../onboarding';
import type { LoginForm } from '../types/forms';

type AuthContextValue = {
  token: string | null;
  role: string | null;
  displayName: string | null;
  username: string | null;
  isAuthenticated: boolean;
  needsOnboarding: boolean;
  login: (data: LoginForm) => Promise<void>;
  logout: () => void;
  completeOnboarding: () => void;
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setTokenState] = useState<string | null>(localStorage.getItem('token'));
  const [role, setRole] = useState<string | null>(localStorage.getItem('role'));
  const [displayName, setDisplayName] = useState<string | null>(localStorage.getItem('displayName'));
  const [username, setUsername] = useState<string | null>(localStorage.getItem('username'));
  const [needsOnboarding, setNeedsOnboarding] = useState(() => {
    const storedUsername = localStorage.getItem('username');
    return Boolean(storedUsername && hasPendingOnboarding(storedUsername));
  });

  const completeOnboarding = useCallback(() => {
    const currentUsername = localStorage.getItem('username');
    if (currentUsername) {
      clearPendingOnboarding(currentUsername);
    }
    setNeedsOnboarding(false);
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      token,
      role,
      displayName,
      username,
      isAuthenticated: Boolean(token),
      needsOnboarding,
      completeOnboarding,
      login: async (data: LoginForm) => {
        const response = await apiFetch<AuthResponse>(
          '/auth/authenticate',
          { method: 'POST', body: JSON.stringify(data) },
          false,
        );
        setToken(response.token);
        localStorage.setItem('role', response.role);
        localStorage.setItem('displayName', response.displayName);
        localStorage.setItem('username', data.username);
        setTokenState(response.token);
        setRole(response.role);
        setDisplayName(response.displayName);
        setUsername(data.username);
        setNeedsOnboarding(hasPendingOnboarding(data.username));
      },
      logout: () => {
        setToken(null);
        setTokenState(null);
        setRole(null);
        setDisplayName(null);
        setUsername(null);
        setNeedsOnboarding(false);
        localStorage.removeItem('username');
      },
    }),
    [token, role, displayName, username, needsOnboarding, completeOnboarding],
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
