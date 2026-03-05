import type { LoginRequest, LoginResponse, ProfileResponse, RegisterRequest } from "../model/types";
import { createApiClient } from "../../../shared/api/client";

const publicClient = createApiClient({ getToken: () => null });

export const authApi = {
  login: (req: LoginRequest) => publicClient.post<LoginResponse>("/api/auth/login", req),
  register: (req: RegisterRequest) => publicClient.post<LoginResponse>("/api/auth/register", req),
  me: (token: string) => createApiClient({ getToken: () => token }).get<ProfileResponse>("/api/auth/me"),
};
