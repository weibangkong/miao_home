import { useEffect, useState } from "react";
import {
  Button,
  message,
  Spin,
  Space,
  Empty,
  Modal,
  Form,
  Input,
} from "antd";
import { BellOutlined, CheckOutlined, MailOutlined } from "@ant-design/icons";
import {
  getNotifications,
  markNotificationRead,
  markAllNotificationsRead,
  sendNotification,
} from "../api";
import { BubbleTag, BubbleCount } from "../components";
import { PageHeader } from "../components/ui";
import type { Notification } from "../types";

export default function NotificationList() {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [loading, setLoading] = useState(true);
  const [sendModalOpen, setSendModalOpen] = useState(false);
  const [sendForm] = Form.useForm();

  const loadNotifications = async () => {
    setLoading(true);
    try {
      const res = await getNotifications();
      if (res.code === 200) setNotifications(res.data);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadNotifications();
  }, []);

  const handleMarkRead = async (id: number) => {
    await markNotificationRead(id);
    message.success("已标记已读");
    loadNotifications();
  };

  const handleMarkAllRead = async () => {
    const unread = notifications.filter((n) => !n.isRead);
    if (unread.length === 0) {
      message.info("没有未读通知");
      return;
    }
    const adopterId = unread[0].adopterId;
    if (adopterId) {
      await markAllNotificationsRead(adopterId);
    } else {
      for (const n of unread) {
        await markNotificationRead(n.id);
      }
    }
    message.success("全部标记已读");
    loadNotifications();
  };

  const handleSend = async (values: any) => {
    await sendNotification(values);
    message.success("发送成功");
    setSendModalOpen(false);
    sendForm.resetFields();
    loadNotifications();
  };

  const unreadCount = notifications.filter((n) => !n.isRead).length;

  const formatTime = (t: string) => {
    const parts = t?.split("T");
    if (!parts || parts.length < 2) return t || "";
    return parts[0] + " " + (parts[1]?.split(".")[0] || "");
  };

  return (
    <div>
      <PageHeader
        title={
          <span style={{ display: "flex", alignItems: "center", gap: 10 }}>
            通知管理
            {unreadCount > 0 && <BubbleCount count={unreadCount} />}
          </span>
        }
        subtitle={`共 ${notifications.length} 条通知`}
        extra={
          <Space>
            <Button
              onClick={handleMarkAllRead}
              style={{ borderRadius: 8 }}
              className="btn-secondary"
            >
              <CheckOutlined /> 全部标记已读
            </Button>
            <Button
              type="primary"
              icon={<BellOutlined />}
              onClick={() => setSendModalOpen(true)}
              style={{ borderRadius: 8 }}
            >
              发送通知
            </Button>
          </Space>
        }
      />

      {loading ? (
        <Spin style={{ display: "block", margin: "40px auto" }} />
      ) : notifications.length === 0 ? (
        <Empty description="暂无通知" />
      ) : (
        notifications.map((item) => (
          <div key={item.id} className="notif-item">
            <div className={`notif-icon ${item.isRead ? "read" : "unread"}`}>
              <MailOutlined />
            </div>
            <div className="notif-body">
              <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                <span className={`notif-title ${item.isRead ? "read" : ""}`}>
                  {item.title}
                </span>
                {!item.isRead && <BubbleTag variant="danger">未读</BubbleTag>}
              </div>
              <div className="notif-content">{item.content}</div>
              <div className="notif-time">{formatTime(item.createdAt)}</div>
            </div>
            {!item.isRead && (
              <Button
                size="small"
                onClick={() => handleMarkRead(item.id)}
                className="btn-secondary"
              >
                标记已读
              </Button>
            )}
          </div>
        ))
      )}

      <Modal
        title="发送通知"
        open={sendModalOpen}
        onCancel={() => setSendModalOpen(false)}
        onOk={() => sendForm.submit()}
      >
        <Form form={sendForm} layout="vertical" onFinish={handleSend}>
          <Form.Item
            name="adopterId"
            label="目标认养人ID"
            rules={[{ required: true, message: "请输入认养人ID" }]}
          >
            <Input type="number" placeholder="输入认养人ID" />
          </Form.Item>
          <Form.Item
            name="title"
            label="通知标题"
            rules={[{ required: true, message: "请输入标题" }]}
          >
            <Input placeholder="如：猫咪动态更新" />
          </Form.Item>
          <Form.Item
            name="content"
            label="通知内容"
            rules={[{ required: true, message: "请输入内容" }]}
          >
            <Input.TextArea rows={4} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
