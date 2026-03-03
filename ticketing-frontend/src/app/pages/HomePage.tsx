import { PlaceholderPage } from "./PlaceholderPage";
import { useAuth } from "../../features/auth/hooks/useAuth";
import { UserTicketsHomePage } from "../../features/tickets/ui/UserTicketsHomePage";

export function HomePage() {
  const { hasAnyRole } = useAuth();

  if (hasAnyRole(["AGENT", "ADMIN"])) {
    return <PlaceholderPage title="Dashboard" />;
  }

  return <UserTicketsHomePage />;
}
