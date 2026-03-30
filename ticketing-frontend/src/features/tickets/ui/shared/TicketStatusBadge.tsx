import { Tag } from "antd";
import { ticketStatusColor, ticketStatusLabel } from "../../model/presentation";
import type { TicketStatus } from "../../model/types";

type TicketStatusBadgeProps = {
  status: TicketStatus;
};

export function TicketStatusBadge({ status }: TicketStatusBadgeProps) {
  return (
    <Tag color={ticketStatusColor[status]} data-testid={`ticket-status-${status}`}>
      {ticketStatusLabel[status]}
    </Tag>
  );
}
