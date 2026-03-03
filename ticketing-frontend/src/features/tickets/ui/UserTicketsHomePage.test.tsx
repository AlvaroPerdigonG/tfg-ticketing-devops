import { render, screen } from "@testing-library/react";
import { ConfigProvider } from "antd";
import { MemoryRouter } from "react-router-dom";
import { vi } from "vitest";
import { UserTicketsHomePage } from "./UserTicketsHomePage";

const getMyTicketsMock = vi.fn();

vi.mock("../api/ticketsApi", () => ({
  ticketsApi: {
    getMyTickets: () => getMyTicketsMock(),
  },
}));

describe("UserTicketsHomePage", () => {
  it("renders empty state when user has no tickets", async () => {
    getMyTicketsMock.mockResolvedValueOnce([]);

    render(
      <ConfigProvider>
        <MemoryRouter>
          <UserTicketsHomePage />
        </MemoryRouter>
      </ConfigProvider>,
    );

    expect(await screen.findByText("Aún no tienes tickets creados")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Crear ticket" })).toBeInTheDocument();
  });
});
