import { render, screen } from "@testing-library/react";
import { ConfigProvider } from "antd";
import { vi } from "vitest";
import { AgentAdminTicketsPage } from "./AgentAdminTicketsPage";

const getQueueTicketsMock = vi.fn();

vi.mock("../api/ticketsApi", () => ({
  ticketsApi: {
    getQueueTickets: () => getQueueTicketsMock(),
  },
}));

describe("AgentAdminTicketsPage", () => {
  it("renders queue controls and loaded table state", async () => {
    getQueueTicketsMock.mockResolvedValueOnce({ items: [], page: 0, size: 20, total: 0 });

    render(
      <ConfigProvider>
        <AgentAdminTicketsPage />
      </ConfigProvider>,
    );

    expect(screen.getByRole("heading", { name: "Gestión de tickets" })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Sin asignar" })).toBeInTheDocument();
    expect(await screen.findByText("No hay tickets para mostrar con los filtros actuales")).toBeInTheDocument();
  });
});
