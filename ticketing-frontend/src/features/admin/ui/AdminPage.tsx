import React, { useEffect, useMemo, useState } from "react";
import { Button, Input, Modal, Space, Switch, Table, Tabs, Tag, Typography, message } from "antd";
import { adminApi } from "../api/adminApi";
import type { AdminCategory, AdminUser } from "../model/types";

type SimpleColumn<T> = {
  title: string;
  key: string;
  dataIndex?: keyof T;
  render?: (value: unknown, row: T) => React.ReactNode;
};

export function AdminPage() {
  const [categories, setCategories] = useState<AdminCategory[]>([]);
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [loadingCategories, setLoadingCategories] = useState(true);
  const [loadingUsers, setLoadingUsers] = useState(true);
  const [creatingCategory, setCreatingCategory] = useState(false);
  const [newCategoryName, setNewCategoryName] = useState("");
  const [editingCategory, setEditingCategory] = useState<AdminCategory | null>(null);
  const [editingName, setEditingName] = useState("");
  const [editingActive, setEditingActive] = useState(true);

  async function loadCategories() {
    setLoadingCategories(true);
    try {
      const data = await adminApi.getCategories();
      setCategories(data);
    } catch {
      message.error("No se pudieron cargar las categorías");
    } finally {
      setLoadingCategories(false);
    }
  }

  async function loadUsers() {
    setLoadingUsers(true);
    try {
      const data = await adminApi.getUsers();
      setUsers(data);
    } catch {
      message.error("No se pudieron cargar los usuarios");
    } finally {
      setLoadingUsers(false);
    }
  }

  useEffect(() => {
    void Promise.all([loadCategories(), loadUsers()]);
  }, []);

  const categoryColumns = useMemo<SimpleColumn<AdminCategory>[]>(
    () => [
      { title: "Nombre", dataIndex: "name", key: "name" },
      {
        title: "Estado",
        key: "isActive",
        render: (_: unknown, record: AdminCategory) =>
          record.isActive ? <Tag color="success">Activa</Tag> : <Tag>Inactiva</Tag>,
      },
      {
        title: "Acciones",
        key: "actions",
        render: (_: unknown, record: AdminCategory) => (
          <Button
            onClick={() => {
              setEditingCategory(record);
              setEditingName(record.name);
              setEditingActive(record.isActive);
            }}
          >
            Editar
          </Button>
        ),
      },
    ],
    [],
  );

  const userColumns = useMemo<SimpleColumn<AdminUser>[]>(
    () => [
      { title: "Nombre", dataIndex: "displayName", key: "displayName" },
      { title: "Email", dataIndex: "email", key: "email" },
      {
        title: "Rol",
        dataIndex: "role",
        key: "role",
        render: (role: unknown) => <Tag>{String(role)}</Tag>,
      },
      {
        title: "Activo",
        key: "isActive",
        render: (_: unknown, record: AdminUser) => (
          <Switch
            checked={record.isActive}
            onChange={async (checked) => {
              try {
                const updated = await adminApi.updateUserActive(record.id, checked);
                setUsers((prev) => prev.map((user) => (user.id === updated.id ? updated : user)));
                message.success("Estado de usuario actualizado");
              } catch {
                message.error("No se pudo actualizar el estado del usuario");
              }
            }}
          />
        ),
      },
    ],
    [],
  );

  return (
    <Space direction="vertical" size={16} style={{ width: "100%" }}>
      <Typography.Title level={3} style={{ margin: 0 }}>
        <span data-testid="admin-title">Administración</span>
      </Typography.Title>
      <Typography.Text type="secondary">
        Gestiona categorías y usuarios de la plataforma. Solo los administradores tienen acceso.
      </Typography.Text>

      <Tabs
        items={[
          {
            key: "categories",
            label: "Categorías",
            children: (
              <Space direction="vertical" size={16} style={{ width: "100%" }}>
                <Space>
                  <Input
                    value={newCategoryName}
                    onChange={(event) => setNewCategoryName(event.target.value)}
                    placeholder="Nueva categoría"
                  />
                  <Button
                    type="primary"
                    loading={creatingCategory}
                    onClick={async () => {
                      const name = newCategoryName.trim();
                      if (!name) {
                        message.error("Nombre obligatorio");
                        return;
                      }

                      setCreatingCategory(true);
                      try {
                        const created = await adminApi.createCategory(name);
                        setCategories((prev) =>
                          [...prev, created].sort((a, b) => a.name.localeCompare(b.name)),
                        );
                        setNewCategoryName("");
                        message.success("Categoría creada");
                      } catch {
                        message.error("No se pudo crear la categoría (puede que ya exista)");
                      } finally {
                        setCreatingCategory(false);
                      }
                    }}
                  >
                    Crear
                  </Button>
                </Space>

                <Table
                  rowKey="id"
                  loading={loadingCategories}
                  dataSource={categories}
                  columns={categoryColumns}
                  pagination={false}
                />
              </Space>
            ),
          },
          {
            key: "users",
            label: "Usuarios",
            children: (
              <Table
                rowKey="id"
                loading={loadingUsers}
                dataSource={users}
                columns={userColumns}
                pagination={false}
              />
            ),
          },
        ]}
      />

      <Modal
        title="Editar categoría"
        open={Boolean(editingCategory)}
        onCancel={() => setEditingCategory(null)}
        onOk={async () => {
          if (!editingCategory) {
            return;
          }

          const name = editingName.trim();
          if (!name) {
            message.error("Nombre obligatorio");
            return;
          }

          try {
            const updated = await adminApi.updateCategory(editingCategory.id, {
              name,
              isActive: editingActive,
            });
            setCategories((prev) =>
              prev.map((category) => (category.id === updated.id ? updated : category)),
            );
            setEditingCategory(null);
            message.success("Categoría actualizada");
          } catch {
            message.error("No se pudo actualizar la categoría");
          }
        }}
        okText="Guardar"
      >
        <Space direction="vertical" style={{ width: "100%" }}>
          <Input
            value={editingName}
            onChange={(event) => setEditingName(event.target.value)}
            placeholder="Nombre"
          />
          <Space>
            <Typography.Text>Activa</Typography.Text>
            <Switch checked={editingActive} onChange={setEditingActive} />
          </Space>
        </Space>
      </Modal>
    </Space>
  );
}
