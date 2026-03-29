import { fireEvent, screen, waitFor } from "@testing-library/react";
import { renderWithProviders } from "src/test/utils/renderWithProviders";
import userEvent from "@testing-library/user-event";
import { vi } from "vitest";
import { CreateTicketPage } from "./CreateTicketPage";

const getCategoriesMock = vi.fn();
const createTicketMock = vi.fn();
const navigateMock = vi.fn();

vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual<typeof import("react-router-dom")>("react-router-dom");
  return {
    ...actual,
    useNavigate: () => navigateMock,
  };
});

vi.mock("../../api/ticketsApi", () => ({
  ticketsApi: {
    getCategories: () => getCategoriesMock(),
    createTicket: (payload: unknown) => createTicketMock(payload),
  },
}));

describe("CreateTicketPage", () => {
  beforeEach(() => {
    getCategoriesMock.mockReset();
    createTicketMock.mockReset();
    navigateMock.mockReset();
  });

  it("keeps submit disabled while required fields are empty", async () => {
    getCategoriesMock.mockResolvedValueOnce([{ id: "cat-1", name: "General" }]);

    renderWithProviders(<CreateTicketPage />, { router: {} });

    const submitButton = await screen.findByRole("button", { name: "Crear ticket" });
    expect(submitButton).toBeDisabled();
  });

  it("submits form and redirects to ticket detail", async () => {
    const user = userEvent.setup();

    getCategoriesMock.mockResolvedValueOnce([{ id: "cat-1", name: "General" }]);
    createTicketMock.mockResolvedValueOnce({ ticketId: "t-100" });

    renderWithProviders(<CreateTicketPage />, { router: {} });

    await user.type(await screen.findByLabelText("Título"), "Impresora bloqueada");
    await user.type(screen.getByLabelText("Descripción"), "Da error E23 cuando intento imprimir un PDF.");
    const categoryCombobox = screen.getByRole("combobox", { name: "Categoría" });
    fireEvent.mouseDown(categoryCombobox);
    await user.click(await screen.findByText("General"));

    await waitFor(() => {
      expect(screen.getByRole("button", { name: "Crear ticket" })).toBeEnabled();
    });

    await user.click(screen.getByRole("button", { name: "Crear ticket" }));

    expect(createTicketMock).toHaveBeenCalledWith({
      title: "Impresora bloqueada",
      description: "Da error E23 cuando intento imprimir un PDF.",
      categoryId: "cat-1",
      priority: "MEDIUM",
    });
    expect(navigateMock).toHaveBeenCalledWith("/tickets/t-100");
  });
});
