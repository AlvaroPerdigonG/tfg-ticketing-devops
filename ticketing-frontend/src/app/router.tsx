import { createBrowserRouter } from "react-router-dom";
import { LoginPage } from "../features/auth/ui/LoginPage";
import { ProtectedRoute } from "../features/auth/ui/ProtectedRoute";

function TicketsPlaceholder() {
  return <div style={{ padding: 16 }}>Tickets (placeholder)</div>;
}

function ForbiddenPage() {
  return <div style={{ padding: 16 }}>403 — Forbidden</div>;
}

export const router = createBrowserRouter([
  { path: "/login", element: <LoginPage /> },
  { path: "/forbidden", element: <ForbiddenPage /> },

  {
    element: <ProtectedRoute />,
    children: [
      { path: "/", element: <TicketsPlaceholder /> },
      { path: "/tickets", element: <TicketsPlaceholder /> },
    ],
  },
]);