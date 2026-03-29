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

    renderWithProviders(<TicketsPage />, { router: {} });

    expect(screen.getByRole("heading", { name: "Mis tickets" })).toBeInTheDocument();
  });

  it("shows AGENT/ADMIN tickets page", () => {
    hasAnyRoleMock.mockReturnValue(true);

    renderWithProviders(<TicketsPage />, { router: {} });

    expect(screen.getByRole("heading", { name: "Gestión de tickets" })).toBeInTheDocument();
  });
});
