export type Role = "USER" | "AGENT" | "ADMIN";

export type AuthUser = {
  id: string; // sub
  email: string;
  displayName: string;
  role: Role;
  roles: Role[]; // claim roles
  exp: number; // unix seconds
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

export type RegisterRequest = {
  email: string;
  displayName: string;
  password: string;
  confirmPassword: string;
};

export type ProfileResponse = {
  sub: string;
  email: string;
  displayName: string;
  role: Role;
  roles: Role[];
};
