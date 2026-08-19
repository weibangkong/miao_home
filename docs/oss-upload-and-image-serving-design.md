# OSS 文件上传与图片回显设计文档

> 版本：v1.0　日期：2026-08-15　状态：待审核

## 1. 背景与目标

当前系统已实现阿里云 OSS 的 STS 前端直传（`AliyunStsService` 签发临时凭证 + 前端 `ali-oss` 直传），但回显仍走「后端 `generateAccessUrl` 生成 presigned 临时签名 URL」的方式（默认 3600s 过期）。

本次改造目标：

1. **上传**：用户头像、猫咪头像、猫咪图片三类资源统一走 STS 前端直传，不流经后端。
2. **回显**：三类资源全部公共读，后端返回固定公开 URL，前端直连 OSS 回显（不再签 presigned）。
3. **选型**：采用「`public/` 目录前缀 + Bucket Policy 匿名只读」实现公共读（详见 §3）。
4. CDN / 自定义域名 / 防盗链本次**不实施**，作为待办记录（详见 §8 与 README）。

## 2. 现状分析

| 环节 | 现状 | 位置 |
|---|---|---|
| STS 凭证签发 | 已实现，policy 限 `oss:PutObject` 到 `<bucket>/<yyyyMMdd>/*` | `AliyunStsService.java` |
| 前端直传 | 已实现，`ali-oss` 客户端 + 凭证缓存，key = `yyyyMMdd/uuid.ext` | `frontend/src/utils/oss.ts` |
| 直传结果落库 | `/cats/{id}/media/confirm` 只落元数据 | `CatController.java` |
| 回显 | `generateAccessUrl()` 对 OSS 返回 presigned URL（3600s） | `AliyunOssStorageService.java` |
| 删除清理 | 删记录同时 `deleteFile` 清理 OSS 对象 | `CatMediaService.java` |
| 用户头像 | 微信头像直接存第三方 URL，无主动上传头像接口 | `UserService.java` |
| 本地存储兜底 | `storage.type=local` 时返回相对路径，前端拼 `/files/` 代理 | `LocalFileStorageService.java` |

结论：STS 直传链路已就绪，本次核心改动是 **key 结构增加 `public/` 前缀 + 回显 URL 由 presigned 改为固定公开 URL**。

## 3. 方案选型

### 3.1 对比

| 维度 | 对象级 ACL（`x-oss-object-acl: public-read`） | `public/` 目录前缀（Bucket Policy） |
|---|---|---|
| 权限控制点 | 对象级，桶私有 | `public/*` 前缀，桶私有 |
| 前端上传负担 | 必须带 ACL header，易漏 | 零负担 |
| STS 权限面 | 需 `oss:PutObject` + `oss:PutObjectAcl` | 仅 `oss:PutObject` |
| key 结构 | 不变，存量不迁移 | 变 `public/yyyyMMdd/uuid` |
| 审计排查 | 分散到对象 | 集中在一条 Bucket Policy |
| 单对象转私有 | 可随时改回 | 需移 key 或叠加对象 ACL |

### 3.2 结论

**采用 `public/` 目录前缀 + Bucket Policy 匿名只读。** 理由见正文，此处不赘述。若未来需要「公开目录内个别对象临时下架」，可对单对象叠加 `x-oss-object-acl: private` 覆盖，与本方案不冲突。

## 4. 总体架构

### 4.1 上传链路（STS 直传，已有，微调）

```
前端/小程序
   │  1. POST /files/sts/credentials（获取临时凭证 + dir）
   ▼
后端 AliyunStsService（AssumeRole，policy 限 public/ 前缀 PutObject）
   │  2. 返回 {accessKeyId, accessKeySecret, securityToken, dir="public/yyyyMMdd/", ...}
   ▼
前端 ali-oss 直传 OSS（key = public/yyyyMMdd/uuid.ext）
   │  3. 上传成功，拿到 objectKey
   ▼
前端 POST /cats/{id}/media/confirm（回传 objectKey + 元数据，仅落库）
```

### 4.2 回显链路（公共读，本次改造）

```
前端拿到后端返回的固定公开 URL（publicBaseUrl + key）
   │
   ▼
直连 OSS 读取（bucket 已配 public/* 匿名 GetObject，无需签名）
```

## 5. 详细设计

### 5.1 目录 / key 结构

- 统一约定所有公开图片 key 结构：`public/yyyyMMdd/uuid.ext`
- `yyyyMMdd` 为上传日期目录，`uuid.ext` 为随机文件名（UUID 保证版本化、天然支持长缓存）。

### 5.2 Bucket 与权限配置（一次性控制台/API 配置，非代码）

桶 ACL 保持 **私有（private）**，新增一条 Bucket Policy 对 `public/*` 前缀授匿名只读：

```json
{
  "Version": "1",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": ["*"],
      "Action": ["oss:GetObject"],
      "Resource": ["acs:oss:*:*:<bucket>/public/*"]
    }
  ]
}
```

RAM 角色（STS 所用）权限保持不变：仅 `oss:PutObject`，资源前缀收窄到 `public/*`。

### 5.3 后端改动

**① `AliyunStsService`**：`dir` 由 `yyyyMMdd/` 改为 `public/yyyyMMdd/`；上传 policy 的 `Resource` 由 `<bucket>/<yyyyMMdd>/*` 改为 `<bucket>/public/<yyyyMMdd>/*`，并增加文件大小 `Condition`（`oss:ContentLength` 上限，如 10MB）限制防刷。

