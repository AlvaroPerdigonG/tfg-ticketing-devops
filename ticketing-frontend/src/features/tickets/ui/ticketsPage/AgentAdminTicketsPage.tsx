import { Alert, Button, Card, Empty, Input, Select, Skeleton, Space, Table, Tag, Typography } from "antd";
import type { TableProps } from "antd";
import { useEffect, useMemo, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { ticketsApi } from "../../api/ticketsApi";
import { ticketPriorityLabel, ticketStatusColor, ticketStatusLabel } from "../../model/presentation";
import type { TicketPriority, TicketQueueScope, TicketStatus, TicketSummary } from "../../model/types";

type QueueView = "unassigned" | "mine" | "all";
type LoadState = "loading" | "ready" | "error";
type QueueFilter = "all" | TicketStatus;

const statusFilterOptions: Array<{ label: string; value: QueueFilter }> = [
  { label: "Todos los estados", value: "all" },
  { label: "Abierto", value: "OPEN" },
  { label: "En progreso", value: "IN_PROGRESS" },
  { label: "Resuelto", value: "RESOLVED" },
];

function toQueueView(value: string | null): QueueView {
  if (value === "mine" || value === "all" || value === "unassigned") {
    return value;
  }

  return "unassigned";
}

function toStatusFilter(value: string | null): QueueFilter {
  if (value === "OPEN" || value === "IN_PROGRESS" || value === "RESOLVED") {
    return value;
  }

  return "all";
}

function formatDate(isoDate: string) {
  return new Intl.DateTimeFormat("es-ES", {
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
  const [statusFilter, setStatusFilter] = useState<QueueFilter>(() => toStatusFilter(searchParams.get("status")));
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
        setErrorMessage(error instanceof Error ? error.message : "No se pudo cargar la cola de tickets.");
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
      { title: "Título", dataIndex: "title", key: "title" },
      {
        title: "Estado",
        dataIndex: "status",
        key: "status",
        width: 140,
        render: (statusValue: unknown) => (
          <Tag color={ticketStatusColor[statusValue as TicketStatus]}>{ticketStatusLabel[statusValue as TicketStatus]}</Tag>
        ),
      },
      {
        title: "Prioridad",
        dataIndex: "priority",
        key: "priority",
        width: 120,
        render: (priorityValue: unknown) => <Tag>{ticketPriorityLabel[priorityValue as TicketPriority]}</Tag>,
      },
      {
        title: "Asignado",
        dataIndex: "assignedToUserId",
        key: "assignedToUserId",
        width: 180,
        render: (assigneeValue: unknown) => (assigneeValue ? String(assigneeValue) : "Sin asignar"),
      },
      {
        title: "Actualizado",
        dataIndex: "updatedAt",
        key: "updatedAt",
        width: 180,
        render: (updatedAtValue: unknown) => formatDate(String(updatedAtValue)),
      },
      {
        title: "Acciones",
        dataIndex: "id",
        key: "actions",
        width: 120,
        render: (_id: unknown, row: TicketSummary) => (
          <Button size="small" onClick={() => navigate(`/tickets/${row.id}`)}>
            Ver
          </Button>
        ),
      },
    ],
    [navigate],
  );

  const viewLabel =
    view === "unassigned"
      ? "Cola sin asignar"
      : view === "mine"
        ? "Tickets asignados a mí"
        : "Todos los tickets";

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
        Gestión de tickets
      </Typography.Title>

      <Space style={{ display: "grid", gridTemplateColumns: "repeat(4, minmax(0, 1fr))" }}>
        <Card>
          <Typography.Text type="secondary">Sin asignar (vista)</Typography.Text>
          <Typography.Title level={3} style={{ margin: "8px 0 0" }}>{unassignedCount}</Typography.Title>
        </Card>
        <Card>
          <Typography.Text type="secondary">Total (vista)</Typography.Text>
          <Typography.Title level={3} style={{ margin: "8px 0 0" }}>{total}</Typography.Title>
        </Card>
        <Card>
          <Typography.Text type="secondary">En progreso (vista)</Typography.Text>
          <Typography.Title level={3} style={{ margin: "8px 0 0" }}>{inProgressCount}</Typography.Title>
        </Card>
        <Card>
          <Typography.Text type="secondary">Scope actual</Typography.Text>
          <Typography.Title level={3} style={{ margin: "8px 0 0" }}>{queueScopeFromView(view)}</Typography.Title>
        </Card>
      </Space>

      <Space>
        <Button type={view === "unassigned" ? "primary" : "default"} onClick={() => setView("unassigned")}>Sin asignar</Button>
        <Button type={view === "mine" ? "primary" : "default"} onClick={() => setView("mine")}>Asignados a mí</Button>
        <Button type={view === "all" ? "primary" : "default"} onClick={() => setView("all")}>Todos</Button>
      </Space>

      <Card>
        <Space>
          <Select
              aria-label="Estado"
              value={statusFilter}
              onChange={(value) => setStatusFilter(value as QueueFilter)}
              options={statusFilterOptions}
              style={{ minWidth: 220 }}
            />
          <Input
            value={inputQuery}
            onChange={(event) => setInputQuery(event.target.value)}
            placeholder="Buscar por título o ID"
          />
          <Button onClick={onApplySearch}>Buscar</Button>
          <Button onClick={onClearFilters}>Limpiar filtros</Button>
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
              message="No se ha podido cargar la cola de tickets"
              description={errorMessage}
            />
          )}

          {loadState === "ready" && tickets.length === 0 && (
            <Empty description="No hay tickets para mostrar con los filtros actuales" />
          )}

          {loadState === "ready" && tickets.length > 0 && (
            <Table<TicketSummary> rowKey="id" columns={columns} dataSource={tickets} pagination={{ pageSize: 10 }} />
          )}
        </Space>
      </Card>
    </Space>
  );
}
