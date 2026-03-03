import { PlaceholderPage } from "./PlaceholderPage";
import { useAuth } from "../../features/auth/hooks/useAuth";
import { UserTicketsHomePage } from "../../features/tickets/ui/UserTicketsHomePage";

export function TicketsPage() {
  const { hasAnyRole } = useAuth();

  if (hasAnyRole(["AGENT", "ADMIN"])) {
    return <PlaceholderPage title="Tickets" />;
  }

  return <UserTicketsHomePage />;
}
