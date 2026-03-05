import { createBrowserRouter } from "react-router-dom";
import { ProtectedRoute } from "../features/auth/ui/ProtectedRoute";
import { LoginPage } from "../features/auth/ui/LoginPage";
import { AppShell } from "./layout/AppShell";
import { NotFoundPage } from "./pages/NotFoundPage";
import { PlaceholderPage } from "./pages/PlaceholderPage";
import { RoleLandingRedirect } from "./pages/RoleLandingRedirect";
import { TicketsPage } from "./pages/TicketsPage";
import { CreateTicketPage } from "../features/tickets/ui/CreateTicketPage";
import { TicketDetailPage } from "../features/tickets/ui/TicketDetailPage";
import { ProfilePage } from "./pages/ProfilePage";
import { RequireRole } from "../features/auth/ui/RequireRole";
import { AdminPage } from "../features/admin/ui/AdminPage";

function ForbiddenPage() {
  return <PlaceholderPage title="403 — Forbidden" />;
}

export const router = createBrowserRouter([
  { path: "/login", element: <LoginPage /> },
  { path: "/forbidden", element: <ForbiddenPage /> },
  {
    element: <ProtectedRoute />,
    children: [
      {
        element: <AppShell />,
        children: [
          { path: "/", element: <RoleLandingRedirect /> },
          { path: "/dashboard", element: <PlaceholderPage title="Dashboard" /> },
          { path: "/tickets", element: <TicketsPage /> },
          { path: "/tickets/new", element: <CreateTicketPage /> },
          { path: "/tickets/:id", element: <TicketDetailPage /> },
          { path: "/profile", element: <ProfilePage /> },
          { path: "/admin", element: <RequireRole anyOf={["ADMIN"]}><AdminPage /></RequireRole> },
        ],
      },
    ],
  },
  { path: "*", element: <NotFoundPage /> },
]);
