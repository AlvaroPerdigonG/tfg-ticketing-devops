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
  UserTicketsHomePage: () => <h1>My tickets</h1>,
}));

vi.mock("src/features/tickets/ui/ticketsPage/AgentAdminTicketsPage", () => ({
  AgentAdminTicketsPage: () => <h1>Ticket management</h1>,
}));

describe("TicketsPage", () => {
  beforeEach(() => {
    hasAnyRoleMock.mockReset();
  });

  it("renders USER view when the user does not have agent/admin role", () => {
    hasAnyRoleMock.mockReturnValue(false);

    renderWithProviders(<TicketsPage />, { router: {} });

    expect(screen.getByRole("heading", { name: "My tickets" })).toBeInTheDocument();
  });

  it("renders management view when the user has AGENT/ADMIN role", () => {
    hasAnyRoleMock.mockReturnValue(true);

    renderWithProviders(<TicketsPage />, { router: {} });

    expect(screen.getByRole("heading", { name: "Ticket management" })).toBeInTheDocument();
  });
});
