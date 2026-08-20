import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  Descriptions,
  Button,
  Spin,
  Image,
  Upload,
  Modal,
  Form,
  Input,
  Select,
  message,
  Space,
  Popconfirm,
  Row,
  Col,
  List,
  DatePicker,
} from "antd";
import {
  EditOutlined,
  DeleteOutlined,
  UploadOutlined,
  ArrowLeftOutlined,
  HeartOutlined,
  HeartFilled,
  BellOutlined,
  StarOutlined,
  PlusOutlined,
  MedicineBoxOutlined,
  EnvironmentOutlined,
  CalendarOutlined,
  CommentOutlined,
} from "@ant-design/icons";
import dayjs from "dayjs";
import { getCatDetail, deleteCat, uploadCatMedia, deleteCatMedia, setAvatar, adoptCat, getAdoptersByCat, cancelAdoption, sendNotificationToCatAdopters, addHealthRecord, updateHealthRecord, deleteHealthRecord, toggleCatLike, getCatLikeStatus } from "../api";
import { GenderBadge, BubbleTag } from "../components";
import { PageHeader } from "../components/ui";
import CommentSection from "../components/CommentSection";
import type { Cat, CatMedia, Adopter, CatHealthRecord } from "../types";
import { useAuth } from "../contexts/AuthContext";
import { getMediaUrl } from "../utils/oss";

