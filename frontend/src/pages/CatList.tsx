import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Row, Col, Input, Spin, Button, Empty } from "antd";
import { PlusOutlined, SearchOutlined } from "@ant-design/icons";
import { getCats } from "../api";
import { GenderBadge, BubbleTag } from "../components";
import { PageHeader } from "../components/ui";
import type { Cat } from "../types";
import { getMediaUrl } from "../utils/oss";

export default function CatList() {
  const [cats, setCats] = useState<Cat[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchText, setSearchText] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    setLoading(true);
    getCats()
      .then((res) => {
        if (res.code === 200) setCats(res.data);
      })
      .finally(() => setLoading(false));
  }, []);

  const filteredCats = cats.filter((c) =>
    c.name?.toLowerCase().includes(searchText.toLowerCase())
  );

  const getAvatarUrl = (url: string) => getMediaUrl(url);

  return (
    <div>
      <PageHeader
        title="猫咪管理"
        subtitle={`共 ${filteredCats.length} 只猫咪`}
        extra={
          <>
            <Input
              placeholder="搜索猫咪名称"
              prefix={<SearchOutlined />}
              value={searchText}
              onChange={(e) => setSearchText(e.target.value)}
              style={{ width: 200, borderRadius: 8 }}
              allowClear
            />
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={() => navigate("/cats/new")}
              style={{ borderRadius: 8 }}
            >
              添加猫咪
            </Button>
          </>
        }
      />

      {loading ? (
        <Spin size="large" style={{ display: "block", margin: "60px auto" }} />
      ) : filteredCats.length === 0 ? (
        <Empty description="暂未记录猫咪信息" />
      ) : (
        <Row gutter={[24, 24]}>
          {filteredCats.map((cat) => (
            <Col xs={24} sm={12} md={8} lg={6} key={cat.id}>
              <div
                className="cat-grid-item"
                onClick={() => navigate(`/cats/${cat.id}`)}
              >
                <div className="cat-grid-cover">
                  {cat.avatarUrl ? (
                    <img
                      alt={cat.name}
                      src={getAvatarUrl(cat.avatarUrl)}
                      onError={(e) => {
                        (e.target as HTMLImageElement).src =
                          "data:image/svg+xml,%3Csvg xmlns=%27http://www.w3.org/2000/svg%27 viewBox=%270 0 100 100%27%3E%3Ctext y=%27.9em%27 font-size=%2790%27%3E🐱%3C/text%3E%3C/svg%3E";
                      }}
                    />
                  ) : (
                    <div
                      style={{
                        height: "100%",
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                        fontSize: 64,
                        background: "#F5F5F5",
                      }}
                    >
                      🐱
                    </div>
                  )}
                  <div style={{ position: "absolute", top: 10, left: 10, display: "flex", gap: 6, flexWrap: "wrap" }}>
                    {cat.isAdopted && <BubbleTag variant="success">已认养</BubbleTag>}
                    {cat.isNeutered && <BubbleTag variant="info">已绝育</BubbleTag>}
                  </div>
                </div>
                <div className="cat-grid-info">
                  <div className="cat-grid-name">{cat.name || "无名猫"}</div>
                  <div className="cat-grid-detail">
                    {cat.gender && <GenderBadge gender={cat.gender} />}
                    {cat.locationList && cat.locationList.length > 0 && (
                      <span>· {cat.locationList.map((l) => l.tenantName ? `${l.tenantName}-${l.building}` : l.building).join(" ")}</span>
                    )}
                    {cat.color && <span>· {cat.color}</span>}
                  </div>
                </div>
              </div>
            </Col>
          ))}
        </Row>
      )}
    </div>
  );
}
