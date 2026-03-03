import { render, screen } from "@testing-library/react";
import { ConfigProvider } from "antd";
import { AgentAdminTicketsPage } from "./AgentAdminTicketsPage";

describe("AgentAdminTicketsPage", () => {
  it("renders queue controls and backend notice", () => {
    render(
      <ConfigProvider>
        <AgentAdminTicketsPage />
      </ConfigProvider>,
    );

    expect(screen.getByRole("heading", { name: "Gestión de tickets" })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Sin asignar" })).toBeInTheDocument();
    expect(screen.getByText("Panel AGENT/ADMIN preparado")).toBeInTheDocument();
  });
});
