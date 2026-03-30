import { useEffect, useState } from "react";
import type { CSSProperties } from "react";
import { Alert, Button, Card, Space, Typography } from "antd";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../features/auth/hooks/useAuth";
import { authApi } from "../../features/auth/api/authApi";
import type { ProfileResponse } from "../../features/auth/model/types";

const rowStyle: CSSProperties = {
  display: "grid",
  gridTemplateColumns: "160px 1fr",
  gap: 12,
  padding: "8px 0",
  borderBottom: "1px solid #f3f4f6",
};

export function ProfilePage() {
  const navigate = useNavigate();
  const { state, logout } = useAuth();
  const [profile, setProfile] = useState<ProfileResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!state.token) return;

    authApi
      .me(state.token)
      .then((data) => {
        setProfile(data);
      })
      .catch((err: Error) => {
        setError(err.message || "No se pudo cargar el perfil");
      });
  }, [state.token]);

  const onLogout = () => {
    logout();
    navigate("/login", { replace: true });
  };

  const data =
    profile ??
    (state.user
      ? {
          sub: state.user.id,
          email: state.user.email,
          displayName: state.user.displayName,
          role: state.user.role,
          roles: state.user.roles,
        }
      : null);

  return (
    <Space direction="vertical" size={16} style={{ width: "100%", maxWidth: 820 }}>
      <div>
        <Typography.Title level={3} style={{ marginBottom: 0 }}>
          Perfil
        </Typography.Title>
        <Typography.Text type="secondary">
          Información de sesión actual basada en JWT.
        </Typography.Text>
      </div>

      {error && <Alert type="error" showIcon message={error} />}

      <Card>
        {data ? (
          <div>
            <div style={rowStyle}>
              <strong>ID (sub)</strong>
              <span>{data.sub}</span>
            </div>
            <div style={rowStyle}>
              <strong>Email</strong>
              <span>{data.email}</span>
            </div>
            <div style={rowStyle}>
              <strong>Display name</strong>
              <span>{data.displayName}</span>
            </div>
            <div style={{ ...rowStyle, borderBottom: "none" }}>
              <strong>Rol</strong>
              <span>{data.role}</span>
            </div>
          </div>
        ) : (
          <Typography.Text type="secondary">No hay datos de perfil disponibles.</Typography.Text>
        )}
      </Card>

      <Button type="default" onClick={onLogout} style={{ width: "fit-content" }}>
        Logout
      </Button>
    </Space>
  );
}
