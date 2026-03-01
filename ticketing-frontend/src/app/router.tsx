import { createBrowserRouter } from "react-router-dom";
import { ProtectedRoute } from "../features/auth/ui/ProtectedRoute";
import { LoginPage } from "../features/auth/ui/LoginPage";
import { AppShell } from "./layout/AppShell";
import { NotFoundPage } from "./pages/NotFoundPage";
import { PlaceholderPage } from "./pages/PlaceholderPage";

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
          { path: "/", element: <PlaceholderPage title="Dashboard" /> },
          { path: "/dashboard", element: <PlaceholderPage title="Dashboard" /> },
          { path: "/tickets", element: <PlaceholderPage title="Tickets" /> },
          { path: "/tickets/new", element: <PlaceholderPage title="Nuevo ticket" /> },
          { path: "/profile", element: <PlaceholderPage title="Perfil" /> },
          { path: "/admin", element: <PlaceholderPage title="Administración" /> },
        ],
      },
    ],
  },
  { path: "*", element: <NotFoundPage /> },
]);
