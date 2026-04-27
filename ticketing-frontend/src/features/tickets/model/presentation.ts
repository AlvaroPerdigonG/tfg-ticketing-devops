import type { TicketPriority, TicketStatus } from "./types";

export const ticketStatusLabel: Record<TicketStatus, string> = {
  OPEN: "Open",
  IN_PROGRESS: "In progress",
  ON_HOLD: "On hold",
  RESOLVED: "Resolved",
};

export const ticketStatusColor: Record<TicketStatus, string> = {
  OPEN: "default",
  IN_PROGRESS: "processing",
  ON_HOLD: "warning",
  RESOLVED: "success",
};

export const ticketPriorityLabel: Record<TicketPriority, string> = {
  LOW: "Low",
  MEDIUM: "Medium",
  HIGH: "High",
};
