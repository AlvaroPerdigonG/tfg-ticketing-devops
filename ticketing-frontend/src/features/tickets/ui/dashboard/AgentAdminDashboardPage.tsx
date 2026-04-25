import { Alert, Card, Empty, Progress, Skeleton, Space, Typography } from "antd";
import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { ticketsApi } from "../../api/ticketsApi";
import type { DashboardAgentCount, DashboardStats, TicketStatus } from "../../model/types";

type LoadState = "loading" | "ready" | "error";

type DashboardMetric = {
  key: string;
  label: string;
  value: number;
  view: "unassigned" | "mine" | "all";
  status?: TicketStatus;
};

function buildTicketsLink(metric: DashboardMetric) {
  const params = new URLSearchParams();
  params.set("view", metric.view);
  if (metric.status) params.set("status", metric.status);
  return `/tickets?${params.toString()}`;
}

function AgentBarChart({
  title,
  data,
  emptyMessage,
}: {
  title: string;
  data: DashboardAgentCount[];
  emptyMessage: string;
}) {
  const maxCount = data.length > 0 ? Math.max(...data.map((item) => item.count)) : 0;

  return (
    <Card>
      <Space direction="vertical" size={12} style={{ width: "100%" }}>
        <Typography.Title level={4} style={{ margin: 0 }}>
          {title}
        </Typography.Title>

        {data.length === 0 ? (
          <Empty description={emptyMessage} />
        ) : (
          <Space direction="vertical" size={10} style={{ width: "100%" }}>
            {data.map((item) => {
              const percentage = maxCount === 0 ? 0 : (item.count / maxCount) * 100;

              return (
                <div key={item.assigneeUserId}>
                  <Space style={{ width: "100%", justifyContent: "space-between" }}>
                    <Typography.Text>{item.assigneeDisplayName}</Typography.Text>
                    <Typography.Text strong>{item.count}</Typography.Text>
                  </Space>
                  <Progress
                    percent={Number(percentage.toFixed(2))}
                    showInfo={false}
                    strokeColor="#389e0d"
                  />
                </div>
              );
            })}
          </Space>
        )}
      </Space>
    </Card>
  );
}

export function AgentAdminDashboardPage() {
  const navigate = useNavigate();
  const [loadState, setLoadState] = useState<LoadState>("loading");
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [stats, setStats] = useState<DashboardStats | null>(null);

  useEffect(() => {
    let isMounted = true;

    const loadDashboard = async () => {
      setLoadState("loading");
      setErrorMessage(null);

      try {
        const response = await ticketsApi.getDashboardStats();

        if (!isMounted) return;

        setStats(response);
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

  const metrics: DashboardMetric[] = useMemo(() => {
    if (!stats) return [];

    return [
      {
        key: "unassigned",
        label: "Tickets sin asignar",
        value: stats.cards.unassigned,
        view: "unassigned",
      },
      {
        key: "mine",
        label: "Tickets asignados a mí",
        value: stats.cards.assignedToMe,
        view: "mine",
      },
      {
        key: "inProgress",
        label: "WIP (en progreso)",
        value: stats.cards.inProgress,
        view: "all",
        status: "IN_PROGRESS",
      },
      {
        key: "onHold",
        label: "Esperando respuesta",
        value: stats.cards.onHold,
        view: "all",
        status: "ON_HOLD",
      },
    ];
  }, [stats]);

  return (
    <Space direction="vertical" size={16} style={{ width: "100%" }}>
      <Typography.Title level={3} style={{ margin: 0 }}>
        Dashboard de soporte
      </Typography.Title>
      <Typography.Text type="secondary">
        Resumen operativo y estadístico para agentes y administradores.
      </Typography.Text>

      {loadState === "loading" && <Skeleton active paragraph={{ rows: 8 }} />}

      {loadState === "error" && (
        <Alert
          type="error"
          showIcon
          message="No se ha podido cargar el dashboard"
          description={errorMessage}
        />
      )}

      {loadState === "ready" && stats && (
        <>
          <div
            style={{
              display: "grid",
              gridTemplateColumns: "repeat(4, minmax(0, 1fr))",
              gap: 12,
            }}
          >
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
                    padding: 0,
                  }}
                >
                  <Typography.Text type="secondary">{metric.label}</Typography.Text>
                  <Typography.Title level={2} style={{ margin: "8px 0 0" }}>
                    {metric.value}
                  </Typography.Title>
                </button>
              </Card>
            ))}
          </div>

          <div
            style={{
              display: "grid",
              gridTemplateColumns: "repeat(2, minmax(0, 1fr))",
              gap: 12,
            }}
          >
            <AgentBarChart
              title="Tickets cerrados por agente/admin"
              data={stats.charts.resolvedByAgent}
              emptyMessage="No hay tickets cerrados asignados"
            />
            <AgentBarChart
              title="Tickets asignados por agente/admin"
              data={stats.charts.assignedByAgent}
              emptyMessage="No hay tickets asignados"
            />
          </div>
        </>
      )}
    </Space>
  );
}
