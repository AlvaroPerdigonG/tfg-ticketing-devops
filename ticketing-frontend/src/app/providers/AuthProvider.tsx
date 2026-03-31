import React, { useCallback, useEffect, useMemo, useState } from "react";
import type { AuthState, Role } from "../../features/auth/model/types";
import { authApi } from "../../features/auth/api/authApi";
import { isExpired, toAuthUser } from "../../features/auth/model/jwt";
import { AuthContext, type AuthContextValue } from "./AuthContext";

const STORAGE_KEY = "ticketing_access_token";

function getInitialAuthState(): AuthState {
  const token = localStorage.getItem(STORAGE_KEY);

  if (!token) {
    return { token: null, user: null };
  }

  try {
    const user = toAuthUser(token);
    if (isExpired(user.exp)) {
      localStorage.removeItem(STORAGE_KEY);
      return { token: null, user: null };
    }

    return { token, user };
  } catch {
    localStorage.removeItem(STORAGE_KEY);
    return { token: null, user: null };
  }
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [state, setState] = useState<AuthState>(getInitialAuthState);
  const isHydrated = true;

  const logout = useCallback(() => {
    localStorage.removeItem(STORAGE_KEY);
    setState({ token: null, user: null });
  }, []);

  const loginWithToken = useCallback((token: string, remember = false) => {
    const user = toAuthUser(token);

    if (isExpired(user.exp)) {
      throw new Error("Token expired");
    }

    if (remember) localStorage.setItem(STORAGE_KEY, token);
    else localStorage.removeItem(STORAGE_KEY);

    setState({ token, user });
  }, []);

  const login = useCallback(
    async (email: string, password: string, remember = false) => {
      const res = await authApi.login({ email, password });
      loginWithToken(res.accessToken, remember);
    },
    [loginWithToken],
  );

  const register = useCallback(
    async ({
      email,
      displayName,
      password,
      confirmPassword,
      remember = false,
    }: {
      email: string;
      displayName: string;
      password: string;
      confirmPassword: string;
      remember?: boolean;
    }) => {
      const res = await authApi.register({ email, displayName, password, confirmPassword });
      loginWithToken(res.accessToken, remember);
    },
    [loginWithToken],
  );

  // Auto-logout si expira mientras estás en la app
  useEffect(() => {
    if (!state.user) return;

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
      register,
      loginWithToken,
      logout,
    }),
    [state, isHydrated, hasRole, hasAnyRole, login, register, loginWithToken, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
