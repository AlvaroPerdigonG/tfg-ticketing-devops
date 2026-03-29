import { screen } from "@testing-library/react";
import { renderWithProviders } from "src/test/utils/renderWithProviders";
import { http } from "src/test/msw/http";
import { jsonResponse } from "src/test/msw/handlers";
import { server } from "src/test/msw/server";
import { UserTicketsHomePage } from "./UserTicketsHomePage";

const baseTicket = {
  id: "TCK-001",
  title: "No funciona la VPN",
  status: "OPEN",
  priority: "HIGH",
  createdAt: "2026-03-01T10:00:00.000Z",
  updatedAt: "2026-03-01T10:00:00.000Z",
  createdByUserId: "user-1",
  assignedToUserId: null,
} as const;

describe("UserTicketsHomePage", () => {
  it("[TICKET-USER-03] muestra solo los tickets del usuario autenticado", async () => {
    server.use(
      http.get("/api/tickets/me", () =>
        jsonResponse({
          items: [baseTicket],
          page: 0,
          size: 20,
          total: 1,
        }),
      ),
    );

    renderWithProviders(<UserTicketsHomePage />, { router: {} });

    expect(await screen.findByRole("cell", { name: "TCK-001" })).toBeInTheDocument();
    expect(screen.getByRole("cell", { name: "No funciona la VPN" })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Ver" })).toBeInTheDocument();
  });

  it("muestra loading state mientras la petición está en curso", async () => {
    server.use(
      http.get("/api/tickets/me", async () => {
        await new Promise((resolve) => setTimeout(resolve, 120));
        return jsonResponse({ items: [], page: 0, size: 20, total: 0 });
      }),
    );

    const { container } = renderWithProviders(<UserTicketsHomePage />, { router: {} });

    expect(container.querySelector(".ant-skeleton")).toBeInTheDocument();
    expect(await screen.findByText("Aún no tienes tickets creados")).toBeInTheDocument();
  });

  it("muestra empty state cuando el usuario no tiene tickets", async () => {
    server.use(
      http.get("/api/tickets/me", () => jsonResponse({ items: [], page: 0, size: 20, total: 0 })),
    );

    renderWithProviders(<UserTicketsHomePage />, { router: {} });

    expect(await screen.findByText("Aún no tienes tickets creados")).toBeInTheDocument();
  });

  it("muestra error state cuando falla la carga", async () => {
    server.use(http.get("/api/tickets/me", () => jsonResponse({ message: "boom" }, { status: 500 })));

    renderWithProviders(<UserTicketsHomePage />, { router: {} });

    expect(await screen.findByText("No hemos podido cargar tus tickets")).toBeInTheDocument();
  });

  it("[TICKET-USER-05] no muestra acciones de cambio de estado", async () => {
    server.use(
      http.get("/api/tickets/me", () =>
        jsonResponse({
          items: [baseTicket],
          page: 0,
          size: 20,
          total: 1,
        }),
      ),
    );

    renderWithProviders(<UserTicketsHomePage />, { router: {} });

    await screen.findByRole("cell", { name: "TCK-001" });
    expect(screen.queryByRole("button", { name: /cambiar estado/i })).not.toBeInTheDocument();
    expect(screen.queryByRole("button", { name: /resolver/i })).not.toBeInTheDocument();
  });
});
