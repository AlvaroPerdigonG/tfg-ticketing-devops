import { Alert, Button, Card, Empty, Input, Skeleton, Space, Tag, Typography } from "antd";
import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useAuth } from "../../../auth/hooks/useAuth";
import { ticketsApi } from "../../api/ticketsApi";
import { ticketPriorityLabel, ticketStatusLabel } from "../../model/presentation";
import type { TicketCategory, TicketDetail, TicketStatus, TimelineEntry } from "../../model/types";
import { TicketStatusBadge } from "../shared/TicketStatusBadge";

type LoadState = "loading" | "ready" | "error";

function formatDate(isoDate: string) {
  return new Intl.DateTimeFormat("en-US", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(isoDate));
}

export function TicketDetailPage() {
  const { id } = useParams<{ id: string }>();
  const { hasAnyRole, state } = useAuth();
  const navigate = useNavigate();

  const canManage = hasAnyRole(["AGENT", "ADMIN"]);
  const isAdmin = hasAnyRole(["ADMIN"]);
  const isAgent = hasAnyRole(["AGENT"]);
  const currentUserId = state.user?.id ?? null;
  const [loadState, setLoadState] = useState<LoadState>("loading");
  const [ticket, setTicket] = useState<TicketDetail | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [busyAction, setBusyAction] = useState<"assign" | "status" | null>(null);
  const [commentText, setCommentText] = useState("");
  const [busyComment, setBusyComment] = useState(false);
  const [categories, setCategories] = useState<TicketCategory[]>([]);

  const availableStatusTransitions = useMemo(
    () => ticket?.availableTransitions ?? ([] as TicketStatus[]),
    [ticket],
  );

  const timelineItems = useMemo(() => {
    if (!ticket) return [];
    return ticket.timeline.map((entry: TimelineEntry) => ({
      key: entry.id,
      children: (
        <Space direction="vertical" size={4}>
          <Typography.Text type="secondary">{formatDate(entry.createdAt)}</Typography.Text>
          {entry.kind === "MESSAGE" ? (
            <>
              <Typography.Text>
                <strong>{entry.actorDisplayName ?? "Sistema"}</strong>
              </Typography.Text>
              <Typography.Paragraph style={{ margin: 0 }}>{entry.content}</Typography.Paragraph>
            </>
          ) : (
            <Typography.Text>
              {entry.eventType === "STATUS_CHANGED" &&
                `Cambio de estado: ${entry.payload.from} → ${entry.payload.to}`}
              {entry.eventType === "ASSIGNED_TO_ME" &&
                `Assigned to ${entry.actorDisplayName ?? "agent"}`}
              {entry.eventType === "TICKET_CREATED" && "Ticket creado"}
            </Typography.Text>
          )}
        </Space>
      ),
    }));
  }, [ticket]);

  const categoryDisplayName = useMemo(() => {
    if (!ticket) return "";
    const category = categories.find((item) => item.id === ticket.categoryId);
    return category?.name ?? ticket.categoryId;
  }, [categories, ticket]);

  const isAgentOwner = useMemo(() => {
    if (!ticket || !isAgent || !currentUserId) return false;
    return ticket.assignedToUserId === currentUserId;
  }, [currentUserId, isAgent, ticket]);

  const canUseActions = useMemo(() => {
    if (!ticket || !canManage) return false;
    if (isAdmin) return true;
    return !ticket.assignedToUserId || isAgentOwner;
  }, [canManage, isAdmin, isAgentOwner, ticket]);

  const canAddComment = useMemo(() => {
    if (!ticket) return false;
    return isAdmin || isAgentOwner || hasAnyRole(["USER"]);
  }, [hasAnyRole, isAdmin, isAgentOwner, ticket]);

  useEffect(() => {
    if (!id) {
      setLoadState("error");
      setErrorMessage("Invalid ticket id");
      return;
    }

    let mounted = true;

    const load = async () => {
      setLoadState("loading");
      setErrorMessage(null);

      try {
        const [ticketResponse, categoriesResponse] = await Promise.all([
          ticketsApi.getTicketById(id),
          ticketsApi.getCategories().catch(() => []),
        ]);
        if (!mounted) return;
        setTicket(ticketResponse);
        setCategories(categoriesResponse);
        setLoadState("ready");
      } catch (error) {
        if (!mounted) return;
        setLoadState("error");
        setErrorMessage(error instanceof Error ? error.message : "Could not load the ticket.");
      }
    };

    void load();
    return () => {
      mounted = false;
    };
  }, [id]);

  const reload = async () => {
    if (!id) return;
    const response = await ticketsApi.getTicketById(id);
    setTicket(response);
  };

  const handleAssignToMe = async () => {
    if (!id) return;
    setBusyAction("assign");
    setErrorMessage(null);
    try {
      await ticketsApi.assignToMe(id);
      await reload();
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : "Could not assign the ticket.");
    } finally {
      setBusyAction(null);
    }
  };

  const handleStatusChange = async (status: TicketStatus) => {
    if (!id) return;
    setBusyAction("status");
    setErrorMessage(null);
    try {
      await ticketsApi.changeStatus(id, status);
      await reload();
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : "Could not change status.");
    } finally {
      setBusyAction(null);
    }
  };

  const handleAddComment = async () => {
    if (!id || !commentText.trim()) return;
    setBusyComment(true);
    setErrorMessage(null);
    try {
      await ticketsApi.addComment(id, commentText.trim());
      setCommentText("");
      await reload();
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : "Could not add comment.");
    } finally {
      setBusyComment(false);
    }
  };

  if (loadState === "loading") {
    return <Skeleton active paragraph={{ rows: 10 }} />;
  }

  if (loadState === "error" || !ticket) {
    return (
      <Space direction="vertical" style={{ width: "100%" }} size={16}>
        <Alert type="error" showIcon message="Error loading ticket" description={errorMessage} />
        <Button onClick={() => navigate("/tickets")}>Back to tickets</Button>
      </Space>
    );
  }

  return (
    <Space direction="vertical" size={16} style={{ width: "100%" }}>
      {errorMessage && (
        <Alert
          type="error"
          showIcon
          message="Could not complete action"
          description={errorMessage}
        />
      )}

      <div
        style={{
          display: "grid",
          gridTemplateColumns: canManage ? "2fr 3fr 1.5fr" : "2fr 4fr",
          gap: 16,
        }}
      >
        <div>
          <Card>
            <Space direction="vertical" size={12} style={{ width: "100%" }}>
              <Typography.Title level={4} style={{ margin: 0 }}>
                <span data-testid="ticket-detail-title">{ticket.title}</span>
              </Typography.Title>
              <div data-testid="ticket-detail-status">
                <TicketStatusBadge status={ticket.status} />
              </div>
              <Tag>{ticketPriorityLabel[ticket.priority]}</Tag>
              <Typography.Title level={5} style={{ margin: 0 }}>
                Metadata
              </Typography.Title>
              <Typography.Text>ID: {ticket.id}</Typography.Text>
              <Typography.Text>Category: {categoryDisplayName}</Typography.Text>
              <Typography.Text>Created by: {ticket.createdByDisplayName}</Typography.Text>
              <Typography.Text>
                Assigned to: {ticket.assignedToDisplayName ?? "Unassigned"}
              </Typography.Text>
              <Typography.Text>Created: {formatDate(ticket.createdAt)}</Typography.Text>
              <Typography.Text>Updated: {formatDate(ticket.updatedAt)}</Typography.Text>
            </Space>
          </Card>
        </div>

        <div>
          <Card>
            <Space direction="vertical" size={12} style={{ width: "100%" }}>
              <Typography.Title level={5} style={{ margin: 0 }}>
                Conversation
              </Typography.Title>
              {timelineItems.length === 0 ? (
                <Empty description="No activity yet" />
              ) : (
                <div style={{ maxHeight: 420, overflowY: "auto", paddingRight: 8 }}>
                  <Space direction="vertical" size={12} style={{ width: "100%" }}>
                    {timelineItems.map((item) => (
                      <Card key={item.key}>{item.children}</Card>
                    ))}
                  </Space>
                </div>
              )}
              {ticket.status === "RESOLVED" && (
                <Alert
                  type="info"
                  message="Ticket resolved"
                  description="No new comments allowed."
                />
              )}
              {canAddComment ? (
                <>
                  <Input.TextArea
                    rows={3}
                    value={commentText}
                    maxLength={2000}
                    placeholder="Write a comment"
                    onChange={(event) => setCommentText(event.target.value)}
                  />
                  <Button
                    type="primary"
                    loading={busyComment}
                    disabled={!commentText.trim() || ticket.status === "RESOLVED"}
                    onClick={handleAddComment}
                  >
                    Send comment
                  </Button>
                </>
              ) : (
                <Typography.Text type="secondary">
                  You do not have permission to add comments.
                </Typography.Text>
              )}
            </Space>
          </Card>
        </div>

        {canManage && (
          <div>
            <Card>
              <Typography.Title level={5} style={{ margin: "0 0 12px" }}>
                Actions
              </Typography.Title>
              {canUseActions ? (
                <Space direction="vertical" style={{ width: "100%" }} size={12}>
                  {!ticket.assignedToUserId && (
                    <Button loading={busyAction === "assign"} onClick={handleAssignToMe}>
                      Assign ticket to me
                    </Button>
                  )}
                  <Typography.Title level={5} style={{ margin: 0 }}>
                    Change status
                  </Typography.Title>
                  {availableStatusTransitions.length === 0 && (
                    <Typography.Text type="secondary">No transitions available.</Typography.Text>
                  )}
                  {availableStatusTransitions.map((nextStatus) => (
                    <Button
                      key={nextStatus}
                      data-testid={`ticket-status-transition-${nextStatus}`}
                      type="primary"
                      loading={busyAction === "status"}
                      onClick={() => handleStatusChange(nextStatus)}
                    >
                      Move to {ticketStatusLabel[nextStatus]}
                    </Button>
                  ))}
                </Space>
              ) : (
                <Typography.Text type="secondary">
                  You are not the owner of this ticket.
                </Typography.Text>
              )}
            </Card>
          </div>
        )}
      </div>
    </Space>
  );
}
