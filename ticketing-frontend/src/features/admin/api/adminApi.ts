import { createApiClient } from "../../../shared/api/client";
import { getStoredAccessToken } from "../../../shared/auth/tokenStorage";
import type { AdminCategory, AdminUser } from "../model/types";

const authClient = createApiClient({
  getToken: getStoredAccessToken,
});

export const adminApi = {
  getCategories: () => authClient.get<AdminCategory[]>("/api/admin/categories"),
  createCategory: (name: string) =>
    authClient.post<AdminCategory>("/api/admin/categories", { name }),
  updateCategory: (categoryId: string, payload: { name: string; isActive: boolean }) =>
    authClient.patch<AdminCategory>(`/api/admin/categories/${categoryId}`, payload),
  getUsers: () => authClient.get<AdminUser[]>("/api/admin/users"),
  updateUserActive: (userId: string, isActive: boolean) =>
    authClient.patch<AdminUser>(`/api/admin/users/${userId}/active`, { isActive }),
};
