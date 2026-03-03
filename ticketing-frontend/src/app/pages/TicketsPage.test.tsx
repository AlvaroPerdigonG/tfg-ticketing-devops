import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { vi } from "vitest";
import { TicketsPage } from "./TicketsPage";

const hasAnyRoleMock = vi.fn<(roles: Array<"USER" | "AGENT" | "ADMIN">) => boolean>();

vi.mock("../../features/auth/hooks/useAuth", () => ({
  useAuth: () => ({
    hasAnyRole: hasAnyRoleMock,
  }),
}));

vi.mock("../../features/tickets/ui/UserTicketsHomePage", () => ({
  UserTicketsHomePage: () => <h1>Mis tickets</h1>,
}));

vi.mock("../../features/tickets/ui/AgentAdminTicketsPage", () => ({
  AgentAdminTicketsPage: () => <h1>Gestión de tickets</h1>,
}));

describe("TicketsPage", () => {
  beforeEach(() => {
    hasAnyRoleMock.mockReset();
  });

  it("shows USER tickets page for normal users", () => {
    hasAnyRoleMock.mockReturnValue(false);

    render(
      <MemoryRouter>
        <TicketsPage />
      </MemoryRouter>,
    );

    expect(screen.getByRole("heading", { name: "Mis tickets" })).toBeInTheDocument();
  });

  it("shows AGENT/ADMIN tickets page", () => {
    hasAnyRoleMock.mockReturnValue(true);

    render(
      <MemoryRouter>
        <TicketsPage />
      </MemoryRouter>,
    );

    expect(screen.getByRole("heading", { name: "Gestión de tickets" })).toBeInTheDocument();
  });
});
