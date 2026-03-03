import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { vi } from "vitest";
import { HomePage } from "./HomePage";

const hasAnyRoleMock = vi.fn<(roles: Array<"USER" | "AGENT" | "ADMIN">) => boolean>();

vi.mock("../../features/auth/hooks/useAuth", () => ({
  useAuth: () => ({
    hasAnyRole: hasAnyRoleMock,
  }),
}));

vi.mock("../../features/tickets/ui/UserTicketsHomePage", () => ({
  UserTicketsHomePage: () => <h1>Mis tickets</h1>,
}));

describe("HomePage", () => {
  beforeEach(() => {
    hasAnyRoleMock.mockReset();
  });

  it("shows USER home for normal users", () => {
    hasAnyRoleMock.mockReturnValue(false);

    render(
      <MemoryRouter>
        <HomePage />
      </MemoryRouter>,
    );

    expect(screen.getByRole("heading", { name: "Mis tickets" })).toBeInTheDocument();
  });

  it("shows dashboard home for AGENT/ADMIN", () => {
    hasAnyRoleMock.mockReturnValue(true);

    render(
      <MemoryRouter>
        <HomePage />
      </MemoryRouter>,
    );

    expect(screen.getByRole("heading", { name: "Dashboard" })).toBeInTheDocument();
  });
});
