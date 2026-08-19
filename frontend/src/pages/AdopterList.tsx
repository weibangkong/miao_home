import { useEffect, useState } from "react";
import {
  Table,
  Popconfirm,
  Button,
  Input,
  Space,
  message,
  Spin,
} from "antd";
import { SearchOutlined, ReloadOutlined, PhoneOutlined, HomeOutlined, UserOutlined } from "@ant-design/icons";
import { getAdopters, cancelAdoption, searchAdopters } from "../api";
import { BubbleTag } from "../components";
import { PageHeader } from "../components/ui";
import type { Adopter } from "../types";

export default function AdopterList() {
  const [adopters, setAdopters] = useState<Adopter[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchText, setSearchText] = useState("");

  const loadAdopters = async () => {
    setLoading(true);
    try {
      let res;
      if (searchText) {
        res = await searchAdopters(searchText);
      } else {
        res = await getAdopters();
      }
      if (res.code === 200) setAdopters(res.data);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAdopters();
  }, []);

  const handleCancel = async (id: number) => {
    await cancelAdoption(id);
    message.success("已取消认养");
    loadAdopters();
  };

  const columns = [
    {
      title: "认养人",
      dataIndex: "adopterName",
      key: "adopterName",
      render: (v: string) => (
        <span>
          <UserOutlined style={{ marginRight: 6, color: "#9CA3AF" }} />
          {v || "匿名"}
        </span>
      ),
    },
    {
      title: "认养猫咪",
      dataIndex: "catName",
      key: "catName",
      render: (v: string) => <span>{v || "-"}</span>,
    },
    {
      title: "认养户号",
      dataIndex: "householdNumber",
      key: "householdNumber",
    },
    {
      title: "楼栋",
      key: "building",
      render: (_: unknown, record: Adopter) =>
        record.building ? (
          <span>
            <HomeOutlined style={{ marginRight: 4, color: "#9CA3AF" }} />
            {record.building} 栋 {record.unitNumber || ""}
          </span>
        ) : "-",
    },
    {
      title: "电话",
      dataIndex: "phone",
      key: "phone",
      render: (v: string) =>
        v ? (
          <span>
            <PhoneOutlined style={{ marginRight: 4, color: "#9CA3AF" }} />
            {v}
          </span>
        ) : "-",
    },
    {
      title: "状态",
      dataIndex: "isActive",
      key: "isActive",
      render: (v: boolean) =>
        v ? (
          <BubbleTag variant="success">认养中</BubbleTag>
        ) : (
          <BubbleTag variant="neutral">已取消</BubbleTag>
        ),
    },
    {
      title: "认养时间",
      dataIndex: "adoptedAt",
      key: "adoptedAt",
      render: (v: string) => v?.split("T")[0] || "-",
    },
    {
      title: "操作",
      key: "action",
      render: (_: unknown, record: Adopter) =>
        record.isActive ? (
          <Popconfirm
            title="确定取消此认养记录？"
            onConfirm={() => handleCancel(record.id)}
          >
            <Button size="small" danger className="btn-danger">
              取消认养
            </Button>
          </Popconfirm>
        ) : null,
    },
  ];

  return (
    <div>
      <PageHeader
        title="认养管理"
        subtitle={`共 ${adopters.length} 条认养记录`}
        extra={
          <Space>
            <Input
              placeholder="搜索户号"
              prefix={<SearchOutlined />}
              value={searchText}
              onChange={(e) => setSearchText(e.target.value)}
              onPressEnter={loadAdopters}
              style={{ width: 200, borderRadius: 8 }}
              allowClear
            />
            <Button
              icon={<ReloadOutlined />}
              onClick={loadAdopters}
              style={{ borderRadius: 8 }}
              className="btn-secondary"
            >
              刷新
            </Button>
          </Space>
        }
      />

      {loading ? (
        <Spin style={{ display: "block", margin: "40px auto" }} />
      ) : (
        <div className="modern-table">
          <Table
            dataSource={adopters}
            columns={columns}
            rowKey="id"
            pagination={{ pageSize: 10 }}
            scroll={{ x: 800 }}
            size="small"
          />
        </div>
      )}
    </div>
  );
}
