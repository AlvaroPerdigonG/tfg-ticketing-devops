import { screen } from "@testing-library/react";
import { renderWithProviders } from "src/test/utils/renderWithProviders";
import { vi } from "vitest";
import { UserTicketsHomePage } from "./UserTicketsHomePage";

const getMyTicketsMock = vi.fn();

vi.mock("../../api/ticketsApi", () => ({
  ticketsApi: {
    getMyTickets: () => getMyTicketsMock(),
  },
}));

describe("UserTicketsHomePage", () => {
  it("renders empty state when user has no tickets", async () => {
    getMyTicketsMock.mockResolvedValueOnce({ items: [], page: 0, size: 20, total: 0 });

    renderWithProviders(<UserTicketsHomePage />, { router: {} });

    expect(await screen.findByText("Aún no tienes tickets creados")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Crear ticket" })).toBeInTheDocument();
  });
});
