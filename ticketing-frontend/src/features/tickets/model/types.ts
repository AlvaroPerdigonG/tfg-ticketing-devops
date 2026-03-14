export type TicketStatus = "OPEN" | "IN_PROGRESS" | "ON_HOLD" | "RESOLVED";
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

export type TicketDetail = {
  id: string;
  title: string;
  description: string;
  status: TicketStatus;
  priority: TicketPriority;
  createdAt: string;
  updatedAt: string;
  createdByUserId: string;
  assignedToUserId: string | null;
  categoryId: string;
  timeline: TimelineEntry[];
  availableTransitions: TicketStatus[];
};

export type TicketEventType = "TICKET_CREATED" | "STATUS_CHANGED" | "ASSIGNED_TO_ME" | "COMMENT_ADDED";

export type TimelineEntry = {
  id: string;
  kind: "MESSAGE" | "EVENT";
  createdAt: string;
  actorUserId: string | null;
  content: string | null;
  eventType: TicketEventType | null;
  payload: Record<string, string>;
};

export type AddTicketCommentResponse = {
  id: string;
  ticketId: string;
  authorUserId: string;
  content: string;
  createdAt: string;
};

export type TicketCategory = {
  id: string;
  name: string;
};

export type CreateTicketInput = {
  title: string;
  description: string;
  categoryId: string;
  priority: TicketPriority;
};

export type CreateTicketResponse = {
  ticketId: string;
};

export type PaginatedResponse<T> = {
  items: T[];
  page: number;
  size: number;
  total: number;
};
