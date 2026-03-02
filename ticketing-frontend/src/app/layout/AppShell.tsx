import { MdDashboard, MdOutlineAdd } from "react-icons/md";
import { Layout, Menu, Typography } from "antd";
import type { MenuProps } from "antd";
import { Outlet, useLocation, useNavigate } from "react-router-dom";
import { HiBars3, HiTicket, HiUsers } from "react-icons/hi2";
import { useAuth } from "../../features/auth/hooks/useAuth";

type AppMenuItem = Required<NonNullable<MenuProps["items"]>>[number];

const { Header, Sider, Content } = Layout;

const baseItems: AppMenuItem[] = [
  { key: "/dashboard", icon: <MdDashboard fontSize={18}/>, label: "Dashboard" },
  { key: "/tickets", icon: <HiTicket   fontSize={18}/>, label: "Tickets" },
  { key: "/tickets/new", icon: <MdOutlineAdd fontSize={18}/>, label: "Nuevo ticket" },
  { key: "/profile", icon: <HiBars3 fontSize={18}/>, label: "Perfil" }
];

function getMenuItems(isAdmin: boolean): AppMenuItem[] {
  if (isAdmin) {
    return [...baseItems, { key: "/admin", icon: <HiUsers fontSize={18}/>, label: "Administración" }];
  }

  return baseItems;
}

function getSelectedKey(pathname: string, items: AppMenuItem[]) {
  const matched = items
    .map((item) => item.key?.toString() ?? "")
    .sort((a, b) => b.length - a.length) // Sort by length descending
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

      <Layout style={{ display: 'flex', flexDirection: 'column', width: '100%' }}>
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
