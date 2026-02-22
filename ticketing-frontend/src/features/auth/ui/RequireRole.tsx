import React from "react";
import { Navigate } from "react-router-dom";
import type { Role } from "../model/types";
import { useAuth } from "../hooks/useAuth";

export function RequireRole({
  anyOf,
  children,
}: {
  anyOf: Role[];
  children: React.ReactNode;
}) {
  const { hasAnyRole } = useAuth();

  if (!hasAnyRole(anyOf)) {
    return <Navigate to="/forbidden" replace />;
  }

  return <>{children}</>;
}