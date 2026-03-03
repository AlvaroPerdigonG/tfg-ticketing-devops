import { useAuth } from "../../features/auth/hooks/useAuth";
import { UserTicketsHomePage } from "../../features/tickets/ui/UserTicketsHomePage";
import { AgentAdminTicketsPage } from "../../features/tickets/ui/AgentAdminTicketsPage";

export function TicketsPage() {
  const { hasAnyRole } = useAuth();

  if (hasAnyRole(["AGENT", "ADMIN"])) {
    return <AgentAdminTicketsPage />;
  }

  return <UserTicketsHomePage />;
}
