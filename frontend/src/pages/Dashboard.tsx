import { useEffect, useState } from "react";
import { Table, Spin } from "antd";
import { ClusterOutlined, HeartOutlined, HomeOutlined } from "@ant-design/icons";
import { getCats, getAdopters, getTenants } from "../api";
import { GenderBadge, BubbleTag } from "../components";
import { StatCard, StatGroup, Card, PageHeader } from "../components/ui";
import type { Cat, Adopter, Tenant } from "../types";
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer,
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

  /** 猫咪所属小区 ID 列表：取「经常出现小区」locationList，为空时回退到所属租户 tenantId */
  const catTenantIds = (c: Cat): number[] => {
    const list = c.locationList || [];
    return list.length > 0 ? list.map((l) => l.tenantId) : [c.tenantId];
  };

  const tenantCount = new Set(cats.flatMap(catTenantIds)).size;

  /** 最近更新的猫咪（按更新时间倒序） */
  const recentCats = [...cats].sort((a, b) => (b.updatedAt || "").localeCompare(a.updatedAt || "")).slice(0, 10);

  /** 最近认养记录（按认养时间倒序） */
  const recentAdopters = [...adopters].sort((a, b) => (b.adoptedAt || "").localeCompare(a.adoptedAt || "")).slice(0, 5);

  /** 按小区聚合的图表数据：猫咪（已认养/待认养堆叠）+ 活跃用户 */
  const chartData = tenants.map((t) => ({
    name: t.name,
    adopted: cats.filter((c) => c.isAdopted && catTenantIds(c).includes(t.id)).length,
    unadopted: cats.filter((c) => !c.isAdopted && catTenantIds(c).includes(t.id)).length,
    activeUsers: adopters.filter((a) => a.tenantId === t.id && a.isActive).length,
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
      title: "活跃位置",
      key: "location",
      render: (_: unknown, record: Cat) => {
        const list = record.locationList;
        if (!list || list.length === 0) return "-";
        return list.map((l) => (l.tenantName ? `${l.tenantName}-${l.building}` : l.building)).join(" ");
      },
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
        <StatCard label="猫咪总数" value={cats.length} icon={<ClusterOutlined />} tone="blue" />
        <StatCard
          label="认养情况"
          value={`已认养 ${adoptedCats.length}`}
          secondary={`待认养 ${pendingCats}`}
          icon={<HeartOutlined />}
          tone="orange"
        />
        <StatCard label="涉及小区" value={tenantCount} icon={<HomeOutlined />} tone="green" />
      </StatGroup>

      {/* 各小区数据分布柱状图 */}
      <div style={{ marginTop: 24 }}>
        <Card>
          <div className="section-title" style={{ marginBottom: 16 }}>各小区数据分布</div>
          {chartData.length === 0 ? (
            <div className="empty-state">暂无小区数据</div>
          ) : (
            <ResponsiveContainer width="100%" height={280}>
              {/* 柱宽为黄金分割（38.2%）的 1/3：柱体约占 12.7%，间距约占 87.3% */}
              <BarChart data={chartData} barCategoryGap="53.8%">
                <CartesianGrid strokeDasharray="3 3" stroke="#F3F4F6" vertical={false} />
                <XAxis
                  dataKey="name"
                  stroke="#9CA3AF"
                  fontSize={13}
                  interval={0}
                  angle={-30}
                  textAnchor="end"
                  height={60}
                />
                <YAxis
                  stroke="#9CA3AF"
                  fontSize={13}
                  allowDecimals={false}
                  domain={[0, (dataMax: number) => Math.max(10, dataMax)]}
                />
                <Tooltip />
                <Legend />
                <Bar dataKey="adopted" name="已认养" stackId="cats" fill="#1C85E8" />
                <Bar dataKey="unadopted" name="待认养" stackId="cats" fill="#FF886F" />
                <Bar dataKey="activeUsers" name="活跃用户" fill="#1BB981" />
              </BarChart>
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
              dataSource={recentCats}
              columns={columns}
              rowKey="id"
              pagination={false}
              size="small"
              scroll={{ x: 600 }}
            />
          </div>
        </Card>

        {/* Right Column */}
        <div style={{ display: "flex", flexDirection: "column", gap: 24 }}>
          {/* Recent Adopters */}
          <Card>
            <div className="section-title" style={{ marginBottom: 16 }}>最近认养记录</div>
            {recentAdopters.length === 0 ? (
              <div className="empty-state">暂无认养记录</div>
            ) : (
              recentAdopters.map((a) => (
                <div className="list-item" key={a.id}>
                  <div className="list-item-left">
                    <div
                      className="avatar-circle"
                      style={{
                        background: a.isActive ? "#1C85E8" : "#FF886F",
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

          {/* Building Distribution */}
          <Card>
            <div className="section-title" style={{ marginBottom: 16 }}>小区分布</div>
            {chartData.length === 0 ? (
              <div className="empty-state">暂无小区数据</div>
            ) : (
              chartData.map((t) => (
                <div className="list-item" key={t.name}>
                  <div className="list-item-left">
                    <span style={{ fontWeight: 500, fontSize: 15, minWidth: 36 }}>{t.name}</span>
                  </div>
                  <div style={{ textAlign: "right" }}>
                    <span style={{ fontWeight: 500 }}>{t.adopted + t.unadopted}</span>
                    <span className="text-light"> 只 </span>
                    <span className="text-secondary" style={{ fontSize: 13 }}>
                      · 已认养 {t.adopted}
                    </span>
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
