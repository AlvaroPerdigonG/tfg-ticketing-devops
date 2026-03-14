import type { TicketPriority, TicketStatus } from "./types";

export const ticketStatusLabel: Record<TicketStatus, string> = {
  OPEN: "Abierto",
  IN_PROGRESS: "En progreso",
  ON_HOLD: "En espera",
  RESOLVED: "Resuelto",
};

export const ticketStatusColor: Record<TicketStatus, string> = {
  OPEN: "default",
  IN_PROGRESS: "processing",
  ON_HOLD: "warning",
  RESOLVED: "success",
};

export const ticketPriorityLabel: Record<TicketPriority, string> = {
  LOW: "Baja",
  MEDIUM: "Media",
  HIGH: "Alta",
};
