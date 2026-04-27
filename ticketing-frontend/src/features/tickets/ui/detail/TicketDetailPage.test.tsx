import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { renderWithProviders } from "src/test/utils/renderWithProviders";
import { jsonResponse } from "src/test/msw/handlers";
import { http } from "src/test/msw/http";
import { server } from "src/test/msw/server";
import { vi } from "vitest";
import type { TicketDetail } from "../../model/types";
import { TicketDetailPage } from "./TicketDetailPage";

const navigateMock = vi.fn();
const hasAnyRoleMock = vi.fn<(roles: Array<"USER" | "AGENT" | "ADMIN">) => boolean>();
const authStateMock = {
  user: { id: "agent-1" },
};

vi.mock("src/features/auth/hooks/useAuth", () => ({
  useAuth: () => ({
    hasAnyRole: hasAnyRoleMock,
    state: authStateMock,
  }),
}));

const paramsMock = vi.fn<() => { id?: string }>(() => ({ id: "ticket-1" }));

vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual<typeof import("react-router-dom")>("react-router-dom");
  return {
    ...actual,
    useNavigate: () => navigateMock,
    useParams: () => paramsMock(),
  };
});

function buildTicket(overrides: Partial<TicketDetail> = {}): TicketDetail {
  return {
    id: "ticket-1",
    title: "Printer issue",
    description: "Paper jam on tray 2",
    status: "OPEN",
    priority: "HIGH",
    createdAt: "2026-03-15T10:00:00.000Z",
    updatedAt: "2026-03-15T10:00:00.000Z",
    createdByUserId: "user-1",
    createdByDisplayName: "User One",
    assignedToUserId: null,
    assignedToDisplayName: null,
    categoryId: "cat-1",
    availableTransitions: ["IN_PROGRESS"],
    timeline: [
      {
        id: "msg-1",
        kind: "MESSAGE",
        createdAt: "2026-03-15T10:00:00.000Z",
        actorUserId: "user-1",
        actorDisplayName: "User One",
        content: "Paper jam on tray 2",
        eventType: null,
        payload: {},
      },
    ],
    ...overrides,
  };
}

