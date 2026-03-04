import { createApiClient } from "../../../shared/api/client";
import type { PaginatedResponse, TicketQueueScope, TicketStatus, TicketSummary } from "../model/types";

const AUTH_TOKEN_STORAGE_KEY = "ticketing_access_token";

const authClient = createApiClient({
  getToken: () => localStorage.getItem(AUTH_TOKEN_STORAGE_KEY),
});

function buildQuery(params: Record<string, string | number | undefined>) {
  const query = new URLSearchParams();

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== "") {
      query.set(key, String(value));
    }
  });

  const asString = query.toString();
  return asString ? `?${asString}` : "";
}

export const ticketsApi = {
  getMyTickets: (params?: { page?: number; size?: number; status?: TicketStatus; q?: string }) =>
    authClient.get<PaginatedResponse<TicketSummary>>(`/api/tickets/me${buildQuery(params ?? {})}`),

  getQueueTickets: (params?: { page?: number; size?: number; status?: TicketStatus; q?: string; scope?: TicketQueueScope }) =>
    authClient.get<PaginatedResponse<TicketSummary>>(`/api/tickets${buildQuery(params ?? {})}`),
};
