import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { createMemoryRouter, RouterProvider } from "react-router-dom";
import { vi } from "vitest";
import { AppShell } from "./AppShell";
import { renderWithProviders } from "src/test/utils/renderWithProviders";

const hasRoleMock = vi.fn<(role: "USER" | "AGENT" | "ADMIN") => boolean>();
const logoutMock = vi.fn();
const authStateMock = {
  user: {
    displayName: "Ada Lovelace",
  },
};

vi.mock("../../features/auth/hooks/useAuth", () => ({
  useAuth: () => ({
    state: authStateMock,
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

  it("renders sidebar, header and page content", async () => {
    hasRoleMock.mockReturnValue(false);
    renderWithRouter();

    expect(await screen.findByText("TFG Ticketing")).toBeInTheDocument();
    expect(screen.getByText("Ada Lovelace")).toBeInTheDocument();
    expect(screen.getByText("Ticketing Platform")).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "Dashboard page" })).toBeInTheDocument();
  });

  it("navigates from sidebar links", async () => {
    hasRoleMock.mockReturnValue(false);
    const user = userEvent.setup();
    renderWithRouter();

    await user.click(await screen.findByText("Tickets"));

    expect(screen.getByRole("heading", { name: "Tickets page" })).toBeInTheDocument();
  });

  it("hides admin menu for USER", async () => {
    hasRoleMock.mockReturnValue(false);
    renderWithRouter();

    await screen.findByRole("heading", { name: "Dashboard page" });
    expect(screen.queryByText("Administration")).not.toBeInTheDocument();
  });

  it("shows admin menu for ADMIN", async () => {
    hasRoleMock.mockReturnValue(true);
    renderWithRouter();

    expect(await screen.findByText("Administration")).toBeInTheDocument();
  });

  it("shows dashboard menu for AGENT but not admin menu", async () => {
    hasRoleMock.mockImplementation((role) => role === "AGENT");
    renderWithRouter();

    expect(await screen.findByText("Dashboard")).toBeInTheDocument();
    expect(screen.queryByText("Administration")).not.toBeInTheDocument();
  });

  it("logs out and redirects to login from header button", async () => {
    hasRoleMock.mockReturnValue(false);
    const user = userEvent.setup();
    renderWithRouter();

    await user.click(await screen.findByRole("button", { name: "Logout" }));

    expect(logoutMock).toHaveBeenCalledTimes(1);
    expect(await screen.findByRole("heading", { name: "Login page" })).toBeInTheDocument();
  });
});
