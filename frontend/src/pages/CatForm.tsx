import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { Form, Input, Select, Button, message, Spin, Switch, Upload, Space } from "antd";
import { ArrowLeftOutlined, SaveOutlined, PlusOutlined, DeleteOutlined } from "@ant-design/icons";
import type { UploadFile } from "antd/es/upload/interface";
import { createCat, updateCat, getCatDetail, uploadCatMedia, getTenants } from "../api";
import { PageHeader } from "../components/ui";
import type { Tenant } from "../types";

export default function CatForm() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [form] = Form.useForm();
  const [submitting, setSubmitting] = useState(false);
  const [loading, setLoading] = useState(false);
  const [photoFile, setPhotoFile] = useState<File | null>(null);
  const [fileList, setFileList] = useState<UploadFile[]>([]);
  const [tenants, setTenants] = useState<Tenant[]>([]);
  const isEdit = Boolean(id);

  useEffect(() => {
    getTenants().then((res) => {
      if (res.code === 200) setTenants(res.data);
    });
  }, []);

  useEffect(() => {
    if (id) {
      setLoading(true);
      getCatDetail(Number(id))
        .then((res) => {
          if (res.code === 200) {
            form.setFieldsValue({
              ...res.data,
              isNeutered: res.data.isNeutered ?? false,
            });
          }
        })
        .finally(() => setLoading(false));
    }
  }, [id]);

  const handleSubmit = async (values: any) => {
    setSubmitting(true);
    try {
      if (isEdit) {
        await updateCat(Number(id), values);
        if (photoFile) {
          await uploadCatMedia(Number(id), photoFile, undefined, true);
        }
        message.success("更新成功");
      } else {
        const res = await createCat(values);
        if (res.code === 200 && res.data?.id && photoFile) {
          await uploadCatMedia(res.data.id, photoFile, undefined, true);
        }
        message.success("创建成功");
      }
      navigate("/cats");
    } catch {
      message.error("操作失败");
    } finally {
      setSubmitting(false);
    }
  };

  const handleUploadChange = ({ fileList: newFileList }: any) => {
    setFileList(newFileList);
  };

  const handleBeforeUpload = (file: File) => {
    setPhotoFile(file);
    return false; // 阻止自动上传，手动在提交时上传
  };

  if (loading) return <Spin size="large" style={{ display: "block", margin: "100px auto" }} />;

  return (
    <div>
      <PageHeader
        title={isEdit ? "编辑猫咪信息" : "添加新猫咪"}
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

      <div className="form-container">
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
          initialValues={{ isNeutered: false }}
        >
          <Form.Item name="name" label="猫咪名称">
            <Input placeholder="如：小花" />
          </Form.Item>
          <Form.Item name="color" label="毛色">
            <Input placeholder="如：橘色、黑白" />
          </Form.Item>
          <Form.Item name="gender" label="性别">
            <Select
              placeholder="选择性别"
              options={[
                { value: "公", label: "♂ 公" },
                { value: "母", label: "♀ 母" },
                { value: "未知", label: "未知" },
              ]}
            />
          </Form.Item>
          <Form.Item name="birthYear" label="出生年份">
            <Input type="number" placeholder="如：2024" />
          </Form.Item>
          {/* 经常出现小区 */}
          <Form.Item label="经常出现小区（可选）">
            <Form.List name="frequentCommunities">
              {(fields, { add, remove }) => (
                <>
                  {fields.map(({ key, name, ...rest }) => (
                    <Space key={key} align="baseline" style={{ marginBottom: 8 }}>
                      <Form.Item
                        {...rest}
                        name={[name, "tenantId"]}
                        rules={[{ required: true, message: "请选择小区" }]}
                        style={{ marginBottom: 0 }}
                      >
                        <Select
                          placeholder="选择小区"
                          style={{ width: 160 }}
                          options={tenants.map((t) => ({ value: t.id, label: t.name }))}
                        />
                      </Form.Item>
                      <Form.Item
                        {...rest}
                        name={[name, "building"]}
                        rules={[{ required: true, message: "请输入楼栋" }]}
                        style={{ marginBottom: 0 }}
                      >
                        <Input placeholder="如 A1" style={{ width: 120 }} />
                      </Form.Item>
                      <Button
                        type="text"
                        icon={<DeleteOutlined />}
                        onClick={() => remove(name)}
                        danger
                      />
                    </Space>
                  ))}
                  <Button type="dashed" onClick={() => add()} icon={<PlusOutlined />} size="small">
                    添加经常出现小区
                  </Button>
                </>
              )}
            </Form.List>
          </Form.Item>

          <Form.Item name="isNeutered" label="是否绝育" valuePropName="checked">
            <Switch checkedChildren="已绝育" unCheckedChildren="未绝育" />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea rows={4} placeholder="描述猫咪的特征、习性、健康状况等" />
          </Form.Item>

          {/* 可选照片上传 */}
          <Form.Item label="猫咪照片（可选）">
            <Upload
              listType="picture-card"
              maxCount={1}
              fileList={fileList}
              beforeUpload={handleBeforeUpload}
              onChange={handleUploadChange}
              onRemove={() => {
                setPhotoFile(null);
                setFileList([]);
              }}
              accept="image/*"
            >
              {fileList.length === 0 && (
                <div>
                  <PlusOutlined />
                  <div style={{ marginTop: 8 }}>上传照片</div>
                </div>
              )}
            </Upload>
          </Form.Item>

          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              loading={submitting}
              icon={<SaveOutlined />}
              className="btn-primary"
              style={{ borderRadius: 8, padding: "4px 28px", height: 40 }}
            >
              {isEdit ? "保存修改" : "添加猫咪"}
            </Button>
          </Form.Item>
        </Form>
      </div>
    </div>
  );
}
