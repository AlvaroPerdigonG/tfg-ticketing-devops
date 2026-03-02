import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { ConfigProvider } from "antd";
import { createMemoryRouter, RouterProvider } from "react-router-dom";
import { vi } from "vitest";
import { AppShell } from "./AppShell";

const hasRoleMock = vi.fn<(role: "USER" | "AGENT" | "ADMIN") => boolean>();

vi.mock("../../features/auth/hooks/useAuth", () => ({
  useAuth: () => ({
    hasRole: hasRoleMock,
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
        ],
      },
    ],
    { initialEntries },
  );

  return render(
    <ConfigProvider>
      <RouterProvider router={router} />
    </ConfigProvider>,
  );
}

describe("AppShell", () => {
  beforeEach(() => {
    hasRoleMock.mockReset();
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
});
