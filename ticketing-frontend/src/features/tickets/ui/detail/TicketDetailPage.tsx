import { Alert, Button, Card, Empty, Skeleton, Space, Tag, Typography } from "antd";
import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useAuth } from "../../../auth/hooks/useAuth";
import { ticketsApi } from "../../api/ticketsApi";
import { ticketPriorityLabel, ticketStatusColor, ticketStatusLabel } from "../../model/presentation";
import type { TicketDetail, TicketStatus } from "../../model/types";

type LoadState = "loading" | "ready" | "error";

function formatDate(isoDate: string) {
  return new Intl.DateTimeFormat("es-ES", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(isoDate));
}

export function TicketDetailPage() {
  const { id } = useParams<{ id: string }>();
  const { hasAnyRole } = useAuth();
  const navigate = useNavigate();

  const canManage = hasAnyRole(["AGENT", "ADMIN"]);
  const [loadState, setLoadState] = useState<LoadState>("loading");
  const [ticket, setTicket] = useState<TicketDetail | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [busyAction, setBusyAction] = useState<"assign" | "status" | null>(null);

  const availableStatusTransitions = useMemo(() => {
    if (!ticket) return [] as TicketStatus[];
    if (ticket.status === "OPEN") return ["IN_PROGRESS"] as TicketStatus[];
    if (ticket.status === "IN_PROGRESS") return ["RESOLVED"] as TicketStatus[];
    return [] as TicketStatus[];
  }, [ticket]);

  useEffect(() => {
    if (!id) {
      setLoadState("error");
      setErrorMessage("Id de ticket inválido");
      return;
    }

    let mounted = true;

    const load = async () => {
      setLoadState("loading");
      setErrorMessage(null);

      try {
        const response = await ticketsApi.getTicketById(id);
        if (!mounted) return;
        setTicket(response);
        setLoadState("ready");
      } catch (error) {
        if (!mounted) return;
        setLoadState("error");
        setErrorMessage(error instanceof Error ? error.message : "No se pudo cargar el ticket.");
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
      setErrorMessage(error instanceof Error ? error.message : "No se pudo asignar el ticket.");
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
      setErrorMessage(error instanceof Error ? error.message : "No se pudo cambiar el estado.");
    } finally {
      setBusyAction(null);
    }
  };

  if (loadState === "loading") {
    return <Skeleton active paragraph={{ rows: 10 }} />;
  }

  if (loadState === "error" || !ticket) {
    return (
      <Space direction="vertical" style={{ width: "100%" }} size={16}>
        <Alert type="error" showIcon message="Error cargando el ticket" description={errorMessage} />
        <Button onClick={() => navigate("/tickets")}>Volver a tickets</Button>
      </Space>
    );
  }

  return (
    <Space direction="vertical" size={16} style={{ width: "100%" }}>
      {errorMessage && <Alert type="error" showIcon message="No se pudo completar la acción" description={errorMessage} />}

      <div style={{ display: "grid", gridTemplateColumns: "2fr 3fr 1.5fr", gap: 16 }}>
        <div>
          <Card>
            <Space direction="vertical" size={12} style={{ width: "100%" }}>
              <Typography.Title level={4} style={{ margin: 0 }}>{ticket.title}</Typography.Title>
              <Tag color={ticketStatusColor[ticket.status]}>{ticketStatusLabel[ticket.status]}</Tag>
              <Tag>{ticketPriorityLabel[ticket.priority]}</Tag>
              <Typography.Title level={5} style={{ margin: 0 }}>Metadata</Typography.Title>
              <Typography.Text>ID: {ticket.id}</Typography.Text>
              <Typography.Text>Categoría: {ticket.categoryId}</Typography.Text>
              <Typography.Text>Creado por: {ticket.createdByUserId}</Typography.Text>
              <Typography.Text>Asignado a: {ticket.assignedToUserId ?? "Sin asignar"}</Typography.Text>
              <Typography.Text>Creado: {formatDate(ticket.createdAt)}</Typography.Text>
              <Typography.Text>Actualizado: {formatDate(ticket.updatedAt)}</Typography.Text>
            </Space>
          </Card>
        </div>

        <div>
          <Card>
            <Space direction="vertical" size={12} style={{ width: "100%" }}>
              <Typography.Title level={5} style={{ margin: 0 }}>Conversación</Typography.Title>
              <Typography.Paragraph>{ticket.description}</Typography.Paragraph>
              <Empty description="Sin comentarios todavía" />
            </Space>
          </Card>
        </div>

        <div>
          <Card>
            <Typography.Title level={5} style={{ margin: "0 0 12px" }}>Acciones</Typography.Title>
            {!canManage && (
              <Alert
                showIcon
                type="info"
                message="Vista de solo lectura"
                description="Tu rol solo puede ver el detalle y la conversación del ticket."
              />
            )}

            {canManage && (
              <Space direction="vertical" style={{ width: "100%" }} size={12}>
                <Button loading={busyAction === "assign"} onClick={handleAssignToMe}>Asignarme ticket</Button>
                <Typography.Title level={5} style={{ margin: 0 }}>Cambiar estado</Typography.Title>
                {availableStatusTransitions.length === 0 && <Typography.Text type="secondary">No hay transiciones disponibles.</Typography.Text>}
                {availableStatusTransitions.map((nextStatus) => (
                  <Button
                    key={nextStatus}
                    type="primary"
                    loading={busyAction === "status"}
                    onClick={() => handleStatusChange(nextStatus)}
                  >
                    Mover a {ticketStatusLabel[nextStatus]}
                  </Button>
                ))}
              </Space>
            )}
          </Card>
        </div>
      </div>
    </Space>
  );
}
