import { createApiClient } from "../../../shared/api/client";
import type { TicketSummary } from "../model/types";

const AUTH_TOKEN_STORAGE_KEY = "ticketing_access_token";

const authClient = createApiClient({
  getToken: () => localStorage.getItem(AUTH_TOKEN_STORAGE_KEY),
});

export const ticketsApi = {
  // TODO: Backend currently only documents POST /api/tickets and PATCH /api/tickets/{id}/status.
  // Keep this GET wired to /api/tickets so the UI can consume it as soon as listing is exposed.
  getMyTickets: () => authClient.get<TicketSummary[]>("/api/tickets"),
};
