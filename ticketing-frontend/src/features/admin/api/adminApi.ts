import { createApiClient } from "../../../shared/api/client";
import type { AdminCategory, AdminUser } from "../model/types";

const AUTH_TOKEN_STORAGE_KEY = "ticketing_access_token";

const authClient = createApiClient({
  getToken: () => localStorage.getItem(AUTH_TOKEN_STORAGE_KEY),
});

export const adminApi = {
  getCategories: () => authClient.get<AdminCategory[]>("/api/admin/categories"),
  createCategory: (name: string) => authClient.post<AdminCategory>("/api/admin/categories", { name }),
  updateCategory: (categoryId: string, payload: { name: string; isActive: boolean }) =>
    authClient.patch<AdminCategory>(`/api/admin/categories/${categoryId}`, payload),
  getUsers: () => authClient.get<AdminUser[]>("/api/admin/users"),
  updateUserActive: (userId: string, isActive: boolean) =>
    authClient.patch<AdminUser>(`/api/admin/users/${userId}/active`, { isActive }),
};
