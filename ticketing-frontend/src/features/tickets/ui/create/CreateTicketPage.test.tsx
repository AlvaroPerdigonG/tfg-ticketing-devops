import { fireEvent, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { renderWithProviders } from "src/test/utils/renderWithProviders";
import { jsonResponse } from "src/test/msw/handlers";
import { http } from "src/test/msw/http";
import { server } from "src/test/msw/server";
import { vi } from "vitest";
import { CreateTicketPage } from "./CreateTicketPage";

const navigateMock = vi.fn();

vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual<typeof import("react-router-dom")>("react-router-dom");
  return {
    ...actual,
    useNavigate: () => navigateMock,
  };
});

describe("CreateTicketPage", () => {
  beforeEach(() => {
    navigateMock.mockReset();
  });

  it("mantiene submit deshabilitado mientras faltan campos requeridos", async () => {
    server.use(http.get("/api/categories", () => jsonResponse([{ id: "cat-1", name: "General" }])));

    renderWithProviders(<CreateTicketPage />, { router: {} });

    const submitButton = await screen.findByRole("button", { name: "Crear ticket" });
    expect(submitButton).toBeDisabled();
  });

  it("deshabilita envío mientras carga categorías", async () => {
    server.use(
      http.get("/api/categories", async () => {
        await new Promise((resolve) => setTimeout(resolve, 150));
        return jsonResponse([{ id: "cat-1", name: "General" }]);
      }),
    );

    renderWithProviders(<CreateTicketPage />, { router: {} });

    const submitButton = screen.getByRole("button", { name: "Crear ticket" });
    expect(submitButton).toBeDisabled();
  });

  it("[TICKET-USER-01] Usuario crea ticket correctamente", async () => {
    const user = userEvent.setup();
    let payload: unknown;

    server.use(
      http.get("/api/categories", () => jsonResponse([{ id: "cat-1", name: "General" }])),
      http.post("/api/tickets", (request) => {
        payload = request.body;
        return jsonResponse({ ticketId: "t-100" }, { status: 201 });
      }),
    );

    renderWithProviders(<CreateTicketPage />, { router: {} });

    await user.type(await screen.findByLabelText("Título"), "  Impresora bloqueada  ");
    await user.type(screen.getByLabelText("Descripción"), "  Error E23 al imprimir PDF  ");

    const categoryCombobox = screen.getByRole("combobox", { name: "Categoría" });
    fireEvent.mouseDown(categoryCombobox);
    await user.click(await screen.findByText("General"));

    await waitFor(() => {
      expect(screen.getByRole("button", { name: "Crear ticket" })).toBeEnabled();
    });

    await user.click(screen.getByRole("button", { name: "Crear ticket" }));

    await waitFor(() => {
      expect(payload).toEqual({
        title: "Impresora bloqueada",
        description: "Error E23 al imprimir PDF",
        categoryId: "cat-1",
        priority: "MEDIUM",
      });
      expect(navigateMock).toHaveBeenCalledWith("/tickets/t-100");
    });
  });


  it("deshabilita botón durante el envío para evitar doble submit", async () => {
    const user = userEvent.setup();

    server.use(
      http.get("/api/categories", () => jsonResponse([{ id: "cat-1", name: "General" }])),
      http.post("/api/tickets", async () => {
        await new Promise((resolve) => setTimeout(resolve, 200));
        return jsonResponse({ ticketId: "t-777" }, { status: 201 });
      }),
    );

    renderWithProviders(<CreateTicketPage />, { router: {} });

    await user.type(await screen.findByLabelText("Título"), "Ticket en cola");
    await user.type(screen.getByLabelText("Descripción"), "Descripción completa");
    fireEvent.mouseDown(screen.getByRole("combobox", { name: "Categoría" }));
    await user.click(await screen.findByText("General"));

    const submitButton = screen.getByRole("button", { name: "Crear ticket" });
    await user.click(submitButton);

    expect(submitButton).toHaveClass("ant-btn-loading");
  });

  it("muestra error visible cuando falla el envío", async () => {
    const user = userEvent.setup();

    server.use(
      http.get("/api/categories", () => jsonResponse([{ id: "cat-1", name: "General" }])),
      http.post("/api/tickets", () => jsonResponse({ message: "No autorizado" }, { status: 403 })),
    );

    renderWithProviders(<CreateTicketPage />, { router: {} });

    await user.type(await screen.findByLabelText("Título"), "No imprime");
    await user.type(screen.getByLabelText("Descripción"), "Error de cola de impresión");

    fireEvent.mouseDown(screen.getByRole("combobox", { name: "Categoría" }));
    await user.click(await screen.findByText("General"));
    await user.click(screen.getByRole("button", { name: "Crear ticket" }));

    expect(await screen.findByText("Error al crear ticket")).toBeInTheDocument();
  });
});
