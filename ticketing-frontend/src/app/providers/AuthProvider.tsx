import React, { createContext, useCallback, useEffect, useMemo, useState } from "react";
import type { AuthState, Role } from "../../features/auth/model/types";
import { authApi } from "../../features/auth/api/authApi";
import { isExpired, toAuthUser } from "../../features/auth/model/jwt";

type AuthContextValue = {
  state: AuthState;
  isAuthenticated: boolean;
  isHydrated: boolean;
  hasRole: (role: Role) => boolean;
  hasAnyRole: (roles: Role[]) => boolean;

  login: (email: string, password: string, remember?: boolean) => Promise<void>;
  loginWithToken: (token: string, remember?: boolean) => void;
  logout: () => void;
};

const STORAGE_KEY = "ticketing_access_token";

export const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [state, setState] = useState<AuthState>({ token: null, user: null });
  const [isHydrated, setIsHydrated] = useState(false);

  const logout = useCallback(() => {
    localStorage.removeItem(STORAGE_KEY);
    setState({ token: null, user: null });
  }, []);

  const loginWithToken = useCallback(
    (token: string, remember = false) => {
      const user = toAuthUser(token);

      if (isExpired(user.exp)) {
        throw new Error("Token expired");
      }

      if (remember) localStorage.setItem(STORAGE_KEY, token);
      else localStorage.removeItem(STORAGE_KEY);

      setState({ token, user });
    },
    [],
  );

  const login = useCallback(
    async (email: string, password: string, remember = false) => {
      const res = await authApi.login({ email, password });
      loginWithToken(res.accessToken, remember);
    },
    [loginWithToken],
  );

  // Auto-hidratar desde localStorage
  useEffect(() => {
    const token = localStorage.getItem(STORAGE_KEY);

    if (token) {
      try {
        const user = toAuthUser(token);
        if (isExpired(user.exp)) {
          localStorage.removeItem(STORAGE_KEY);
        } else {
          setState({ token, user });
        }
      } catch {
        localStorage.removeItem(STORAGE_KEY);
      }
    }

    setIsHydrated(true);
  }, []);

  // Auto-logout si expira mientras estás en la app
  useEffect(() => {
    if (!state.user) return;

    if (isExpired(state.user.exp)) {
      logout();
      return;
    }

    const now = Math.floor(Date.now() / 1000);
    const secondsUntilExp = state.user.exp - now;
    const timeoutMs = Math.max(0, (secondsUntilExp - 5) * 1000);

    const id = window.setTimeout(() => logout(), timeoutMs);
    return () => window.clearTimeout(id);
  }, [state.user, logout]);

  const hasRole = useCallback(
    (role: Role) => Boolean(state.user?.roles?.includes(role)),
    [state.user],
  );

  const hasAnyRole = useCallback(
    (roles: Role[]) => roles.some((r) => state.user?.roles?.includes(r)),
    [state.user],
  );

  const value = useMemo<AuthContextValue>(
    () => ({
      state,
      isAuthenticated: Boolean(state.token && state.user && !isExpired(state.user.exp)),
      isHydrated,
      hasRole,
      hasAnyRole,
      login,
      loginWithToken,
      logout,
    }),
    [state, isHydrated, hasRole, hasAnyRole, login, loginWithToken, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}