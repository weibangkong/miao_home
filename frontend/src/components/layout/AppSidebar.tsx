import { useLocation, Link } from "react-router-dom";
import { Grid } from "antd";
import {
  AppstoreOutlined,
  BugOutlined,
  UserOutlined,
  BellOutlined,
  SettingOutlined,
} from "@ant-design/icons";

const { useBreakpoint } = Grid;

interface NavItem {
  key: string;
  path: string;
  icon: React.ReactNode;
  label: string;
}

const navItems: NavItem[] = [
  { key: "/", path: "/", icon: <AppstoreOutlined />, label: "数据概览" },
  { key: "/cats", path: "/cats", icon: <BugOutlined />, label: "猫咪管理" },
  { key: "/adopters", path: "/adopters", icon: <UserOutlined />, label: "认养管理" },
  { key: "/notifications", path: "/notifications", icon: <BellOutlined />, label: "通知管理" },
];

export default function AppSidebar() {
  const location = useLocation();
  const screens = useBreakpoint();
  const isMobile = !screens.md;
  const currentKey = "/" + (location.pathname.split("/")[1] || "");

  if (isMobile) return null;

  return (
    <aside
      style={{
        width: 64,
        height: "100vh",
        position: "fixed",
        left: 0,
        top: 0,
        background: "#FFFFFF",
        borderRight: "1px solid #F3F4F6",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        padding: "24px 0",
        zIndex: 50,
      }}
    >
      {/* Logo */}
      <Link
        to="/"
        style={{
          width: 36,
          height: 36,
          borderRadius: 10,
          background: "#1C85E8",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          color: "#FFFFFF",
          fontSize: 16,
          fontWeight: 700,
          marginBottom: 32,
          textDecoration: "none",
        }}
      >
        M
      </Link>

      {/* Nav items */}
      {navItems.map((item) => {
        const isActive = currentKey === item.key;
        return (
          <Link
            key={item.key}
            to={item.path}
            title={item.label}
            style={{
              width: 40,
              height: 40,
              borderRadius: 10,
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              color: isActive ? "#1C85E8" : "#9CA3AF",
              background: isActive ? "rgba(28,133,232,0.1)" : "transparent",
              marginBottom: 8,
              fontSize: 20,
              transition: "all 0.2s",
              textDecoration: "none",
            }}
          >
            {item.icon}
          </Link>
        );
      })}

      {/* Spacer */}
      <div style={{ flex: 1 }} />

      {/* Settings at bottom */}
      <div
        style={{
          width: 40,
          height: 40,
          borderRadius: 10,
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          color: "#9CA3AF",
          fontSize: 20,
        }}
      >
        <SettingOutlined />
      </div>
    </aside>
  );
}
