import { screen, waitFor } from "@testing-library/react";
import { renderWithProviders } from "src/test/utils/renderWithProviders";
import { jsonResponse } from "src/test/msw/handlers";
import { http } from "src/test/msw/http";
import { server } from "src/test/msw/server";
import { AgentAdminTicketsPage } from "./AgentAdminTicketsPage";

const queueTicket = {
  id: "TCK-200",
  title: "Impresora bloqueada",
  status: "IN_PROGRESS",
  priority: "MEDIUM",
  createdAt: "2026-03-01T10:00:00.000Z",
  updatedAt: "2026-03-02T12:00:00.000Z",
  createdByUserId: "user-77",
  assignedToUserId: "agent-1",
} as const;

describe("AgentAdminTicketsPage", () => {
  it("[TICKET-AGENT-03] muestra tickets gestionables para agent/admin", async () => {
    server.use(
      http.get("/api/tickets", () =>
        jsonResponse({
          items: [queueTicket],
          page: 0,
          size: 20,
          total: 1,
        }),
      ),
    );

    renderWithProviders(<AgentAdminTicketsPage />, { router: { initialEntries: ["/tickets"] } });

    expect(await screen.findByRole("cell", { name: "TCK-200" })).toBeInTheDocument();
    expect(screen.getByRole("cell", { name: "Impresora bloqueada" })).toBeInTheDocument();
  });

  it("lee filtros desde query params y los aplica al backend", async () => {
    let capturedSearch = "";

    server.use(
      http.get("/api/tickets", (request) => {
        capturedSearch = request.url.search;
        return jsonResponse({ items: [], page: 0, size: 20, total: 0 });
      }),
    );

    renderWithProviders(<AgentAdminTicketsPage />, {
      router: { initialEntries: ["/tickets?view=mine&status=IN_PROGRESS&q=printer"] },
    });

    await waitFor(() => {
      expect(capturedSearch).toContain("scope=MINE");
      expect(capturedSearch).toContain("status=IN_PROGRESS");
      expect(capturedSearch).toContain("q=printer");
    });

    expect(screen.getByRole("heading", { name: "Tickets assigned to me" })).toBeInTheDocument();
  });

  it("muestra loading y empty state", async () => {
    server.use(
      http.get("/api/tickets", async () => {
        await new Promise((resolve) => setTimeout(resolve, 120));
        return jsonResponse({ items: [], page: 0, size: 20, total: 0 });
      }),
    );

    const { container } = renderWithProviders(<AgentAdminTicketsPage />, {
      router: { initialEntries: ["/tickets"] },
    });

    expect(container.querySelector(".ant-skeleton")).toBeInTheDocument();
    expect(
      await screen.findByText("No tickets to display with the current filters"),
    ).toBeInTheDocument();
  });

  it("muestra error state cuando falla la cola", async () => {
    server.use(http.get("/api/tickets", () => jsonResponse({ message: "fail" }, { status: 500 })));

    renderWithProviders(<AgentAdminTicketsPage />, { router: { initialEntries: ["/tickets"] } });

    expect(await screen.findByText("Could not load the ticket queue")).toBeInTheDocument();
  });

  it("[TICKET-AGENT-01] muestra acciones de UI para gestión de estado si existen", async () => {
    server.use(
      http.get("/api/tickets", () =>
        jsonResponse({
          items: [queueTicket],
          page: 0,
          size: 20,
          total: 1,
        }),
      ),
    );

    renderWithProviders(<AgentAdminTicketsPage />, { router: { initialEntries: ["/tickets"] } });

    await screen.findByRole("cell", { name: "TCK-200" });

    // En esta pantalla actualmente solo existe acción "View".
    expect(screen.getByRole("button", { name: "View" })).toBeInTheDocument();
  });
});
