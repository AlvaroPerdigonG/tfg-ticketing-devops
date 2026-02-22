export type Role = "USER" | "AGENT" | "ADMIN";

export type AuthUser = {
  id: string;      // sub
  roles: Role[];   // claim roles
  exp: number;     // unix seconds
};

export type AuthState = {
  token: string | null;
  user: AuthUser | null;
};

export type LoginRequest = {
  email: string;
  password: string;
};

export type LoginResponse = {
  accessToken: string;
};