import { useEffect, useMemo, useState } from "react";
import { Alert, Button, Card, Form, Input, Select, Space, Typography } from "antd";
import { useNavigate } from "react-router-dom";
import { ticketsApi } from "../../api/ticketsApi";
import type { TicketCategory, TicketPriority } from "../../model/types";

const priorityOptions: Array<{ value: TicketPriority; label: string }> = [
  { value: "LOW", label: "Baja" },
  { value: "MEDIUM", label: "Media" },
  { value: "HIGH", label: "Alta" },
];

export function CreateTicketPage() {
  const navigate = useNavigate();
  const [categories, setCategories] = useState<TicketCategory[]>([]);
  const [isLoadingCategories, setIsLoadingCategories] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [categoryId, setCategoryId] = useState("");
  const [priority, setPriority] = useState<TicketPriority>("MEDIUM");

  const isSubmitDisabled = useMemo(() => {
    return !title.trim() || !description.trim() || !categoryId || !priority;
  }, [categoryId, description, priority, title]);

  useEffect(() => {
    let isMounted = true;

    const loadCategories = async () => {
      setIsLoadingCategories(true);
      setErrorMessage(null);

      try {
        const loadedCategories = await ticketsApi.getCategories();
        if (!isMounted) return;

        setCategories(loadedCategories);
      } catch (error) {
        if (!isMounted) return;
        setErrorMessage(error instanceof Error ? error.message : "No se pudieron cargar las categorías.");
      } finally {
        if (isMounted) {
          setIsLoadingCategories(false);
        }
      }
    };

    void loadCategories();

    return () => {
      isMounted = false;
    };
  }, []);

  const handleSubmit = async () => {
    if (isSubmitDisabled) {
      return;
    }

    setIsSubmitting(true);
    setErrorMessage(null);

    try {
      const response = await ticketsApi.createTicket({
        title: title.trim(),
        description: description.trim(),
        categoryId,
        priority,
      });
      navigate(`/tickets/${response.ticketId}`);
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : "No se pudo crear el ticket.");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Card>
      <Space direction="vertical" size={20} style={{ width: "100%" }}>
        <Typography.Title level={3} style={{ margin: 0 }}>
          Crear ticket
        </Typography.Title>

        {errorMessage && <Alert showIcon type="error" message="Error al crear ticket" description={errorMessage} />}

        <Form layout="vertical" onFinish={handleSubmit}>
          <Space direction="vertical" size={16} style={{ width: "100%" }}>
            <Form.Item label="Título" style={{ marginBottom: 0 }}>
              <Input
                aria-label="Título"
                value={title}
                onChange={(event) => setTitle(event.target.value)}
                placeholder="Describe brevemente tu incidencia"
                maxLength={200}
              />
            </Form.Item>

            <Form.Item label="Descripción" style={{ marginBottom: 0 }}>
              <Input.TextArea
                aria-label="Descripción"
                value={description}
                onChange={(event) => setDescription(event.target.value)}
                placeholder="Explica qué ocurre, cuándo empezó y qué has probado"
                maxLength={1000}
                rows={6}
              />
            </Form.Item>

            <Form.Item label="Categoría" style={{ marginBottom: 0 }}>
              <Select
                aria-label="Categoría"
                value={categoryId || undefined}
                onChange={(value) => setCategoryId(value)}
                disabled={isLoadingCategories}
                placeholder="Selecciona una categoría"
                options={categories.map((category) => ({
                  label: category.name,
                  value: category.id,
                }))}
              />
            </Form.Item>

            <Form.Item label="Prioridad" style={{ marginBottom: 0 }}>
              <Select
                aria-label="Prioridad"
                value={priority}
                onChange={(value) => setPriority(value as TicketPriority)}
                options={priorityOptions}
              />
            </Form.Item>

            <Button
              type="primary"
              htmlType="submit"
              loading={isSubmitting}
              disabled={isSubmitDisabled || isLoadingCategories}
              style={{ backgroundColor: "#389e0d" }}
            >
              Crear ticket
            </Button>
          </Space>
        </Form>
      </Space>
    </Card>
  );
}
