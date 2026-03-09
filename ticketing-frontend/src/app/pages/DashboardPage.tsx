import { useAuth } from "../../features/auth/hooks/useAuth";
import { PlaceholderPage } from "./PlaceholderPage";
import { AgentAdminDashboardPage } from "../../features/tickets/ui/AgentAdminDashboardPage";

export function DashboardPage() {
  const { hasAnyRole } = useAuth();

  if (hasAnyRole(["AGENT", "ADMIN"])) {
    return <AgentAdminDashboardPage />;
  }

  return <PlaceholderPage title="Dashboard no disponible para tu rol" />;
}
