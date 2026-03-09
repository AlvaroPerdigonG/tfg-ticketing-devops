
import { AgentAdminTicketsPage } from "src/features/tickets/ui/ticketsPage/AgentAdminTicketsPage";
import { useAuth } from "../../features/auth/hooks/useAuth";
import { UserTicketsHomePage } from "src/features/tickets/ui/ticketsPage/UserTicketsHomePage";

export function TicketsPage() {
  const { hasAnyRole } = useAuth();

  if (hasAnyRole(["AGENT", "ADMIN"])) {
    return <AgentAdminTicketsPage />;
  }

  return <UserTicketsHomePage />;
}