**② `StorageConfig.AliyunOssProperties`**：新增配置项 `publicBaseUrl`（如 `https://<bucket>.<endpoint>`），为将来切 CDN 域名预留（本次填 bucket 直连域名）。

**③ `FileStorageService` / `AliyunOssStorageService`**：`generateAccessUrl(key)` 语义调整为：
- key 以 `public/` 开头 → 返回 `publicBaseUrl + "/" + key`（固定公开 URL，不再签 presigned）；
- 其余（未来 private 前缀）→ 仍走 presigned。

**④ `CatController.confirmMediaObject`**：objectKey 校验正则由 `^\d{8}/[\w.\-]+$` 改为 `^public/\d{8}/[\w.\-]+$`，并增加扩展名白名单（图片/视频）与 fileSize 上限兜底校验。

**⑤ 配置（application.yml）**：`storage.aliyun` 增加 `public-base-url: ${ALIYUN_OSS_PUBLIC_BASE_URL:}`，dev/pro 环境变量同步。

### 5.4 前端（React）改动

- `uploadToOss`：无需改逻辑，`dir` 由后端返回 `public/yyyyMMdd/`，key 自动带 `public/` 前缀。
- `getMediaUrl`：已兼容（`http(s)` 开头直用，否则拼 `/miaohome/api/files/`）。OSS 下后端返回固定 http(s) URL，直接使用；local 下返回相对路径 `public/yyyyMMdd/uuid`，拼代理路径。
- 列表/详情缩略图：后续接入图片处理参数（见 §8），本次不强制。

### 5.5 小程序（frontend-miniapp）改动

- 回显：`getFileUrl` 增加判断——后端返回完整 `http(s)` URL 时直接使用，返回相对路径时才拼 `UPLOAD_BASE`。
- 上传：当前小程序仍走 `wx.uploadFile` 后端代理，本次不做 STS 直传改造（不影响回显）。
- 域名：公共 URL 若为自定义 https 域名，需在小程序后台配置 `downloadFile` 合法域名（见 §8）。

### 5.6 用户头像

- **微信头像**：直接为微信第三方公开 URL（`thirdwx.qlogo.cn`），`toUserResponse` 原样返回即可，无需 OSS、无需改造。
- **用户主动上传头像**：当前无此功能。如需实现，复用猫咪媒体同款链路（STS 直传 → `public/` 目录 → 新增 `PUT /users/me/avatar` 接口回传 objectKey 落库）。本次可选项，非必需。

## 6. 安全与边界

1. **文件大小**：STS policy `Condition` 限 `oss:ContentLength` 上限；前端 `accept` 限制类型；后端 confirm 二次校验扩展名 + 大小。
2. **objectKey 合法性**：confirm 严格校验 `^public/\d{8}/[\w.\-]+$`，防止伪造/路径穿越。
3. **归属校验（可选）**：当前为公开社区资源，归属校验优先级低；若未来有私密资源，需校验 objectKey 归属当前用户，或改用 OSS 上传回调 `x-oss-callback` 落库。
4. **孤儿对象**：前端「上传成功、confirm 失败」会留下无记录对象，建议后续定时清理或引入 OSS 回调（记入待办，非本次）。
5. **删除一致性**：`deleteMedia` 已删 OSS 对象；公共读无 CDN 时删除即时生效，接 CDN 后需接受缓存窗口或刷新（见 §8）。

## 7. 本地存储兼容

`storage.type=local`（开发默认）保持不变：`generateAccessUrl` 返回相对路径，前端拼 `/miaohome/api/files/` 代理访问。key 结构建议与 OSS 对齐为 `public/yyyyMMdd/uuid.ext`，`FileController` 代理路径相应支持 `public/{dateStr}/{fileName}`（由 `/{dateStr}/{fileName:.+}` 调整）。

## 8. 待办（暂缓实施，完整方案见 README）

1. 图片 CDN + 自定义域名 + 防盗链 + 图片处理缩略图；
2. OSS 上传回调 / 孤儿对象定时清理。

## 9. 影响面与改造清单

| 模块 | 文件 | 改动 |
|---|---|---|
| 后端 | `AliyunStsService.java` | dir 加 `public/` 前缀，policy Resource/Condition 调整 |
| 后端 | `StorageConfig.java` | 新增 `publicBaseUrl` 配置项 |
| 后端 | `AliyunOssStorageService.java` | `generateAccessUrl` 对 `public/` 返回固定 URL |
| 后端 | `CatController.java` | confirm objectKey 校验正则 + 类型/大小兜底 |
| 后端 | `application.yml` / dev / pro | 新增 `public-base-url` 环境变量 |
| 后端 | `FileController.java` | 代理路径支持 `public/{dateStr}/{fileName}` |
| 前端 | `oss.ts` / `api/index.ts` | 基本无逻辑改动（dir 自适应） |
| 小程序 | `utils/api.js` | `getFileUrl` 增加完整 URL 判断 |
| 基础设施 | OSS 控制台 | 一次性配置 Bucket Policy（`public/*` 匿名只读） |

## 10. 上线步骤

1. OSS 控制台配置 Bucket Policy（`public/*` 匿名只读）。
2. 后端合并上述代码改动 + 新增 `public-base-url` 配置。
3. 前端/小程序合并回显判断改动。
4. 存量数据迁移（如 OSS 已有 `yyyyMMdd/` 对象，批量移动 key 到 `public/` 前缀，同步 DB）。
5. 联调验证：上传 → 直传成功 → confirm 落库 → 前端直连固定 URL 回显 → 删除清理。
