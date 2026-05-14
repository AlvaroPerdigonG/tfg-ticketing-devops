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

  it("loads the ticket and shows title, status, and timeline", async () => {
    server.use(http.get("/api/tickets/ticket-1", () => jsonResponse(buildTicket())));

    renderWithProviders(<TicketDetailPage />, { router: {} });

    expect(await screen.findByTestId("ticket-detail-title")).toHaveTextContent("Printer issue");
    expect(screen.getByTestId("ticket-detail-status")).toHaveTextContent("Open");
    expect(screen.getByText("Paper jam on tray 2")).toBeInTheDocument();
    expect(screen.getByText("Category: Hardware")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Assign ticket to me" })).toBeInTheDocument();
  });

  it("allows assigning the ticket to self and reloads detail", async () => {
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

    await user.click(await screen.findByRole("button", { name: "Assign ticket to me" }));

    await waitFor(() => {
      expect(screen.getByText(/Assigned to: Agent One/i)).toBeInTheDocument();
    });
  });

  it("allows changing status and reflects the new badge", async () => {
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
      expect(screen.getByTestId("ticket-detail-status")).toHaveTextContent("In progress");
    });
  });

  it("sends a comment, clears the textarea, and shows new timeline content", async () => {
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

    const textarea = await screen.findByPlaceholderText("Write a comment");
    await user.type(textarea, "  Reviewed, applying a solution.  ");
    await user.click(screen.getByRole("button", { name: "Send comment" }));

    await waitFor(() => {
      expect(screen.getByDisplayValue("")).toBeInTheDocument();
      expect(screen.getByText("Reviewed, applying a solution.")).toBeInTheDocument();
    });
  });

  it("with USER role hides the management actions box but allows commenting", async () => {
    hasAnyRoleMock.mockImplementation((roles) => roles.includes("USER"));
    authStateMock.user = { id: "user-1" };
    server.use(http.get("/api/tickets/ticket-1", () => jsonResponse(buildTicket())));

    renderWithProviders(<TicketDetailPage />, { router: {} });

    expect(await screen.findByText("Conversation")).toBeInTheDocument();
    expect(screen.queryByText("Actions")).not.toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "Assign ticket to me" })).not.toBeInTheDocument();
    expect(screen.queryByTestId("ticket-status-transition-IN_PROGRESS")).not.toBeInTheDocument();
    expect(screen.getByPlaceholderText("Write a comment")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Send comment" })).toBeInTheDocument();
  });

  it("non-owner agent sees a message and cannot use actions or comment", async () => {
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

    expect(await screen.findByText("Actions")).toBeInTheDocument();
    expect(screen.getByText("You are not the owner of this ticket.")).toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "Assign ticket to me" })).not.toBeInTheDocument();
    expect(screen.queryByTestId("ticket-status-transition-IN_PROGRESS")).not.toBeInTheDocument();
    expect(screen.getByText("You do not have permission to add comments.")).toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "Send comment" })).not.toBeInTheDocument();
  });

  it("admin can use actions and comment even when not the owner", async () => {
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

    expect(await screen.findByText("Actions")).toBeInTheDocument();
    expect(screen.getByTestId("ticket-status-transition-IN_PROGRESS")).toBeInTheDocument();
    expect(screen.getByPlaceholderText("Write a comment")).toBeInTheDocument();
  });

  it("disables comment submission when the ticket is resolved", async () => {
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

    expect(await screen.findByText("Ticket resolved")).toBeInTheDocument();
    const submitCommentButton = screen.getByRole("button", { name: "Send comment" });
    expect(submitCommentButton).toBeDisabled();
  });

  it("shows an error when the id is missing and back navigates to /tickets", async () => {
    paramsMock.mockReturnValue({ id: undefined });
    const user = userEvent.setup();

    renderWithProviders(<TicketDetailPage />, { router: {} });

    expect(await screen.findByText("Error loading ticket")).toBeInTheDocument();
    expect(screen.getByText("Invalid ticket id")).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: "Back to tickets" }));
    expect(navigateMock).toHaveBeenCalledWith("/tickets");
  });

  it("renders timeline events for creation, status changes, and assignment", async () => {
    server.use(
      http.get("/api/tickets/ticket-1", () =>
        jsonResponse(
          buildTicket({
            timeline: [
              {
                id: "ev-1",
                kind: "EVENT",
                createdAt: "2026-03-15T10:00:00.000Z",
                actorUserId: "user-1",
                actorDisplayName: "User One",
                content: null,
                eventType: "TICKET_CREATED",
                payload: {},
              },
              {
                id: "ev-2",
                kind: "EVENT",
                createdAt: "2026-03-15T10:01:00.000Z",
                actorUserId: "agent-1",
                actorDisplayName: "Agent One",
                content: null,
                eventType: "STATUS_CHANGED",
                payload: { from: "OPEN", to: "IN_PROGRESS" },
              },
              {
                id: "ev-3",
                kind: "EVENT",
                createdAt: "2026-03-15T10:02:00.000Z",
                actorUserId: "agent-1",
                actorDisplayName: "Agent One",
                content: null,
                eventType: "ASSIGNED_TO_ME",
                payload: {},
              },
            ],
          }),
        ),
      ),
    );

    renderWithProviders(<TicketDetailPage />, { router: {} });

    expect(await screen.findByText("Ticket creado")).toBeInTheDocument();
    expect(screen.getByText("Cambio de estado: OPEN → IN_PROGRESS")).toBeInTheDocument();
    expect(screen.getByText("Assigned to Agent One")).toBeInTheDocument();
  });

  it("shows a generic message when loading fails with an untyped error", async () => {
    server.use(
      http.get("/api/tickets/ticket-1", () => {
        throw "boom";
      }),
    );

    renderWithProviders(<TicketDetailPage />, { router: {} });

    expect(await screen.findByText("Error loading ticket")).toBeInTheDocument();
    expect(screen.getByText("Could not load the ticket.")).toBeInTheDocument();
  });
});
