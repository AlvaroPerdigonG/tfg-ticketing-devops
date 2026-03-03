export type TicketStatus = "OPEN" | "IN_PROGRESS" | "RESOLVED";

export type TicketSummary = {
  id: string;
  title: string;
  status: TicketStatus;
  createdAt: string;
};
