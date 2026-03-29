import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { createMemoryRouter, RouterProvider } from "react-router-dom";
import { vi } from "vitest";
import { AppShell } from "./AppShell";
import { renderWithProviders } from "src/test/utils/renderWithProviders";

const hasRoleMock = vi.fn<(role: "USER" | "AGENT" | "ADMIN") => boolean>();
const logoutMock = vi.fn();

vi.mock("../../features/auth/hooks/useAuth", () => ({
  useAuth: () => ({
    hasRole: hasRoleMock,
    logout: logoutMock,
  }),
}));

function renderWithRouter(initialEntries: string[] = ["/dashboard"]) {
  const router = createMemoryRouter(
    [
      {
        element: <AppShell />,
        children: [
          { path: "/dashboard", element: <h1>Dashboard page</h1> },
          { path: "/tickets", element: <h1>Tickets page</h1> },
          { path: "/tickets/new", element: <h1>Nuevo ticket page</h1> },
          { path: "/login", element: <h1>Login page</h1> },
        ],
      },
    ],
    { initialEntries },
  );

  return renderWithProviders(<RouterProvider router={router} />);
}

describe("AppShell", () => {
  beforeEach(() => {
    hasRoleMock.mockReset();
    logoutMock.mockReset();
  });

  it("renders sidebar, header and page content", () => {
    hasRoleMock.mockReturnValue(false);
    renderWithRouter();

    expect(screen.getByText("TFG Ticketing")).toBeInTheDocument();
    expect(screen.getByText("Ticketing Platform")).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "Dashboard page" })).toBeInTheDocument();
  });

  it("navigates from sidebar links", async () => {
    hasRoleMock.mockReturnValue(false);
    const user = userEvent.setup();
    renderWithRouter();

    await user.click(screen.getByText("Tickets"));

    expect(screen.getByRole("heading", { name: "Tickets page" })).toBeInTheDocument();
  });

  it("hides admin menu for USER", () => {
    hasRoleMock.mockReturnValue(false);
    renderWithRouter();

    expect(screen.queryByText("Administración")).not.toBeInTheDocument();
  });

  it("shows admin menu for ADMIN", () => {
    hasRoleMock.mockReturnValue(true);
    renderWithRouter();

    expect(screen.getByText("Administración")).toBeInTheDocument();
  });

  it("logs out and redirects to login from header button", async () => {
    hasRoleMock.mockReturnValue(false);
    const user = userEvent.setup();
    renderWithRouter();

    await user.click(screen.getByRole("button", { name: "Logout" }));

    expect(logoutMock).toHaveBeenCalledTimes(1);
    expect(screen.getByRole("heading", { name: "Login page" })).toBeInTheDocument();
  });
});
