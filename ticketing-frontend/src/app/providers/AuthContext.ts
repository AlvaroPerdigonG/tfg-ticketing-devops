import { createContext } from "react";
import type { AuthState, Role } from "../../features/auth/model/types";

export type AuthContextValue = {
  state: AuthState;
  isAuthenticated: boolean;
  isHydrated: boolean;
  hasRole: (role: Role) => boolean;
  hasAnyRole: (roles: Role[]) => boolean;
  login: (email: string, password: string, remember?: boolean) => Promise<void>;
  register: (payload: {
    email: string;
    displayName: string;
    password: string;
    confirmPassword: string;
    remember?: boolean;
  }) => Promise<void>;
  loginWithToken: (token: string, remember?: boolean) => void;
  logout: () => void;
};

export const AuthContext = createContext<AuthContextValue | null>(null);
