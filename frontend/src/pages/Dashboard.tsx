import { useEffect, useState } from "react";
import { Table, Spin } from "antd";
import { getCats, getAdopters, getTenants } from "../api";
import { GenderBadge, BubbleTag } from "../components";
import { StatCard, StatGroup, Card, PageHeader } from "../components/ui";
import type { Cat, Adopter, Tenant } from "../types";
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer,
} from "recharts";

export default function Dashboard() {
  const [cats, setCats] = useState<Cat[]>([]);
  const [adopters, setAdopters] = useState<Adopter[]>([]);
  const [tenants, setTenants] = useState<Tenant[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([getCats(), getAdopters(), getTenants()]).then(([catRes, adopterRes, tenantRes]) => {
      if (catRes.code === 200) setCats(catRes.data);
      if (adopterRes.code === 200) setAdopters(adopterRes.data);
      if (tenantRes.code === 200) setTenants(tenantRes.data);
      setLoading(false);
    });
  }, []);

  if (loading) return <Spin size="large" style={{ display: "block", margin: "100px auto" }} />;

  const adoptedCats = cats.filter((c) => c.isAdopted);
  const pendingCats = cats.length - adoptedCats.length;
  const activeAdopters = adopters.filter((a) => a.isActive);
  const tenantCount = [...new Set(cats.map((c) => c.tenantId))].length;

  /** 按小区聚合的图表数据 */
  const chartData = tenants.map((t) => ({
    name: t.name,
    strayCats: cats.filter((c) => c.tenantId === t.id).length,
    activeUsers: adopters.filter((a) => a.tenantId === t.id).length,
  }));

  const columns = [
    {
      title: "猫咪名称",
      dataIndex: "name",
      key: "name",
      render: (v: string) => <span style={{ fontWeight: 500 }}>{v || "无名猫"}</span>,
    },
    {
      title: "性别",
      dataIndex: "gender",
      key: "gender",
      width: 80,
      render: (v: string) => <GenderBadge gender={v} />,
    },
    {
      title: "毛色",
      dataIndex: "color",
      key: "color",
      render: (v: string) => v || "-",
    },
    {
      title: "状态",
      dataIndex: "isAdopted",
      key: "isAdopted",
      width: 100,
      render: (v: boolean) =>
        v ? (
          <BubbleTag variant="success">已认养</BubbleTag>
        ) : (
          <BubbleTag variant="warning">待认养</BubbleTag>
        ),
    },
  ];

  return (
    <div>
      <PageHeader
        title="数据概览"
        subtitle="小区猫咪统计与认养概况"
      />

      {/* Stat Cards */}
      <StatGroup>
        <StatCard label="猫咪总数" value={cats.length} />
        <StatCard label="已认养" value={adoptedCats.length} />
        <StatCard label="待认养" value={pendingCats} />
        <StatCard label="涉及小区" value={tenantCount} />
      </StatGroup>

      {/* 各小区数据趋势曲线图 */}
      <div style={{ marginTop: 24 }}>
        <Card>
          <div className="section-title" style={{ marginBottom: 16 }}>各小区数据趋势</div>
          {chartData.length === 0 ? (
            <div className="empty-state">暂无小区数据</div>
          ) : (
            <ResponsiveContainer width="100%" height={300}>
              <LineChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#F3F4F6" />
                <XAxis dataKey="name" stroke="#9CA3AF" fontSize={13} />
                <YAxis stroke="#9CA3AF" fontSize={13} allowDecimals={false} />
                <Tooltip />
                <Legend />
                <Line
                  type="monotone"
                  dataKey="activeUsers"
                  name="活跃用户"
                  stroke="#1C85E8"
                  strokeWidth={2}
                  dot={{ fill: "#1C85E8", r: 4 }}
                  activeDot={{ r: 6 }}
                />
                <Line
                  type="monotone"
                  dataKey="strayCats"
                  name="流浪猫数量"
                  stroke="#FF886F"
                  strokeWidth={2}
                  dot={{ fill: "#FF886F", r: 4 }}
                  activeDot={{ r: 6 }}
                />
              </LineChart>
            </ResponsiveContainer>
          )}
        </Card>
      </div>

      {/* Content Grid */}
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 24, marginTop: 24 }}>
        {/* Left: Recent Cats */}
        <Card>
          <div className="section-title" style={{ marginBottom: 12 }}>最近更新的猫咪</div>
          <div className="modern-table">
            <Table
              dataSource={cats.slice(0, 10)}
              columns={columns}
              rowKey="id"
              pagination={false}
              size="small"
              scroll={{ x: 400 }}
            />
          </div>
        </Card>

        {/* Right Column */}
        <div style={{ display: "flex", flexDirection: "column", gap: 24 }}>
          {/* Building Distribution */}
          <Card>
            <div className="section-title" style={{ marginBottom: 16 }}>小区分布</div>
            {chartData.length === 0 ? (
              <div className="empty-state">暂无小区数据</div>
            ) : (
              chartData.map((t) => {
                const adopted = cats.filter((c) => c.tenantId === tenants.find(tn => tn.name === t.name)?.id && c.isAdopted).length;
                return (
                  <div className="list-item" key={t.name}>
                    <div className="list-item-left">
                      <span style={{ fontWeight: 500, fontSize: 15, minWidth: 36 }}>{t.name}</span>
                    </div>
                    <div style={{ textAlign: "right" }}>
                      <span style={{ fontWeight: 500 }}>{t.strayCats}</span>
                      <span className="text-light"> 只 </span>
                      <span className="text-secondary" style={{ fontSize: 13 }}>
                        · 已认养 {adopted}
                      </span>
                    </div>
                  </div>
                );
              })
            )}
          </Card>

          {/* Recent Adopters */}
          <Card>
            <div className="section-title" style={{ marginBottom: 16 }}>最近认养记录</div>
            {activeAdopters.length === 0 ? (
              <div className="empty-state">暂无认养记录</div>
            ) : (
              adopters.slice(0, 5).map((a) => (
                <div className="list-item" key={a.id}>
                  <div className="list-item-left">
                    <div
                      className="avatar-circle"
                      style={{
                        background: Math.random() > 0.5 ? "#1C85E8" : "#FF886F",
                      }}
                    >
                      {(a.adopterName || "匿")[0]}
                    </div>
                    <div>
                      <div className="list-item-title">{a.adopterName || "匿名认养者"}</div>
                      <div className="list-item-desc">{a.catName || "猫咪"}</div>
                    </div>
                  </div>
                  <div style={{ textAlign: "right" }}>
                    <span className="text-secondary" style={{ fontSize: 13 }}>
                      {a.householdNumber}
                    </span>
                    <div className="list-item-meta">
                      {a.adoptedAt?.split("T")[0] || ""}
                    </div>
                  </div>
                </div>
              ))
            )}
          </Card>
        </div>
      </div>
    </div>
  );
}
