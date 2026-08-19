import { useState } from "react";
import { Form, Input, Button, Card, Typography } from "antd";
import { PhoneOutlined, LockOutlined } from "@ant-design/icons";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";

const { Title, Text } = Typography;

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const from = (location.state as { from?: string })?.from || "/cats";

  const handleSubmit = async (values: { phone: string; password: string }) => {
    setLoading(true);
    setError("");
    try {
      await login(values.phone, values.password);
      navigate(from, { replace: true });
    } catch (e: any) {
      setError(e.message || "登录失败");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      style={{
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        minHeight: "calc(100vh - 120px)",
      }}
    >
      <Card
        style={{
          width: 380,
          borderRadius: 16,
          boxShadow: "0 4px 24px rgba(0,0,0,0.06)",
        }}
      >
        <Title level={3} style={{ textAlign: "center", marginBottom: 8 }}>
          登录喵之家
        </Title>
        <Text
          type="secondary"
          style={{ display: "block", textAlign: "center", marginBottom: 24 }}
        >
          登录后可点赞、评论互动
        </Text>

        {error && (
          <div
            style={{
              color: "#FF886F",
              background: "#FFF2EF",
              padding: "8px 12px",
              borderRadius: 8,
              marginBottom: 16,
              fontSize: 13,
            }}
          >
            {error}
          </div>
        )}

        <Form layout="vertical" onFinish={handleSubmit} size="large">
          <Form.Item
            name="phone"
            rules={[
              { required: true, message: "请输入手机号" },
              { pattern: /^1[3-9]\d{9}$/, message: "手机号格式不正确" },
            ]}
          >
            <Input prefix={<PhoneOutlined />} placeholder="手机号" />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[
              { required: true, message: "请输入密码" },
              { min: 6, message: "密码至少 6 位" },
            ]}
          >
            <Input.Password prefix={<LockOutlined />} placeholder="密码" />
          </Form.Item>

          <Form.Item style={{ marginBottom: 12 }}>
            <Button
              type="primary"
              htmlType="submit"
              loading={loading}
              block
              className="btn-primary"
              style={{ borderRadius: 8, height: 44 }}
            >
              登录
            </Button>
          </Form.Item>
        </Form>

        <div style={{ textAlign: "center" }}>
          <Text type="secondary">
            还没有账号？{" "}
            <Link to="/register" style={{ color: "#1C85E8", fontWeight: 600 }}>
              立即注册
            </Link>
          </Text>
        </div>
      </Card>
    </div>
  );
}
