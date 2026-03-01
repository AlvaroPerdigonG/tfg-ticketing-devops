import { DashboardOutlined, FileAddOutlined, ProfileOutlined, TagsOutlined, TeamOutlined } from "@ant-design/icons";
import { Layout, Menu, Typography } from "antd";
import type { MenuProps } from "antd";
import { Outlet, useLocation, useNavigate } from "react-router-dom";

const { Header, Sider, Content } = Layout;

const menuItems: MenuProps["items"] = [
  { key: "/dashboard", icon: <DashboardOutlined />, label: "Dashboard" },
  { key: "/tickets", icon: <TagsOutlined />, label: "Tickets" },
  { key: "/tickets/new", icon: <FileAddOutlined />, label: "Nuevo ticket" },
  { key: "/profile", icon: <ProfileOutlined />, label: "Perfil" },
  { key: "/admin", icon: <TeamOutlined />, label: "Administración" },
];

function getSelectedKey(pathname: string) {
  const matched = menuItems
    ?.map((item) => item?.key?.toString() ?? "")
    .find((key) => key && pathname.startsWith(key));

  return matched ? [matched] : ["/dashboard"];
}

export function AppShell() {
  const location = useLocation();
  const navigate = useNavigate();

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
          selectedKeys={getSelectedKey(location.pathname)}
          items={menuItems}
          onClick={({ key }) => navigate(key)}
          style={{ borderInlineEnd: "none" }}
        />
      </Sider>

      <Layout>
        <Header
          style={{
            background: "#fff",
            borderBottom: "1px solid #f0f0f0",
            display: "flex",
            alignItems: "center",
            paddingInline: 24,
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
