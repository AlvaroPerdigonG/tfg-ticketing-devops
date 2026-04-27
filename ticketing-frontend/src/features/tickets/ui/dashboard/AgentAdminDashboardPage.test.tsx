import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { createMemoryRouter, RouterProvider } from "react-router-dom";
import { renderWithProviders } from "src/test/utils/renderWithProviders";
import { jsonResponse } from "src/test/msw/handlers";
import { http } from "src/test/msw/http";
import { server } from "src/test/msw/server";
import { AgentAdminDashboardPage } from "./AgentAdminDashboardPage";

const statsFixture = {
  cards: {
    unassigned: 4,
    assignedToMe: 6,
    inProgress: 3,
    onHold: 2,
  },
  charts: {
    resolvedByAgent: [
      { assigneeUserId: "agent-1", assigneeDisplayName: "Agent Uno", count: 9 },
      { assigneeUserId: "admin-1", assigneeDisplayName: "Admin Uno", count: 5 },
    ],
    assignedByAgent: [
      { assigneeUserId: "admin-1", assigneeDisplayName: "Admin Uno", count: 11 },
      { assigneeUserId: "agent-1", assigneeDisplayName: "Agent Uno", count: 8 },
    ],
  },
} as const;

function renderPage() {
  const router = createMemoryRouter(
    [
      { path: "/dashboard", element: <AgentAdminDashboardPage /> },
      { path: "/tickets", element: <h1>Tickets destination</h1> },
    ],
    { initialEntries: ["/dashboard"] },
  );

  return { ...renderWithProviders(<RouterProvider router={router} />), router };
}

describe("AgentAdminDashboardPage", () => {
  it("muestra cards y gráficos de estadísticas", async () => {
    server.use(http.get("/api/tickets/dashboard/stats", () => jsonResponse(statsFixture)));

    renderPage();

    expect(await screen.findByText("Unassigned tickets")).toBeInTheDocument();
    expect(screen.getByText("Tickets resolved by agent/admin")).toBeInTheDocument();
    expect(screen.getAllByText("Agent Uno")).toHaveLength(2);
    expect(screen.getAllByText("Admin Uno")).toHaveLength(2);
  });

  it("navega a tickets con filtros al clicar una card", async () => {
    server.use(http.get("/api/tickets/dashboard/stats", () => jsonResponse(statsFixture)));

    const { router } = renderPage();
    const user = userEvent.setup();

    await screen.findByText("WIP (in progress)");

    await user.click(screen.getByRole("button", { name: /WIP \(in progress\)/i }));

    expect(router.state.location.pathname).toBe("/tickets");
    expect(router.state.location.search).toContain("view=all");
    expect(router.state.location.search).toContain("status=IN_PROGRESS");
  });
});
