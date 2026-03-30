import { useEffect, useMemo, useState } from "react";
import { Alert, Button, Empty, Skeleton, Space, Table, Tag, Typography } from "antd";
import type { TableProps } from "antd";
import { useNavigate } from "react-router-dom";
import { ticketsApi } from "../../api/ticketsApi";
import { ticketPriorityLabel } from "../../model/presentation";
import type { TicketPriority, TicketStatus, TicketSummary } from "../../model/types";
import { TicketStatusBadge } from "../shared/TicketStatusBadge";

type LoadState = "loading" | "ready" | "error";

function formatDate(isoDate: string) {
  return new Intl.DateTimeFormat("es-ES", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(isoDate));
}

export function UserTicketsHomePage() {
  const navigate = useNavigate();
  const [tickets, setTickets] = useState<TicketSummary[]>([]);
  const [loadState, setLoadState] = useState<LoadState>("loading");
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    let isMounted = true;

    const loadTickets = async () => {
      setLoadState("loading");
      setErrorMessage(null);

      try {
        const response = await ticketsApi.getMyTickets();

        if (!isMounted) return;

        setTickets(response.items);
        setLoadState("ready");
      } catch (error) {
        if (!isMounted) return;

        setLoadState("error");
        setErrorMessage(
          error instanceof Error ? error.message : "No se pudieron cargar tus tickets.",
        );
      }
    };

    void loadTickets();

    return () => {
      isMounted = false;
    };
  }, []);

  const columns: TableProps<TicketSummary>["columns"] = useMemo(
    () => [
      {
        title: "ID",
        dataIndex: "id",
        key: "id",
        width: 240,
        ellipsis: true,
      },
      {
        title: "Título",
        dataIndex: "title",
        key: "title",
      },
      {
        title: "Estado",
        dataIndex: "status",
        key: "status",
        width: 140,
        render: (statusValue: unknown) => {
          const status = statusValue as TicketStatus;
          return <TicketStatusBadge status={status} />;
        },
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
        title: "Fecha creación",
        dataIndex: "createdAt",
        key: "createdAt",
        width: 220,
        render: (createdAtValue: unknown) => formatDate(String(createdAtValue)),
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
    <Space direction="vertical" size={20} style={{ width: "100%" }}>
      <Space style={{ width: "100%", justifyContent: "space-between" }}>
        <Typography.Title level={3} style={{ margin: 0 }}>
          <span data-testid="user-tickets-title">Mis tickets</span>
        </Typography.Title>
        <Button
          data-testid="user-create-ticket-cta"
          type="primary"
          size="large"
          onClick={() => navigate("/tickets/new")}
        >
          Crear ticket
        </Button>
      </Space>

      {loadState === "loading" && <Skeleton active paragraph={{ rows: 6 }} />}

      {loadState === "error" && (
        <Alert
          type="error"
          showIcon
          message="No hemos podido cargar tus tickets"
          description={errorMessage}
        />
      )}

      {loadState === "ready" && tickets.length === 0 && (
        <Empty description="Aún no tienes tickets creados" />
      )}

      {loadState === "ready" && tickets.length > 0 && (
        <Table<TicketSummary>
          rowKey="id"
          columns={columns}
          dataSource={tickets}
          pagination={{ pageSize: 10, hideOnSinglePage: true }}
        />
      )}
    </Space>
  );
}
