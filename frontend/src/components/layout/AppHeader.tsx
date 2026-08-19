import { useNavigate, useLocation } from "react-router-dom";
import { Button, Dropdown, Space, Grid } from "antd";
import {
  BellOutlined,
  UserOutlined,
  LogoutOutlined,
  LoginOutlined,
  UserAddOutlined,
} from "@ant-design/icons";
import { useAuth } from "../../contexts/AuthContext";

const { useBreakpoint } = Grid;

export default function AppHeader() {
  const { user, isAuthenticated, loading: authLoading, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const screens = useBreakpoint();
  const isMobile = !screens.md;

  return (
    <header
      style={{
        display: "flex",
        alignItems: "center",
        justifyContent: "space-between",
        padding: isMobile ? "12px 20px" : "16px 32px",
        background: "#F5F5F5",
      }}
    >
      {/* Left: brand + search placeholder */}
      <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
        <div
          style={{
            width: 32,
            height: 32,
            borderRadius: "50%",
            background: "#1C85E8",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            color: "#FFFFFF",
            fontSize: 13,
            fontWeight: 700,
          }}
        >
          M
        </div>
        <div
          style={{
            background: "#FFFFFF",
            border: "1px solid #E5E7EB",
            borderRadius: 10,
            padding: "8px 16px",
            fontSize: 13,
            color: "#9CA3AF",
            display: "flex",
            alignItems: "center",
            gap: 8,
            width: isMobile ? 160 : 240,
          }}
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#9CA3AF" strokeWidth="2">
            <circle cx="11" cy="11" r="8" /><path d="m21 21-4.35-4.35" />
          </svg>
          <span>搜索猫咪、认养记录...</span>
        </div>
      </div>

      {/* Right: notifications + user */}
      <div style={{ display: "flex", alignItems: "center", gap: 16 }}>
        {/* Notification bell */}
        <div style={{ position: "relative", cursor: "pointer", color: "#FF886F" }}>
          <BellOutlined style={{ fontSize: 18 }} />
          <span
            style={{
              position: "absolute",
              top: -2,
              right: -2,
              width: 8,
              height: 8,
              background: "#FF886F",
              borderRadius: "50%",
              border: "2px solid #F5F5F5",
            }}
          />
        </div>

        {/* User */}
        {!authLoading && (
          isAuthenticated ? (
            <Dropdown
              menu={{
                items: [
                  {
                    key: "logout",
                    icon: <LogoutOutlined />,
                    label: "退出登录",
                    onClick: () => logout(),
                  },
                ],
              }}
              trigger={["click"]}
            >
              <Button
                type="text"
                icon={<UserOutlined />}
                style={{ color: "#1A1A1A", fontWeight: 500 }}
              >
                {!isMobile && user?.nickname}
              </Button>
            </Dropdown>
          ) : (
            <Space size={4}>
              <Button
                type="text"
                icon={<LoginOutlined />}
                onClick={() =>
                  navigate("/login", { state: { from: location.pathname } })
                }
                style={{ color: "#737373" }}
              >
                {!isMobile && "登录"}
              </Button>
              {!isMobile && (
                <Button
                  type="text"
                  icon={<UserAddOutlined />}
                  onClick={() => navigate("/register")}
                  style={{ color: "#737373" }}
                >
                  注册
                </Button>
              )}
            </Space>
          )
        )}
      </div>
    </header>
  );
}
