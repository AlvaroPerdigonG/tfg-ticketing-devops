import { Navigate } from "react-router-dom";
import { useAuth } from "../../features/auth/hooks/useAuth";

export function RoleLandingRedirect() {
  const { hasAnyRole } = useAuth();

  if (hasAnyRole(["AGENT", "ADMIN"])) {
    return <Navigate to="/dashboard" replace />;
  }

  return <Navigate to="/tickets" replace />;
}
