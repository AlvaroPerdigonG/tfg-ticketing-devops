import { createApiClient } from "../../../shared/api/client";
import type {
  CreateTicketInput,
  AddTicketCommentResponse,
  CreateTicketResponse,
  PaginatedResponse,
  TicketCategory,
  TicketDetail,
  TicketQueueScope,
  TicketStatus,
  TicketSummary,
} from "../model/types";

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

  getCategories: () => authClient.get<TicketCategory[]>("/api/categories"),

  createTicket: (payload: CreateTicketInput) => authClient.post<CreateTicketResponse>("/api/tickets", payload),

  getTicketById: (ticketId: string) => authClient.get<TicketDetail>(`/api/tickets/${ticketId}`),

  changeStatus: (ticketId: string, status: TicketStatus) =>
    authClient.patch<void>(`/api/tickets/${ticketId}/status`, { status }),

  assignToMe: (ticketId: string) => authClient.patch<void>(`/api/tickets/${ticketId}/assignment/me`, {}),

  addComment: (ticketId: string, content: string) =>
    authClient.post<AddTicketCommentResponse>(`/api/tickets/${ticketId}/comments`, { content }),
};
