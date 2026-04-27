import { screen } from "@testing-library/react";
import { renderWithProviders } from "src/test/utils/renderWithProviders";
import { TicketStatusBadge } from "./TicketStatusBadge";

describe("TicketStatusBadge", () => {
  it("renders status text in English for critical states", () => {
    const { rerender } = renderWithProviders(<TicketStatusBadge status="OPEN" />);
    expect(screen.getByText("Open")).toBeInTheDocument();

    rerender(<TicketStatusBadge status="IN_PROGRESS" />);
    expect(screen.getByText("In progress")).toBeInTheDocument();

    rerender(<TicketStatusBadge status="ON_HOLD" />);
    expect(screen.getByText("On hold")).toBeInTheDocument();

    rerender(<TicketStatusBadge status="RESOLVED" />);
    expect(screen.getByText("Resolved")).toBeInTheDocument();
  });
});
