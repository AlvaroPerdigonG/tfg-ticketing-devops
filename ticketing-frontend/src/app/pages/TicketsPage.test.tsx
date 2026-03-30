import { screen } from "@testing-library/react";
import { renderWithProviders } from "src/test/utils/renderWithProviders";
import { vi } from "vitest";
import { TicketsPage } from "./TicketsPage";

const hasAnyRoleMock = vi.fn<(roles: Array<"USER" | "AGENT" | "ADMIN">) => boolean>();

vi.mock("../../features/auth/hooks/useAuth", () => ({
  useAuth: () => ({
    hasAnyRole: hasAnyRoleMock,
  }),
}));

vi.mock("src/features/tickets/ui/ticketsPage/UserTicketsHomePage", () => ({
  UserTicketsHomePage: () => <h1>Mis tickets</h1>,
}));

vi.mock("src/features/tickets/ui/ticketsPage/AgentAdminTicketsPage", () => ({
  AgentAdminTicketsPage: () => <h1>Gestión de tickets</h1>,
}));

describe("TicketsPage", () => {
  beforeEach(() => {
    hasAnyRoleMock.mockReset();
  });

  it("renderiza vista USER cuando no tiene rol agent/admin", () => {
    hasAnyRoleMock.mockReturnValue(false);

    renderWithProviders(<TicketsPage />, { router: {} });

    expect(screen.getByRole("heading", { name: "Mis tickets" })).toBeInTheDocument();
  });

  it("renderiza vista de gestión cuando tiene rol AGENT/ADMIN", () => {
    hasAnyRoleMock.mockReturnValue(true);

    renderWithProviders(<TicketsPage />, { router: {} });

    expect(screen.getByRole("heading", { name: "Gestión de tickets" })).toBeInTheDocument();
  });
});
