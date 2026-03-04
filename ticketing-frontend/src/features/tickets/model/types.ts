export type TicketStatus = "OPEN" | "IN_PROGRESS" | "RESOLVED";
export type TicketPriority = "LOW" | "MEDIUM" | "HIGH";
export type TicketQueueScope = "UNASSIGNED" | "MINE" | "OTHERS" | "ALL";

export type TicketSummary = {
  id: string;
  title: string;
  status: TicketStatus;
  priority: TicketPriority;
  createdAt: string;
  updatedAt: string;
  createdByUserId: string;
  assignedToUserId: string | null;
};

export type PaginatedResponse<T> = {
  items: T[];
  page: number;
  size: number;
  total: number;
};
