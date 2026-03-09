import { render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { vi } from "vitest";
import { AgentAdminTicketsPage } from "./AgentAdminTicketsPage";

const getQueueTicketsMock = vi.fn();

vi.mock("../api/ticketsApi", () => ({
  ticketsApi: {
    getQueueTickets: (...args: unknown[]) => getQueueTicketsMock(...args),
  },
}));

describe("AgentAdminTicketsPage", () => {
  beforeEach(() => {
    getQueueTicketsMock.mockReset();
    getQueueTicketsMock.mockResolvedValue({
      items: [],
      page: 0,
      size: 20,
      total: 0,
    });
  });

  it("reads queue filters from url params", async () => {
    render(
      <MemoryRouter initialEntries={["/tickets?view=mine&status=IN_PROGRESS&q=printer"]}>
        <Routes>
          <Route path="/tickets" element={<AgentAdminTicketsPage />} />
        </Routes>
      </MemoryRouter>,
    );

    await waitFor(() => {
      expect(getQueueTicketsMock).toHaveBeenCalledWith({
        scope: "MINE",
        status: "IN_PROGRESS",
        q: "printer",
        page: 0,
        size: 20,
      });
    });

    expect(screen.getByRole("heading", { name: "Tickets asignados a mí" })).toBeInTheDocument();
  });
});
