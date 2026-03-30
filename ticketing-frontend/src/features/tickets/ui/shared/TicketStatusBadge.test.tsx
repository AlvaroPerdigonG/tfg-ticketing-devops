import { screen } from "@testing-library/react";
import { renderWithProviders } from "src/test/utils/renderWithProviders";
import { TicketStatusBadge } from "./TicketStatusBadge";

describe("TicketStatusBadge", () => {
  it("renderiza el texto de estado en español para estados críticos", () => {
    const { rerender } = renderWithProviders(<TicketStatusBadge status="OPEN" />);
    expect(screen.getByText("Abierto")).toBeInTheDocument();

    rerender(<TicketStatusBadge status="IN_PROGRESS" />);
    expect(screen.getByText("En progreso")).toBeInTheDocument();

    rerender(<TicketStatusBadge status="ON_HOLD" />);
    expect(screen.getByText("En espera")).toBeInTheDocument();

    rerender(<TicketStatusBadge status="RESOLVED" />);
    expect(screen.getByText("Resuelto")).toBeInTheDocument();
  });
});
