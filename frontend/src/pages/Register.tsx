import { useState } from "react";
import { Form, Input, Button, Card, Typography } from "antd";
import { PhoneOutlined, LockOutlined, UserOutlined } from "@ant-design/icons";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";

const { Title, Text } = Typography;

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (values: {
    phone: string;
    password: string;
    confirm: string;
    nickname: string;
  }) => {
    if (values.password !== values.confirm) {
      setError("两次输入的密码不一致");
      return;
    }
    setLoading(true);
    setError("");
    try {
      await register(values.phone, values.password, values.nickname);
      navigate("/cats", { replace: true });
    } catch (e: any) {
      setError(e.message || "注册失败");
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
          注册喵之家
        </Title>
        <Text
          type="secondary"
          style={{ display: "block", textAlign: "center", marginBottom: 24 }}
        >
          注册后即可参与社区互动
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
            name="nickname"
            rules={[
              { required: true, message: "请输入昵称" },
              { max: 20, message: "昵称最多 20 个字符" },
            ]}
          >
            <Input prefix={<UserOutlined />} placeholder="昵称" />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[
              { required: true, message: "请输入密码" },
              { min: 6, message: "密码至少 6 位" },
            ]}
          >
            <Input.Password prefix={<LockOutlined />} placeholder="密码（至少 6 位）" />
          </Form.Item>

          <Form.Item
            name="confirm"
            rules={[{ required: true, message: "请确认密码" }]}
          >
            <Input.Password prefix={<LockOutlined />} placeholder="确认密码" />
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
              注册
            </Button>
          </Form.Item>
        </Form>

        <div style={{ textAlign: "center" }}>
          <Text type="secondary">
            已有账号？{" "}
            <Link to="/login" style={{ color: "#1C85E8", fontWeight: 600 }}>
              去登录
            </Link>
          </Text>
        </div>
      </Card>
    </div>
  );
}
