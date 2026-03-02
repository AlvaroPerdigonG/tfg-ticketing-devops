import { Layout, Menu, Typography } from "antd";
import type { MenuProps } from "antd";
import { Outlet, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../../features/auth/hooks/useAuth";

type AppMenuItem = Required<NonNullable<MenuProps["items"]>>[number];

const { Header, Sider, Content } = Layout;

const baseItems: AppMenuItem[] = [
  { key: "/dashboard", label: "Dashboard" },
  { key: "/tickets", label: "Tickets" },
  { key: "/tickets/new", label: "Nuevo ticket" },
  { key: "/profile", label: "Perfil" },
];

function getMenuItems(isAdmin: boolean): AppMenuItem[] {
  if (isAdmin) {
    return [...baseItems, { key: "/admin", label: "Administración" }];
  }

  return baseItems;
}

function getSelectedKey(pathname: string, items: AppMenuItem[]) {
  const matched = items
    .map((item) => item.key?.toString() ?? "")
    .sort((a, b) => b.length - a.length)
    .find((key) => key && pathname.startsWith(key));

  return matched ? [matched] : ["/dashboard"];
}

export function AppShell() {
  const location = useLocation();
  const navigate = useNavigate();
  const { hasRole } = useAuth();

  const menuItems = getMenuItems(hasRole("AGENT"));

  return (
    <Layout style={{ minHeight: "100vh" }}>
      <Sider style={{ borderRight: "1px solid #f0f0f0", width: 240 }}>
        <div style={{ padding: "16px 20px" }}>
          <Typography.Title level={4} style={{ margin: 0 }}>
            TFG Ticketing
          </Typography.Title>
          <Typography.Text type="secondary">Frontend MVP</Typography.Text>
        </div>
        <Menu
          mode="inline"
          selectedKeys={getSelectedKey(location.pathname, menuItems)}
          items={menuItems}
          onClick={({ key }) => navigate(key)}
          style={{ borderInlineEnd: "none" }}
        />
      </Sider>

      <Layout style={{ display: "flex", flexDirection: "column", width: "100%" }}>
        <Header
          style={{
            background: "#fff",
            borderBottom: "1px solid #f0f0f0",
            display: "flex",
            alignItems: "center",
            paddingInline: 24,
            height: 64,
          }}
        >
          <Typography.Title level={5} style={{ margin: 0 }}>
            Ticketing Platform
          </Typography.Title>
        </Header>
        <Content style={{ margin: 24 }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
}
