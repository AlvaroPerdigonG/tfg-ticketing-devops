import { PlaceholderPage } from "./PlaceholderPage";
import { useAuth } from "../../features/auth/hooks/useAuth";
import { UserTicketsHomePage } from "../../features/tickets/ui/UserTicketsHomePage";

export function HomePage() {
  const { hasRole } = useAuth();

  if (hasRole("USER")) {
    return <UserTicketsHomePage />;
  }

  return <PlaceholderPage title="Dashboard" />;
}
