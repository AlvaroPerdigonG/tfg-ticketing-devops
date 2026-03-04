import { type FormEvent, useEffect, useMemo, useState } from "react";
import { Alert, Button, Card, Space, Typography } from "antd";
import { useNavigate } from "react-router-dom";
import { ticketsApi } from "../api/ticketsApi";
import type { TicketCategory, TicketPriority } from "../model/types";

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

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

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

        <form onSubmit={handleSubmit}>
          <Space direction="vertical" size={16} style={{ width: "100%" }}>
            <label>
              Título
              <input
                aria-label="Título"
                value={title}
                onChange={(event) => setTitle(event.target.value)}
                placeholder="Describe brevemente tu incidencia"
                maxLength={200}
                style={{ width: "100%", marginTop: 6, padding: 8 }}
              />
            </label>

            <label>
              Descripción
              <textarea
                aria-label="Descripción"
                value={description}
                onChange={(event) => setDescription(event.target.value)}
                placeholder="Explica qué ocurre, cuándo empezó y qué has probado"
                maxLength={1000}
                rows={6}
                style={{ width: "100%", marginTop: 6, padding: 8 }}
              />
            </label>

            <label>
              Categoría
              <select
                aria-label="Categoría"
                value={categoryId}
                onChange={(event) => setCategoryId(event.target.value)}
                disabled={isLoadingCategories}
                style={{ width: "100%", marginTop: 6, padding: 8 }}
              >
                <option value="">Selecciona una categoría</option>
                {categories.map((category) => (
                  <option key={category.id} value={category.id}>
                    {category.name}
                  </option>
                ))}
              </select>
            </label>

            <label>
              Prioridad
              <select
                aria-label="Prioridad"
                value={priority}
                onChange={(event) => setPriority(event.target.value as TicketPriority)}
                style={{ width: "100%", marginTop: 6, padding: 8 }}
              >
                {priorityOptions.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            </label>

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
        </form>
      </Space>
    </Card>
  );
}
