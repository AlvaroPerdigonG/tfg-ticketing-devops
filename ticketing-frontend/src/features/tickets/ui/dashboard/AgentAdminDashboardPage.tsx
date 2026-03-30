import { Alert, Button, Card, Empty, Skeleton, Space, Table, Tag, Typography } from "antd";
import type { TableProps } from "antd";
import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { ticketsApi } from "../../api/ticketsApi";
import { ticketPriorityLabel } from "../../model/presentation";
import type { TicketPriority, TicketStatus, TicketSummary } from "../../model/types";
import { TicketStatusBadge } from "../shared/TicketStatusBadge";

type LoadState = "loading" | "ready" | "error";

type DashboardMetric = {
  key: string;
  label: string;
  value: number;
  view: "unassigned" | "mine" | "all";
  status?: TicketStatus;
};

function formatDate(isoDate: string) {
  return new Intl.DateTimeFormat("es-ES", {
    dateStyle: "short",
    timeStyle: "short",
  }).format(new Date(isoDate));
}

function buildTicketsLink(metric: DashboardMetric) {
  const params = new URLSearchParams();
  params.set("view", metric.view);
  if (metric.status) params.set("status", metric.status);
  return `/tickets?${params.toString()}`;
}

export function AgentAdminDashboardPage() {
  const navigate = useNavigate();
  const [loadState, setLoadState] = useState<LoadState>("loading");
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [attentionTickets, setAttentionTickets] = useState<TicketSummary[]>([]);
  const [unassignedCount, setUnassignedCount] = useState(0);
  const [mineCount, setMineCount] = useState(0);
  const [inProgressCount, setInProgressCount] = useState(0);
  const [waitingResponseCount, setWaitingResponseCount] = useState(0);

  useEffect(() => {
    let isMounted = true;

    const loadDashboard = async () => {
      setLoadState("loading");
      setErrorMessage(null);

      try {
        const [unassigned, mine, inProgress, waitingResponse] = await Promise.all([
          ticketsApi.getQueueTickets({ scope: "UNASSIGNED", page: 0, size: 1 }),
          ticketsApi.getQueueTickets({ scope: "MINE", page: 0, size: 1 }),
          ticketsApi.getQueueTickets({ scope: "ALL", status: "IN_PROGRESS", page: 0, size: 1 }),
          ticketsApi.getQueueTickets({ scope: "MINE", status: "OPEN", page: 0, size: 20 }),
        ]);

        if (!isMounted) return;

        const requiresAttention = waitingResponse.items.filter(
          (ticket) => ticket.assignedToUserId !== null,
        );

        setUnassignedCount(unassigned.total);
        setMineCount(mine.total);
        setInProgressCount(inProgress.total);
        setWaitingResponseCount(waitingResponse.total);
        setAttentionTickets(requiresAttention.slice(0, 8));
        setLoadState("ready");
      } catch (error) {
        if (!isMounted) return;

        setLoadState("error");
        setErrorMessage(
          error instanceof Error ? error.message : "No se pudo cargar el dashboard de tickets.",
        );
      }
    };

    void loadDashboard();

    return () => {
      isMounted = false;
    };
  }, []);

  const metrics: DashboardMetric[] = [
    { key: "unassigned", label: "Sin asignar", value: unassignedCount, view: "unassigned" },
    { key: "mine", label: "Asignados a mí", value: mineCount, view: "mine" },
    {
      key: "inProgress",
      label: "En progreso",
      value: inProgressCount,
      view: "all",
      status: "IN_PROGRESS",
    },
    {
      key: "waitingResponse",
      label: "Esperando respuesta",
      value: waitingResponseCount,
      view: "mine",
      status: "OPEN",
    },
  ];

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
          <TicketStatusBadge status={statusValue as TicketStatus} />
        ),
      },
      {
        title: "Prioridad",
        dataIndex: "priority",
        key: "priority",
        width: 120,
        render: (priorityValue: unknown) => (
          <Tag>{ticketPriorityLabel[priorityValue as TicketPriority]}</Tag>
        ),
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

  return (
    <Space direction="vertical" size={16} style={{ width: "100%" }}>
      <Typography.Title level={3} style={{ margin: 0 }}>
        Dashboard de soporte
      </Typography.Title>
      <Typography.Text type="secondary">
        Resumen operativo para agentes y administradores.
      </Typography.Text>

      {loadState === "loading" && <Skeleton active paragraph={{ rows: 5 }} />}

      {loadState === "error" && (
        <Alert
          type="error"
          showIcon
          message="No se ha podido cargar el dashboard"
          description={errorMessage}
        />
      )}

      {loadState === "ready" && (
        <>
          <Space style={{ display: "grid", gridTemplateColumns: "repeat(4, minmax(0, 1fr))" }}>
            {metrics.map((metric) => (
              <Card key={metric.key}>
                <button
                  type="button"
                  onClick={() => navigate(buildTicketsLink(metric))}
                  style={{
                    border: "none",
                    background: "transparent",
                    cursor: "pointer",
                    textAlign: "left",
                    width: "100%",
                  }}
                >
                  <Typography.Text type="secondary">{metric.label}</Typography.Text>
                  <Typography.Title level={3} style={{ margin: "8px 0 0" }}>
                    {metric.value}
                  </Typography.Title>
                </button>
              </Card>
            ))}
          </Space>

          <Card>
            <Space direction="vertical" size={12} style={{ width: "100%" }}>
              <Typography.Title level={4} style={{ margin: 0 }}>
                Requieren atención
              </Typography.Title>

              {attentionTickets.length === 0 ? (
                <Empty description="No hay tickets pendientes de atención inmediata" />
              ) : (
                <Table<TicketSummary>
                  rowKey="id"
                  columns={columns}
                  dataSource={attentionTickets}
                  pagination={false}
                />
              )}
            </Space>
          </Card>
        </>
      )}
    </Space>
  );
}
