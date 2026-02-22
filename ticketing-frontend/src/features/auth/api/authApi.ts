import type { LoginRequest, LoginResponse } from "../model/types";
import { createApiClient } from "../../../shared/api/client";

// En login todavía NO hay token, por eso getToken devuelve null
const publicClient = createApiClient({ getToken: () => null });

export const authApi = {
  login: (req: LoginRequest) => publicClient.post<LoginResponse>("/api/auth/login", req),
};