export default function CatDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuth();
  const [cat, setCat] = useState<Cat | null>(null);
  const [mediaList, setMediaList] = useState<CatMedia[]>([]);
  const [adopters, setAdopters] = useState<Adopter[]>([]);
  const [healthRecords, setHealthRecords] = useState<CatHealthRecord[]>([]);
  const [loading, setLoading] = useState(true);
  const [catLiked, setCatLiked] = useState(false);
  const [catLikeCount, setCatLikeCount] = useState(0);

  // Modal states
  const [adoptModalOpen, setAdoptModalOpen] = useState(false);
  const [notifyModalOpen, setNotifyModalOpen] = useState(false);
  const [uploadModalOpen, setUploadModalOpen] = useState(false);
  const [healthModalOpen, setHealthModalOpen] = useState(false);
  const [editingHealthRecord, setEditingHealthRecord] = useState<CatHealthRecord | null>(null);

  const [adoptForm] = Form.useForm();
  const [notifyForm] = Form.useForm();
  const [uploadForm] = Form.useForm();
  const [healthForm] = Form.useForm();

  const loadCat = () => {
    if (!id) return;
    setLoading(true);
    getCatDetail(Number(id))
      .then((res) => {
        if (res.code === 200) {
          setCat(res.data);
          setMediaList(res.data.mediaList || []);
          setHealthRecords(res.data.healthRecordList || []);
          setCatLikeCount(res.data.likeCount || 0);
        }
      })
      .finally(() => setLoading(false));

    getAdoptersByCat(Number(id)).then((res) => {
      if (res.code === 200) setAdopters(res.data);
    });

    if (isAuthenticated) {
      getCatLikeStatus(Number(id)).then((res) => {
        if (res.code === 200) setCatLiked(res.data.liked);
      });
    }
  };

  useEffect(() => {
    loadCat();
  }, [id, isAuthenticated]);

  const handleDelete = async () => {
    if (!cat) return;
    await deleteCat(cat.id);
    message.success("已删除猫咪");
    navigate("/cats");
  };

  const handleUpload = async (values: any) => {
    if (!cat) return;
    const file = values.file?.fileList?.[0]?.originFileObj;
    if (!file) { message.error("请选择文件"); return; }
    try {
      await uploadCatMedia(cat.id, file, values.ageStage || undefined, values.isAvatar || false);
    } catch (e) {
      message.error(e instanceof Error ? e.message : "上传失败");
      return;
    }
    message.success("上传成功");
    setUploadModalOpen(false);
    uploadForm.resetFields();
    loadCat();
  };

  const handleSetAvatar = async (mediaId: number) => {
    if (!cat) return;
    await setAvatar(cat.id, mediaId);
    message.success("已设为头像");
    loadCat();
  };

  const handleDeleteMedia = async (mediaId: number) => {
    await deleteCatMedia(mediaId);
    message.success("已删除");
    loadCat();
  };

  const handleAdopt = async (values: any) => {
    if (!cat) return;
    await adoptCat(cat.id, values);
    message.success("认养成功");
    setAdoptModalOpen(false);
    adoptForm.resetFields();
    loadCat();
  };

  const handleCancelAdoption = async (adopterId: number) => {
    await cancelAdoption(adopterId);
    message.success("已取消认养");
    loadCat();
  };

  const handleNotify = async (values: any) => {
    if (!cat) return;
    await sendNotificationToCatAdopters(cat.id, values.title, values.content);
    message.success("推送通知成功");
    setNotifyModalOpen(false);
    notifyForm.resetFields();
  };

  const handleCatLike = async () => {
    if (!cat || !isAuthenticated) {
      message.info("请先登录");
      return;
    }
    const res = await toggleCatLike(cat.id);
    if (res.code === 200) {
      setCatLiked(res.data.liked);
      setCatLikeCount(res.data.likeCount);
    }
  };

  // ---- Health Records ----
  const openAddHealthRecord = () => {
    setEditingHealthRecord(null);
    healthForm.resetFields();
    setHealthModalOpen(true);
  };

  const openEditHealthRecord = (record: CatHealthRecord) => {
    setEditingHealthRecord(record);
    healthForm.setFieldsValue({
      ...record,
      recordDate: record.recordDate ? dayjs(record.recordDate) : null,
    });
    setHealthModalOpen(true);
  };

  const handleHealthSubmit = async (values: any) => {
    if (!cat) return;
    const data = {
      ...values,
      recordDate: values.recordDate ? values.recordDate.format("YYYY-MM-DD") : undefined,
    };
    if (editingHealthRecord) {
      await updateHealthRecord(cat.id, editingHealthRecord.id, data);
      message.success("健康记录已更新");
    } else {
      await addHealthRecord(cat.id, data);
      message.success("健康记录已添加");
    }
    setHealthModalOpen(false);
    healthForm.resetFields();
    loadCat();
  };

  const handleDeleteHealthRecord = async (recordId: number) => {
    if (!cat) return;
    await deleteHealthRecord(cat.id, recordId);
    message.success("健康记录已删除");
    loadCat();
  };

  if (loading) return <Spin size="large" style={{ display: "block", margin: "100px auto" }} />;
  if (!cat) return <div className="empty-state">猫咪不存在</div>;

  const getUrl = (url: string) => getMediaUrl(url);
  const sickCount = healthRecords.filter(r => r.isSick).length;
  const healthyCount = healthRecords.length - sickCount;

  return (
    <div>
      <PageHeader
        title={cat.name || "无名猫"}
        subtitle={`${cat.color || ""}${cat.birthYear ? " · " + cat.birthYear + " 年" : ""}`}
        extra={
          <Button
            type="text"
            icon={<ArrowLeftOutlined />}
            onClick={() => navigate("/cats")}
            style={{ color: "#737373" }}
          >
            返回列表
          </Button>
        }
      />

      <Row gutter={[32, 32]}>
        {/* 左侧：头像和操作 */}
        <Col xs={24} md={8}>
          <img
            alt={cat.name}
            src={cat.avatarUrl ? getUrl(cat.avatarUrl) : ""}
            className="detail-avatar"
            onError={(e) => {
              (e.target as HTMLImageElement).src =
                "data:image/svg+xml,%3Csvg xmlns=%27http://www.w3.org/2000/svg%27 viewBox=%270 0 100 100%27%3E%3Ctext y=%27.9em%27 font-size=%2790%27%3E🐱%3C/text%3E%3C/svg%3E";
            }}
          />
          <div style={{ marginTop: 20 }}>
            {/* 状态行 */}
            <div style={{ display: "flex", gap: 16, flexWrap: "wrap", marginBottom: 20 }}>
              {cat.isAdopted ? (
                <BubbleTag variant="success">已认养</BubbleTag>
              ) : (
                <BubbleTag variant="warning">待认养</BubbleTag>
              )}
              {cat.isNeutered ? (
                <BubbleTag variant="info">已绝育</BubbleTag>
              ) : (
                <BubbleTag variant="neutral">未绝育</BubbleTag>
              )}
              {sickCount > 0 && (
                <BubbleTag variant="danger">{sickCount} 条异常</BubbleTag>
              )}
            </div>
            <Space wrap style={{ gap: 8 }}>
              <Button
                icon={catLiked ? <HeartFilled style={{ color: "#FF886F" }} /> : <HeartOutlined />}
                onClick={handleCatLike}
                style={{ borderRadius: 8 }}
                className="btn-secondary"
              >
                {catLikeCount > 0 ? catLikeCount : "点赞"}
              </Button>
              <Button
                icon={<UploadOutlined />}
                onClick={() => setUploadModalOpen(true)}
                style={{ borderRadius: 8 }}
                className="btn-secondary"
              >
                上传照片/视频
              </Button>
              <Button
                type="primary"
                icon={<HeartOutlined />}
                onClick={() => setAdoptModalOpen(true)}
                disabled={cat.isAdopted}
                className="btn-primary"
                style={{ borderRadius: 8 }}
              >
                认养
              </Button>
              <Button
                icon={<BellOutlined />}
                onClick={() => setNotifyModalOpen(true)}
                style={{ borderRadius: 8 }}
                className="btn-secondary"
              >
                推送通知
              </Button>
            </Space>
          </div>
        </Col>

        {/* 右侧：详情 */}
        <Col xs={24} md={16}>
          {/* 基本信息 */}
          <div className="section">
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 16 }}>
              <div className="section-title" style={{ marginBottom: 0 }}>
                {cat.name || "无名猫"} 的基本信息
              </div>
              <Space>
                <Button
                  icon={<EditOutlined />}
                  onClick={() => navigate(`/cats/${cat.id}/edit`)}
                  style={{ borderRadius: 8 }}
                  className="btn-secondary"
                >
                  编辑
                </Button>
                <Popconfirm title="确定删除此猫咪？" onConfirm={handleDelete}>
                  <Button
                    danger
                    icon={<DeleteOutlined />}
                    style={{ borderRadius: 8 }}
                    className="btn-danger"
                  >
                    删除
                  </Button>
                </Popconfirm>
              </Space>
            </div>
            <Descriptions column={{ xs: 1, sm: 2 }} bordered size="small">
              <Descriptions.Item label="性别">
                {cat.gender ? <GenderBadge gender={cat.gender} /> : "未知"}
              </Descriptions.Item>
              <Descriptions.Item label="毛色">{cat.color || "未知"}</Descriptions.Item>
              <Descriptions.Item label="出生年份">
                {cat.birthYear ? (
                  <span><CalendarOutlined style={{ marginRight: 4 }} />{cat.birthYear}</span>
                ) : "未知"}
              </Descriptions.Item>
              <Descriptions.Item label="认养状态">
                {cat.isAdopted ? (
                  <BubbleTag variant="success">已认养</BubbleTag>
                ) : (
                  <BubbleTag variant="warning">待认养</BubbleTag>
                )}
              </Descriptions.Item>
              <Descriptions.Item label="绝育状态">
                {cat.isNeutered ? (
                  <BubbleTag variant="info">已绝育</BubbleTag>
                ) : (
                  <BubbleTag variant="neutral">未绝育</BubbleTag>
                )}
              </Descriptions.Item>
              <Descriptions.Item label="点赞数">
                <span style={{ display: "flex", gap: 6, alignItems: "center" }}>
                  <HeartFilled style={{ color: "#FF886F" }} />
                  {cat.likeCount || 0}
                </span>
              </Descriptions.Item>
              <Descriptions.Item label="健康概况">
                {healthRecords.length === 0 ? (
                  <span className="text-light">无记录</span>
                ) : (
                  <span style={{ display: "flex", gap: 12, alignItems: "center" }}>
                    <span className="health-indicator healthy">
                      <span className="dot" />
                      健康 {healthyCount}
                    </span>
                    <span className="health-indicator sick">
                      <span className="dot" />
                      异常 {sickCount}
                    </span>
                  </span>
                )}
              </Descriptions.Item>
              <Descriptions.Item label="描述" span={2}>
                {cat.description || "暂无描述"}
              </Descriptions.Item>
              <Descriptions.Item label="出没地点" span={2}>
                {cat.locationList && cat.locationList.length > 0 ? (
                  cat.locationList.map((l, i) => {
                    const label = l.tenantName ? `${l.tenantName}-${l.building}` : l.building;
                    return (
                      <span key={i}>
                        {i > 0 && "；"}
                        <EnvironmentOutlined style={{ marginRight: 2 }} />
                        {label}
                      </span>
                    );
                  })
                ) : "未知"}
              </Descriptions.Item>
            </Descriptions>
          </div>

          <hr className="section-divider" />

          {/* 健康记录 */}
          <div className="section">
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 16 }}>
              <div className="section-title" style={{ marginBottom: 0 }}>
                <MedicineBoxOutlined style={{ marginRight: 8 }} />健康记录
              </div>
              <Button
                type="primary"
                size="small"
                icon={<PlusOutlined />}
                onClick={openAddHealthRecord}
                className="btn-primary"
                style={{ borderRadius: 8 }}
              >
                添加记录
              </Button>
            </div>
            {healthRecords.length === 0 ? (
              <div className="empty-state">暂无健康记录</div>
            ) : (
              <List
                dataSource={healthRecords}
                renderItem={(record) => (
                  <List.Item
                    style={{ borderRadius: 0, padding: "16px 0", borderBottom: "1px solid #F0F0F0" }}
                    actions={[
                      <Button
                        size="small"
                        icon={<EditOutlined />}
                        onClick={() => openEditHealthRecord(record)}
                        className="btn-secondary"
                      >
                        编辑
                      </Button>,
                      <Popconfirm title="确定删除？" onConfirm={() => handleDeleteHealthRecord(record.id)}>
                        <Button size="small" danger icon={<DeleteOutlined />} className="btn-danger">
                          删除
                        </Button>
                      </Popconfirm>,
                    ]}
                  >
                    <List.Item.Meta
                      avatar={
                        record.isSick ? (
                          <span className="health-indicator sick">
                            <span className="dot" />
                            异常
                          </span>
                        ) : (
                          <span className="health-indicator healthy">
                            <span className="dot" />
                            健康
                          </span>
                        )
                      }
                      title={
                        <span style={{ fontWeight: 600, fontSize: 15 }}>
                          {record.diseaseName || (record.isSick ? "身体异常" : "健康检查")}
                        </span>
                      }
                      description={
                        <div>
                          {record.description && <div style={{ marginTop: 4 }}>{record.description}</div>}
                          {record.treatment && (
                            <div style={{ color: "#1C85E8", marginTop: 4, fontWeight: 500, fontSize: 13 }}>
                              治疗: {record.treatment}
                            </div>
                          )}
                          <div style={{ color: "#8C8C8C", fontSize: 12, marginTop: 4 }}>
                            记录日期: {record.recordDate || "-"}
                          </div>
                        </div>
                      }
                    />
                  </List.Item>
                )}
              />
            )}
          </div>

          <hr className="section-divider" />

          {/* 照片/视频 */}
          <div className="section">
            <div className="section-title">照片 / 视频</div>
            {mediaList.length === 0 ? (
              <div className="empty-state">暂无照片或视频</div>
            ) : (
              <div className="detail-media-grid">
                {mediaList.map((media) => (
                  <div key={media.id} className="detail-media-item">
                    {media.mediaType === "VIDEO" ? (
                      <video controls src={getUrl(media.url)} style={{ width: "100%", height: 180, objectFit: "cover" }} />
                    ) : (
                      <Image src={getUrl(media.url)} style={{ width: "100%", height: 180, objectFit: "cover" }} preview={{ mask: "点击预览" }} />
                    )}
                    <div className="detail-media-label">{media.ageStage || "未标注"}</div>
                    {media.isAvatar && (
                      <div style={{ position: "absolute", top: 10, right: 10, background: "#1C85E8", color: "#fff", padding: "3px 10px", borderRadius: 4, fontSize: 11, fontWeight: 500 }}>
                        头像
                      </div>
                    )}
                    <div style={{ position: "absolute", bottom: 8, right: 8, display: "flex", gap: 4 }}>
                      {!media.isAvatar && (
                        <Button
                          size="small"
                          icon={<StarOutlined />}
                          onClick={() => handleSetAvatar(media.id)}
                          title="设为头像"
                          className="btn-secondary"
                        />
                      )}
                      <Popconfirm title="确定删除？" onConfirm={() => handleDeleteMedia(media.id)}>
                        <Button size="small" danger icon={<DeleteOutlined />} title="删除" className="btn-danger" />
                      </Popconfirm>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          <hr className="section-divider" />

          {/* 认养记录 */}
          <div className="section">
            <div className="section-title">认养记录</div>
            {adopters.length === 0 ? (
              <div className="empty-state">暂无认养记录</div>
            ) : (
              <List
                dataSource={adopters}
                renderItem={(adopter) => (
                  <List.Item
                    style={{ borderRadius: 0, padding: "16px 0", borderBottom: "1px solid #F0F0F0" }}
                    actions={[
                      adopter.isActive ? (
                        <Popconfirm title="确定取消认养？" onConfirm={() => handleCancelAdoption(adopter.id)}>
                          <Button size="small" danger className="btn-danger">取消认养</Button>
                        </Popconfirm>
                      ) : (
                        <BubbleTag variant="neutral">已取消</BubbleTag>
                      ),
                    ]}
                  >
                    <List.Item.Meta
                      title={
                        <span style={{ display: "flex", alignItems: "center", gap: 8 }}>
                          {adopter.adopterName || "匿名认养者"}
                          {adopter.isActive && (
                            <BubbleTag variant="success">认养中</BubbleTag>
                          )}
                        </span>
                      }
                      description={
                        <div style={{ display: "flex", flexWrap: "wrap", gap: "6px 16px", color: "#737373", fontSize: 13 }}>
                          <span>户号: {adopter.householdNumber}</span>
                          {adopter.building && <span>{adopter.building} 栋 {adopter.unitNumber || ""}</span>}
                          {adopter.phone && <span>{adopter.phone}</span>}
                          <span style={{ color: "#8C8C8C" }}>
                            {adopter.adoptedAt?.split("T")[0] || ""}
                          </span>
                        </div>
                      }
                    />
                  </List.Item>
                )}
              />
            )}
          </div>

          <hr className="section-divider" />

          {/* 评论 */}
          <div className="section">
            <div className="section-title"><CommentOutlined style={{ marginRight: 8 }} />评论</div>
            <CommentSection catId={cat.id} />
          </div>
        </Col>
      </Row>

      {/* Upload Modal */}
      <Modal title="上传照片/视频" open={uploadModalOpen} onCancel={() => setUploadModalOpen(false)} onOk={() => uploadForm.submit()}>
        <Form form={uploadForm} layout="vertical" onFinish={handleUpload}>
          <Form.Item name="file" label="选择文件" rules={[{ required: true, message: "请选择文件" }]}>
            <Upload.Dragger beforeUpload={() => false} maxCount={1} accept="image/*,video/*">
              <p className="ant-upload-drag-icon"><UploadOutlined /></p>
              <p>点击或拖拽文件到此处上传</p>
              <p style={{ color: "#8C8C8C" }}>支持图片和视频格式</p>
            </Upload.Dragger>
          </Form.Item>
          <Form.Item name="ageStage" label="年龄阶段">
            <Select placeholder="选择年龄阶段" allowClear options={[
              { value: "幼猫", label: "幼猫" }, { value: "少年", label: "少年" },
              { value: "成年", label: "成年" }, { value: "老年", label: "老年" },
            ]} />
          </Form.Item>
          <Form.Item name="isAvatar" label="设为头像" valuePropName="checked">
            <Select options={[{ value: true, label: "是" }, { value: false, label: "否" }]} defaultValue={false} />
          </Form.Item>
        </Form>
      </Modal>

      {/* Health Record Modal */}
      <Modal
        title={editingHealthRecord ? "编辑健康记录" : "添加健康记录"}
        open={healthModalOpen}
        onCancel={() => { setHealthModalOpen(false); healthForm.resetFields(); }}
        onOk={() => healthForm.submit()}
      >
        <Form form={healthForm} layout="vertical" onFinish={handleHealthSubmit}>
          <Form.Item name="isSick" label="状态" rules={[{ required: true, message: "请选择状态" }]}>
            <Select options={[
              { value: true, label: "身体异常（生病）" },
              { value: false, label: "健康" },
            ]} placeholder="选择状态" />
          </Form.Item>
          <Form.Item name="diseaseName" label="疾病名称/诊断">
            <Input placeholder="如：猫鼻支、外伤" />
          </Form.Item>
          <Form.Item name="description" label="症状描述">
            <Input.TextArea rows={3} placeholder="描述症状或检查情况" />
          </Form.Item>
          <Form.Item name="treatment" label="治疗措施">
            <Input.TextArea rows={2} placeholder="描述治疗方式" />
          </Form.Item>
          <Form.Item name="recordDate" label="记录日期">
            <DatePicker style={{ width: "100%" }} />
          </Form.Item>
        </Form>
      </Modal>

      {/* Adopt Modal */}
      <Modal title="认养猫咪" open={adoptModalOpen} onCancel={() => setAdoptModalOpen(false)} onOk={() => adoptForm.submit()}>
        <Form form={adoptForm} layout="vertical" onFinish={handleAdopt}>
          <Form.Item name="householdNumber" label="认养户号" rules={[{ required: true, message: "请输入户号" }]}>
            <Input placeholder="如：A1-101" />
          </Form.Item>
          <Form.Item name="adopterName" label="认养人姓名"><Input placeholder="可选" /></Form.Item>
          <Form.Item name="phone" label="联系电话"><Input placeholder="可选" /></Form.Item>
          <Form.Item name="building" label="楼栋"><Input placeholder="如：A1" /></Form.Item>
          <Form.Item name="unitNumber" label="单元号"><Input placeholder="如：101" /></Form.Item>
        </Form>
      </Modal>

      {/* Notify Modal */}
      <Modal title="向认养者推送通知" open={notifyModalOpen} onCancel={() => setNotifyModalOpen(false)} onOk={() => notifyForm.submit()}>
        <Form form={notifyForm} layout="vertical" onFinish={handleNotify}>
          <Form.Item name="title" label="通知标题" rules={[{ required: true, message: "请输入标题" }]}>
            <Input placeholder="如：猫咪疫苗接种提醒" />
          </Form.Item>
          <Form.Item name="content" label="通知内容" rules={[{ required: true, message: "请输入内容" }]}>
            <Input.TextArea rows={4} placeholder="请输入通知内容" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
