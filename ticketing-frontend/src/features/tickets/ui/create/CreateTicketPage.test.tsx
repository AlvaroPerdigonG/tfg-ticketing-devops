import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { ConfigProvider } from "antd";
import { MemoryRouter } from "react-router-dom";
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

    render(
      <ConfigProvider>
        <MemoryRouter>
          <CreateTicketPage />
        </MemoryRouter>
      </ConfigProvider>,
    );

    const submitButton = await screen.findByRole("button", { name: "Crear ticket" });
    expect(submitButton).toBeDisabled();
  });

  it("submits form and redirects to ticket detail", async () => {
    const user = userEvent.setup();

    getCategoriesMock.mockResolvedValueOnce([{ id: "cat-1", name: "General" }]);
    createTicketMock.mockResolvedValueOnce({ ticketId: "t-100" });

    render(
      <ConfigProvider>
        <MemoryRouter>
          <CreateTicketPage />
        </MemoryRouter>
      </ConfigProvider>,
    );

    await user.type(await screen.findByLabelText("Título"), "Impresora bloqueada");
    await user.type(screen.getByLabelText("Descripción"), "Da error E23 cuando intento imprimir un PDF.");
    await user.selectOptions(screen.getByLabelText("Categoría"), "cat-1");

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
