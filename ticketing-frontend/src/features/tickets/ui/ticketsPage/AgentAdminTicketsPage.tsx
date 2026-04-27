import {
  Alert,
  Button,
  Card,
  Empty,
  Input,
  Select,
  Skeleton,
  Space,
  Table,
  Tag,
  Typography,
} from "antd";
import type { TableProps } from "antd";
import { useEffect, useMemo, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { ticketsApi } from "../../api/ticketsApi";
import { ticketPriorityLabel } from "../../model/presentation";
import type {
  TicketPriority,
  TicketQueueScope,
  TicketStatus,
  TicketSummary,
} from "../../model/types";
import { TicketStatusBadge } from "../shared/TicketStatusBadge";

type QueueView = "unassigned" | "mine" | "all";
type LoadState = "loading" | "ready" | "error";
type QueueFilter = "all" | TicketStatus;

const statusFilterOptions: Array<{ label: string; value: QueueFilter }> = [
  { label: "All statuses", value: "all" },
  { label: "Open", value: "OPEN" },
  { label: "In progress", value: "IN_PROGRESS" },
  { label: "On hold", value: "ON_HOLD" },
  { label: "Resolved", value: "RESOLVED" },
];

function toQueueView(value: string | null): QueueView {
  if (value === "mine" || value === "all" || value === "unassigned") {
    return value;
  }

  return "unassigned";
}

function toStatusFilter(value: string | null): QueueFilter {
  if (value === "OPEN" || value === "IN_PROGRESS" || value === "ON_HOLD" || value === "RESOLVED") {
    return value;
  }

  return "all";
}

function formatDate(isoDate: string) {
  return new Intl.DateTimeFormat("en-US", {
    dateStyle: "short",
    timeStyle: "short",
  }).format(new Date(isoDate));
}

function queueScopeFromView(view: QueueView): TicketQueueScope {
  if (view === "unassigned") return "UNASSIGNED";
  if (view === "mine") return "MINE";
  return "ALL";
}

export function AgentAdminTicketsPage() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();

  const [view, setView] = useState<QueueView>(() => toQueueView(searchParams.get("view")));
  const [statusFilter, setStatusFilter] = useState<QueueFilter>(() =>
    toStatusFilter(searchParams.get("status")),
  );
  const [query, setQuery] = useState<string>(() => searchParams.get("q") ?? "");
  const [inputQuery, setInputQuery] = useState<string>(() => searchParams.get("q") ?? "");
  const [loadState, setLoadState] = useState<LoadState>("loading");
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [tickets, setTickets] = useState<TicketSummary[]>([]);
  const [total, setTotal] = useState(0);

  useEffect(() => {
    const params = new URLSearchParams();
    params.set("view", view);
    if (statusFilter !== "all") params.set("status", statusFilter);
    if (query.trim()) params.set("q", query.trim());
    setSearchParams(params, { replace: true });
  }, [query, setSearchParams, statusFilter, view]);

  useEffect(() => {
    let isMounted = true;

    const loadQueue = async () => {
      setLoadState("loading");
      setErrorMessage(null);

      try {
        const response = await ticketsApi.getQueueTickets({
          scope: queueScopeFromView(view),
          status: statusFilter === "all" ? undefined : statusFilter,
          q: query.trim() || undefined,
          page: 0,
          size: 20,
        });

        if (!isMounted) return;

        setTickets(response.items);
        setTotal(response.total);
        setLoadState("ready");
      } catch (error) {
        if (!isMounted) return;

        setLoadState("error");
        setErrorMessage(
          error instanceof Error ? error.message : "Could not load the ticket queue.",
        );
      }
    };

    void loadQueue();

    return () => {
      isMounted = false;
    };
  }, [query, statusFilter, view]);

  const columns: TableProps<TicketSummary>["columns"] = useMemo(
    () => [
      { title: "ID", dataIndex: "id", key: "id", width: 180 },
      { title: "Title", dataIndex: "title", key: "title" },
      {
        title: "Status",
        dataIndex: "status",
        key: "status",
        width: 140,
        render: (statusValue: unknown) => (
          <TicketStatusBadge status={statusValue as TicketStatus} />
        ),
      },
      {
        title: "Priority",
        dataIndex: "priority",
        key: "priority",
        width: 120,
        render: (priorityValue: unknown) => (
          <Tag>{ticketPriorityLabel[priorityValue as TicketPriority]}</Tag>
        ),
      },
      {
        title: "Assigned",
        dataIndex: "assignedToUserId",
        key: "assignedToUserId",
        width: 180,
        render: (assigneeValue: unknown) => (assigneeValue ? String(assigneeValue) : "Unassigned"),
      },
      {
        title: "Updated",
        dataIndex: "updatedAt",
        key: "updatedAt",
        width: 180,
        render: (updatedAtValue: unknown) => formatDate(String(updatedAtValue)),
      },
      {
        title: "Actions",
        dataIndex: "id",
        key: "actions",
        width: 120,
        render: (_id: unknown, row: TicketSummary) => (
          <Button size="small" onClick={() => navigate(`/tickets/${row.id}`)}>
            View
          </Button>
        ),
      },
    ],
    [navigate],
  );

  const viewLabel =
    view === "unassigned"
      ? "Unassigned queue"
      : view === "mine"
        ? "Tickets assigned to me"
        : "All tickets";

  const unassignedCount = tickets.filter((ticket) => ticket.assignedToUserId === null).length;
  const inProgressCount = tickets.filter((ticket) => ticket.status === "IN_PROGRESS").length;

  const onApplySearch = () => {
    setQuery(inputQuery);
  };

  const onClearFilters = () => {
    setView("unassigned");
    setStatusFilter("all");
    setInputQuery("");
    setQuery("");
  };

  return (
    <Space direction="vertical" size={16} style={{ width: "100%" }}>
      <Typography.Title level={3} style={{ margin: 0 }}>
        <span data-testid="agent-tickets-title">Ticket management</span>
      </Typography.Title>

      <Space style={{ display: "grid", gridTemplateColumns: "repeat(4, minmax(0, 1fr))" }}>
        <Card>
          <Typography.Text type="secondary">Unassigned (view)</Typography.Text>
          <Typography.Title level={3} style={{ margin: "8px 0 0" }}>
            {unassignedCount}
          </Typography.Title>
        </Card>
        <Card>
          <Typography.Text type="secondary">Total (view)</Typography.Text>
          <Typography.Title level={3} style={{ margin: "8px 0 0" }}>
            {total}
          </Typography.Title>
        </Card>
        <Card>
          <Typography.Text type="secondary">In progress (view)</Typography.Text>
          <Typography.Title level={3} style={{ margin: "8px 0 0" }}>
            {inProgressCount}
          </Typography.Title>
        </Card>
        <Card>
          <Typography.Text type="secondary">Current scope</Typography.Text>
          <Typography.Title level={3} style={{ margin: "8px 0 0" }}>
            {queueScopeFromView(view)}
          </Typography.Title>
        </Card>
      </Space>

      <Space>
        <Button
          type={view === "unassigned" ? "primary" : "default"}
          onClick={() => setView("unassigned")}
        >
          Unassigned
        </Button>
        <Button type={view === "mine" ? "primary" : "default"} onClick={() => setView("mine")}>
          Assigned to me
        </Button>
        <Button type={view === "all" ? "primary" : "default"} onClick={() => setView("all")}>
          All
        </Button>
      </Space>

      <Card>
        <Space>
          <Select
            aria-label="Status"
            value={statusFilter}
            onChange={(value) => setStatusFilter(value as QueueFilter)}
            options={statusFilterOptions}
            style={{ minWidth: 220 }}
          />
          <Input
            value={inputQuery}
            onChange={(event) => setInputQuery(event.target.value)}
            placeholder="Search by title or ID"
          />
          <Button onClick={onApplySearch}>Search</Button>
          <Button onClick={onClearFilters}>Clear filters</Button>
        </Space>
      </Card>

      <Card>
        <Space direction="vertical" size={12} style={{ width: "100%" }}>
          <Typography.Title level={4} style={{ margin: 0 }}>
            {viewLabel}
          </Typography.Title>

          {loadState === "loading" && <Skeleton active paragraph={{ rows: 6 }} />}

          {loadState === "error" && (
            <Alert
              type="error"
              showIcon
              message="Could not load the ticket queue"
              description={errorMessage}
            />
          )}

          {loadState === "ready" && tickets.length === 0 && (
            <Empty description="No tickets to display with the current filters" />
          )}

          {loadState === "ready" && tickets.length > 0 && (
            <Table<TicketSummary>
              rowKey="id"
              columns={columns}
              dataSource={tickets}
              pagination={{ pageSize: 10 }}
            />
          )}
        </Space>
      </Card>
    </Space>
  );
}
