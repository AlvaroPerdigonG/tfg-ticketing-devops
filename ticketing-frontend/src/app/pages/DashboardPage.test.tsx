import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { vi } from "vitest";
import { DashboardPage } from "./DashboardPage";

const hasAnyRoleMock = vi.fn<(roles: Array<"USER" | "AGENT" | "ADMIN">) => boolean>();

vi.mock("../../features/auth/hooks/useAuth", () => ({
  useAuth: () => ({
    hasAnyRole: hasAnyRoleMock,
  }),
}));

vi.mock("../../features/tickets/ui/AgentAdminDashboardPage", () => ({
  AgentAdminDashboardPage: () => <h1>Dashboard agente</h1>,
}));

describe("DashboardPage", () => {
  beforeEach(() => {
    hasAnyRoleMock.mockReset();
  });

  it("shows dashboard for agents and admins", () => {
    hasAnyRoleMock.mockReturnValue(true);

    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>,
    );

    expect(screen.getByRole("heading", { name: "Dashboard agente" })).toBeInTheDocument();
  });

  it("shows placeholder for other roles", () => {
    hasAnyRoleMock.mockReturnValue(false);

    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>,
    );

    expect(screen.getByRole("heading", { name: "Dashboard no disponible para tu rol" })).toBeInTheDocument();
  });
});
