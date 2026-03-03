import { Alert, Button, Card, Empty, Space, Table, Tag, Typography } from "antd";
import type { TableProps } from "antd";
import { useMemo, useState } from "react";

type AgentTicketQueueItem = {
  id: string;
  title: string;
  status: "OPEN" | "IN_PROGRESS" | "RESOLVED";
  priority: "LOW" | "MEDIUM" | "HIGH" | "URGENT";
  assignee: string | null;
  updatedAt: string;
};

type QueueView = "unassigned" | "mine" | "all";

const STATUS_LABEL: Record<AgentTicketQueueItem["status"], string> = {
  OPEN: "Abierto",
  IN_PROGRESS: "En progreso",
  RESOLVED: "Resuelto",
};

const PRIORITY_LABEL: Record<AgentTicketQueueItem["priority"], string> = {
  LOW: "Baja",
  MEDIUM: "Media",
  HIGH: "Alta",
  URGENT: "Urgente",
};

const METRICS = [
  { title: "Sin asignar", value: "--" },
  { title: "Asignados a mí", value: "--" },
  { title: "En progreso", value: "--" },
  { title: "SLA en riesgo", value: "--" },
] as const;

const EMPTY_QUEUE: AgentTicketQueueItem[] = [];

function formatDate(isoDate: string) {
  return new Intl.DateTimeFormat("es-ES", {
    dateStyle: "short",
    timeStyle: "short",
  }).format(new Date(isoDate));
}

export function AgentAdminTicketsPage() {
  const [view, setView] = useState<QueueView>("unassigned");

  const columns: TableProps<AgentTicketQueueItem>["columns"] = useMemo(
    () => [
      { title: "ID", dataIndex: "id", key: "id", width: 180 },
      { title: "Título", dataIndex: "title", key: "title" },
      {
        title: "Estado",
        dataIndex: "status",
        key: "status",
        width: 140,
        render: (statusValue: unknown) => <Tag>{STATUS_LABEL[statusValue as AgentTicketQueueItem["status"]]}</Tag>,
      },
      {
        title: "Prioridad",
        dataIndex: "priority",
        key: "priority",
        width: 140,
        render: (priorityValue: unknown) => <Tag>{PRIORITY_LABEL[priorityValue as AgentTicketQueueItem["priority"]]}</Tag>,
      },
      {
        title: "Asignado",
        dataIndex: "assignee",
        key: "assignee",
        width: 160,
        render: (assigneeValue: unknown) => (assigneeValue ? String(assigneeValue) : "Sin asignar"),
      },
      {
        title: "Actualizado",
        dataIndex: "updatedAt",
        key: "updatedAt",
        width: 180,
        render: (updatedAtValue: unknown) => formatDate(String(updatedAtValue)),
      },
    ],
    [],
  );

  const viewLabel =
    view === "unassigned"
      ? "Cola sin asignar"
      : view === "mine"
        ? "Tickets asignados a mí"
        : "Todos los tickets";

  return (
    <Space direction="vertical" size={16} style={{ width: "100%" }}>
      <Typography.Title level={3} style={{ margin: 0 }}>
        Gestión de tickets
      </Typography.Title>

      <Space style={{ display: "grid", gridTemplateColumns: "repeat(4, minmax(0, 1fr))" }}>
        {METRICS.map((metric) => (
          <Card key={metric.title}>
            <Typography.Text type="secondary">{metric.title}</Typography.Text>
            <Typography.Title level={3} style={{ margin: "8px 0 0" }}>
              {metric.value}
            </Typography.Title>
          </Card>
        ))}
      </Space>

      <Alert
        type="info"
        showIcon
        message="Panel AGENT/ADMIN preparado"
        description="La estructura de cola, métricas y tabla ya está lista. En cuanto estén los endpoints de listado/filtros se conectará con datos reales."
      />

      <Space>
        <Button type={view === "unassigned" ? "primary" : "default"} onClick={() => setView("unassigned")}>Sin asignar</Button>
        <Button type={view === "mine" ? "primary" : "default"} onClick={() => setView("mine")}>Asignados a mí</Button>
        <Button type={view === "all" ? "primary" : "default"} onClick={() => setView("all")}>Todos</Button>
      </Space>

      <Card>
        <Space direction="vertical" size={12} style={{ width: "100%" }}>
          <Typography.Title level={4} style={{ margin: 0 }}>
            {viewLabel}
          </Typography.Title>

          {EMPTY_QUEUE.length === 0 ? (
            <Empty description="No hay tickets para mostrar con los filtros actuales" />
          ) : (
            <Table<AgentTicketQueueItem> rowKey="id" columns={columns} dataSource={EMPTY_QUEUE} pagination={{ pageSize: 10 }} />
          )}
        </Space>
      </Card>
    </Space>
  );
}
