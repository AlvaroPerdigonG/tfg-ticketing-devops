import { render, screen } from "@testing-library/react";
import { createMemoryRouter, RouterProvider } from "react-router-dom";
import { vi } from "vitest";
import { RoleLandingRedirect } from "./RoleLandingRedirect";

const hasAnyRoleMock = vi.fn<(roles: Array<"USER" | "AGENT" | "ADMIN">) => boolean>();

vi.mock("../../features/auth/hooks/useAuth", () => ({
  useAuth: () => ({
    hasAnyRole: hasAnyRoleMock,
  }),
}));

function renderRoute() {
  const router = createMemoryRouter(
    [
      { path: "/", element: <RoleLandingRedirect /> },
      { path: "/tickets", element: <h1>Tickets</h1> },
      { path: "/dashboard", element: <h1>Dashboard</h1> },
    ],
    { initialEntries: ["/"] },
  );

  return render(<RouterProvider router={router} />);
}

describe("RoleLandingRedirect", () => {
  beforeEach(() => {
    hasAnyRoleMock.mockReset();
  });

  it("redirects USER to /tickets", async () => {
    hasAnyRoleMock.mockReturnValue(false);
    renderRoute();

    expect(await screen.findByRole("heading", { name: "Tickets" })).toBeInTheDocument();
  });

  it("redirects AGENT/ADMIN to /dashboard", async () => {
    hasAnyRoleMock.mockReturnValue(true);
    renderRoute();

    expect(await screen.findByRole("heading", { name: "Dashboard" })).toBeInTheDocument();
  });
});