describe("TicketDetailPage", () => {
  beforeEach(() => {
    navigateMock.mockReset();
    paramsMock.mockReturnValue({ id: "ticket-1" });
    hasAnyRoleMock.mockImplementation((roles) => roles.includes("AGENT"));
    authStateMock.user = { id: "agent-1" };
    server.use(
      http.get("/api/categories", () =>
        jsonResponse([
          { id: "cat-1", name: "Hardware" },
          { id: "cat-2", name: "Software" },
        ]),
      ),
    );
  });

  it("carga el ticket y muestra título, estado y timeline", async () => {
    server.use(http.get("/api/tickets/ticket-1", () => jsonResponse(buildTicket())));

    renderWithProviders(<TicketDetailPage />, { router: {} });

    expect(await screen.findByTestId("ticket-detail-title")).toHaveTextContent("Printer issue");
    expect(screen.getByTestId("ticket-detail-status")).toHaveTextContent("Abierto");
    expect(screen.getByText("Paper jam on tray 2")).toBeInTheDocument();
    expect(screen.getByText("Categoría: Hardware")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Asignarme ticket" })).toBeInTheDocument();
  });

  it("permite asignarse ticket y recarga detalle", async () => {
    const ticketState = buildTicket();

    server.use(
      http.get("/api/tickets/ticket-1", () => jsonResponse(ticketState)),
      http.patch("/api/tickets/ticket-1/assignment/me", () => {
        ticketState.assignedToUserId = "agent-1";
        ticketState.assignedToDisplayName = "Agent One";
        ticketState.updatedAt = "2026-03-15T10:05:00.000Z";
        return jsonResponse({}, { status: 200 });
      }),
    );

    const user = userEvent.setup();
    renderWithProviders(<TicketDetailPage />, { router: {} });

    await user.click(await screen.findByRole("button", { name: "Asignarme ticket" }));

    await waitFor(() => {
      expect(screen.getByText(/Asignado a: Agent One/i)).toBeInTheDocument();
    });
  });

  it("permite cambiar de estado y refleja el nuevo badge", async () => {
    const ticketState = buildTicket();

    server.use(
      http.get("/api/tickets/ticket-1", () => jsonResponse(ticketState)),
      http.patch("/api/tickets/ticket-1/status", (request) => {
        const body = request.body as { status: "IN_PROGRESS" };
        ticketState.status = body.status;
        ticketState.availableTransitions = ["ON_HOLD", "RESOLVED"];
        ticketState.updatedAt = "2026-03-15T10:06:00.000Z";
        return jsonResponse({}, { status: 200 });
      }),
    );

    const user = userEvent.setup();
    renderWithProviders(<TicketDetailPage />, { router: {} });

    await user.click(await screen.findByTestId("ticket-status-transition-IN_PROGRESS"));

    await waitFor(() => {
      expect(screen.getByTestId("ticket-detail-status")).toHaveTextContent("En progreso");
    });
  });

  it("envía comentario, limpia textarea y muestra contenido nuevo en timeline", async () => {
    const ticketState = buildTicket();
    ticketState.assignedToUserId = "agent-1";
    ticketState.assignedToDisplayName = "Agent One";

    server.use(
      http.get("/api/tickets/ticket-1", () => jsonResponse(ticketState)),
      http.post("/api/tickets/ticket-1/comments", (request) => {
        const body = request.body as { content: string };
        ticketState.timeline.push({
          id: "msg-2",
          kind: "MESSAGE",
          createdAt: "2026-03-15T10:07:00.000Z",
          actorUserId: "agent-1",
          actorDisplayName: "Agent One",
          content: body.content,
          eventType: null,
          payload: {},
        });
        return jsonResponse(
          {
            id: "msg-2",
            ticketId: "ticket-1",
            authorUserId: "agent-1",
            content: body.content,
            createdAt: "2026-03-15T10:07:00.000Z",
          },
          { status: 201 },
        );
      }),
    );

    const user = userEvent.setup();
    renderWithProviders(<TicketDetailPage />, { router: {} });

    const textarea = await screen.findByPlaceholderText("Escribe un comentario");
    await user.type(textarea, "  Revisado, aplico solución.  ");
    await user.click(screen.getByRole("button", { name: "Enviar comentario" }));

    await waitFor(() => {
      expect(screen.getByDisplayValue("")).toBeInTheDocument();
      expect(screen.getByText("Revisado, aplico solución.")).toBeInTheDocument();
    });
  });

  it("en rol USER oculta la caja de acciones de gestión pero permite comentar", async () => {
    hasAnyRoleMock.mockImplementation((roles) => roles.includes("USER"));
    authStateMock.user = { id: "user-1" };
    server.use(http.get("/api/tickets/ticket-1", () => jsonResponse(buildTicket())));

    renderWithProviders(<TicketDetailPage />, { router: {} });

    expect(await screen.findByText("Conversación")).toBeInTheDocument();
    expect(screen.queryByText("Acciones")).not.toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "Asignarme ticket" })).not.toBeInTheDocument();
    expect(screen.queryByTestId("ticket-status-transition-IN_PROGRESS")).not.toBeInTheDocument();
    expect(screen.getByPlaceholderText("Escribe un comentario")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Enviar comentario" })).toBeInTheDocument();
  });

  it("agente no propietario ve mensaje y no puede usar acciones ni comentar", async () => {
    authStateMock.user = { id: "agent-2" };
    server.use(
      http.get("/api/tickets/ticket-1", () =>
        jsonResponse(
          buildTicket({
            assignedToUserId: "agent-1",
            assignedToDisplayName: "Agent One",
          }),
        ),
      ),
    );

    renderWithProviders(<TicketDetailPage />, { router: {} });

    expect(await screen.findByText("Acciones")).toBeInTheDocument();
    expect(screen.getByText("No eres el propietario de este ticket.")).toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "Asignarme ticket" })).not.toBeInTheDocument();
    expect(screen.queryByTestId("ticket-status-transition-IN_PROGRESS")).not.toBeInTheDocument();
    expect(screen.getByText("No tienes permisos para añadir comentarios.")).toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "Enviar comentario" })).not.toBeInTheDocument();
  });

  it("admin puede usar acciones y comentar aunque no sea propietario", async () => {
    hasAnyRoleMock.mockImplementation((roles) => roles.includes("ADMIN"));
    authStateMock.user = { id: "admin-1" };
    server.use(
      http.get("/api/tickets/ticket-1", () =>
        jsonResponse(
          buildTicket({
            assignedToUserId: "agent-1",
            assignedToDisplayName: "Agent One",
          }),
        ),
      ),
    );

    renderWithProviders(<TicketDetailPage />, { router: {} });

    expect(await screen.findByText("Acciones")).toBeInTheDocument();
    expect(screen.getByTestId("ticket-status-transition-IN_PROGRESS")).toBeInTheDocument();
    expect(screen.getByPlaceholderText("Escribe un comentario")).toBeInTheDocument();
  });

  it("si el ticket está resuelto deshabilita envío de comentarios", async () => {
    server.use(
      http.get("/api/tickets/ticket-1", () =>
        jsonResponse(
          buildTicket({
            status: "RESOLVED",
            availableTransitions: [],
            assignedToUserId: "agent-1",
            assignedToDisplayName: "Agent One",
          }),
        ),
      ),
    );

    renderWithProviders(<TicketDetailPage />, { router: {} });

    expect(await screen.findByText("Ticket resuelto")).toBeInTheDocument();
    const submitCommentButton = screen.getByRole("button", { name: "Enviar comentario" });
    expect(submitCommentButton).toBeDisabled();
  });

  it("si falta id muestra error y volver navega a /tickets", async () => {
    paramsMock.mockReturnValue({ id: undefined });
    const user = userEvent.setup();

    renderWithProviders(<TicketDetailPage />, { router: {} });

    expect(await screen.findByText("Error cargando el ticket")).toBeInTheDocument();
    expect(screen.getByText("Id de ticket inválido")).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: "Volver a tickets" }));
    expect(navigateMock).toHaveBeenCalledWith("/tickets");
  });
});
