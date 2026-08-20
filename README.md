# 🐱 喵之家 — 小区流浪猫记录管理系统

小区流浪猫信息化管理平台，支持多小区（多租户）、猫咪档案、健康记录、认养管理、评论互动、通知推送等功能。提供 **React 管理端** 与 **微信小程序端** 两套前端。

---

## 技术栈

### 后端
| 技术 | 版本 |
|---|---|
| Spring Boot | 3.2.4 |
| Java | 17 |
| PostgreSQL | — |
| Spring Data JPA (Hibernate) | — |
| Springdoc OpenAPI (Swagger) | — |
| Maven | — |

### 前端（React 管理端）
| 技术 | 版本 |
|---|---|
| React | 18.3 |
| TypeScript | 5.4 |
| Ant Design | 5.15 |
| React Router | 6.22 |
| Axios | 1.6 |
| Recharts | 3.x（仪表盘图表） |
| Day.js | 1.11 |
| ali-oss | 6.x（OSS 前端直传） |
| @jsquash/jpeg + @jsquash/webp | 1.x（WASM 图片压缩） |
| Vite | 5.1 |

### 前端（微信小程序端）
| 技术 | 说明 |
|---|---|
| 原生小程序 | 无框架，`wx.request` / `wx.uploadFile` |
| 自定义组件 | `cat-card`、`gender-badge` |

---

## 项目结构

```
miao_home/
├── backend/                              # Spring Boot 后端
│   ├── pom.xml
│   ├── sql/                              # 数据库脚本
│   │   ├── init.sql                      # 核心建表 + 初始数据
│   │   ├── create_users.sql              # 数据库用户创建
│   │   ├── init_comments.sql             # 评论/点赞建表
│   │   └── init_user_login_record.sql    # 登录记录建表
│   └── src/main/
│       ├── java/com/miaohome/
│       │   ├── MiaoHomeApplication.java  # 启动入口
│       │   ├── config/                   # 配置类（认证拦截、存储、微信、CORS 等）
│       │   ├── controller/               # 控制器层（认证/猫咪/认养/通知/文件/租户/管理）
│       │   ├── service/                  # 业务逻辑层
│       │   │   └── file/                 # 文件存储子包（OSS STS / OSS / 本地存储）
│       │   ├── repository/               # 数据访问层（JPA）
│       │   ├── entity/                   # 数据库实体
│       │   ├── dto/                      # 请求/响应 DTO
│       │   ├── exception/                # 异常处理 + 错误码
│       │   ├── tenant/                   # 多租户上下文 + 过滤器
│       │   ├── log/                      # 日志标记 + 请求追踪
│       │   └── comment/                  # 评论与点赞业务模块（entity/dto/repository/service/controller）
│       └── resources/
│           ├── application.yml           # 公共配置（默认激活 dev）
│           ├── application-dev.yml       # 开发环境
│           ├── application-pro.yml       # 生产环境
│           └── logback-spring.xml        # 日志配置
│
├── frontend/                             # React + Vite 管理端
│   ├── index.html
│   ├── package.json
│   ├── vite.config.ts                    # Vite 配置（代理 + 端口 3000）
│   └── src/
│       ├── main.tsx / App.tsx            # 应用入口 / 根组件（布局 + 路由）
│       ├── api/index.ts                  # 后端 API 封装（含图片压缩接入）
│       ├── types/                        # TypeScript 类型定义（含 ali-oss 声明）
│       ├── contexts/AuthContext.tsx      # 登录态上下文
│       ├── theme/                        # 主题 token 与颜色常量
│       ├── utils/                        # oss.ts（OSS 直传）、imageCompress.ts（图片压缩）
│       ├── components/                   # 可复用组件
│       │   ├── layout/                   # 顶部栏 / 侧边栏
│       │   ├── ui/                       # Card / PageHeader / StatCard / StatGroup
│       │   ├── CommentSection.tsx        # 评论区
│       │   ├── RequireAuth.tsx / GuestOnly.tsx
│       │   └── ...                       # 徽章、气泡计数等
│       └── pages/                        # Dashboard / CatList / CatDetail / CatForm
│                                         # AdopterList / NotificationList / Login / Register
│
├── frontend-miniapp/                     # 微信小程序端
│   ├── app.js / app.json / app.wxss      # 小程序入口与全局配置
│   ├── project.config.json               # 开发者工具项目配置
│   ├── images/                           # tabBar 图标
│   ├── components/                       # 自定义组件（cat-card、gender-badge）
│   ├── utils/                            # api.js（请求封装）、config.js（后端地址）
│   └── pages/                            # index(猫咪) / range(范围) / notifications(通知)
│                                         # mine(我的) / cat-detail / adopt(认养) / community(社区)
│
├── docs/                                 # 设计文档
│   ├── oss-upload-and-image-serving-design.md   # OSS 上传与图片回显设计
│   └── image-compression-design.md              # 前端图片压缩设计
│
├── ststest/                              # 阿里云 RAM STS 签发样例（独立 Java 工程）
├── uploads/                              # 本地存储模式的文件上传目录
└── logs/                                 # 应用日志输出目录
```

---

## 功能模块

| 模块 | 功能 |
|---|---|
| 🏠 **仪表盘** | 小区猫咪数量、认养率、绝育率统计 |
| 🐱 **猫咪管理** | 猫咪档案 CRUD、照片/视频上传、头像设置 |
| 🏥 **健康记录** | 猫咪病史记录、治疗措施管理 |
| 👤 **认养管理** | 认养登记、取消认养、按户号搜索 |
| 💬 **评论与点赞** | 猫咪点赞/取消、发表/删除评论、评论点赞 |
| 🔔 **通知管理** | 给认养人发送通知、标记已读/未读、SSE 实时推送 |
| 👥 **用户认证** | 手机号注册/登录、微信小程序登录/绑定、头像更新 |
| 🏘️ **多小区** | 多租户隔离，通过 Header `X-Tenant-Id` 区分 |
| 📱 **微信小程序** | 猫咪浏览、范围看板、通知、社区评论、个人中心 |

---

## 数据库

### 表结构

表名统一使用 `mh_` 前缀、单数形式，关联字段在字段注释中标注（不使用物理外键）。

| 表 | 说明 |
|---|---|
| `mh_tenant` | 租户（小区信息） |
| `mh_user` | 用户 |
| `mh_user_auth_provider` | 用户认证渠道（手机号 / 微信） |
| `mh_user_login_record` | 用户登录记录 |
| `mh_cat` | 猫咪基本信息（含点赞数冗余字段） |
| `mh_cat_media` | 猫咪照片/视频 |
| `mh_cat_health_record` | 猫咪健康记录 |
| `mh_cat_location` | 猫咪位置信息 |
| `mh_cat_comment` | 猫咪评论 |
| `mh_cat_comment_like` | 评论点赞 |
| `mh_cat_like` | 猫咪点赞 |
| `mh_adopter` | 认养记录 |
| `mh_notification` | 通知消息 |

### 数据库用户

| 用户名 | 权限 | 用途 |
|---|---|---|
| `miao_home` | DML（增删改查） | 生产环境应用连接 |
| `miao_home_dev` | DML + DDL（建表/改表） | 开发环境应用连接 |

---

## 快速开始

### 前置条件

- **JDK 17**（需配置 `JAVA_HOME`）
- **Maven** 3.5+
- **Node.js** 18+
- **PostgreSQL**（已创建数据库 `miao_home`）
- **微信开发者工具**（运行小程序端时需要）

### 1. 初始化数据库

```bash
# 用 postgres 超级用户登录，执行建表脚本
psql -h <数据库地址> -p 5432 -U postgres -d miao_home -f backend/sql/init.sql
psql -h <数据库地址> -p 5432 -U postgres -d miao_home -f backend/sql/init_comments.sql
psql -h <数据库地址> -p 5432 -U postgres -d miao_home -f backend/sql/init_user_login_record.sql

# 创建应用用户并授权
psql -h <数据库地址> -p 5432 -U postgres -d miao_home -f backend/sql/create_users.sql
```

> 开发环境 `ddl-auto=update` 会自动建表，脚本主要用于生产环境与初始数据。

### 2. 启动后端

```bash
cd backend

# 开发环境（默认 profile = dev）
mvn spring-boot:run

# 生产环境
mvn spring-boot:run -Dspring-boot.run.profiles=pro
```

后端服务启动后运行在 `http://localhost:8080/miaohome/api`，接口文档见 `http://localhost:8080/miaohome/api/swagger-ui/index.html`。

### 3. 启动管理端前端

```bash
cd frontend

# 安装依赖（仅首次）
npm install

# 启动开发服务器
npm run dev
```

前端开发服务器运行在 `http://localhost:3000`，`/miaohome/api` 请求自动代理到后端 8080 端口。

### 4. 运行微信小程序端

1. 使用「微信开发者工具」导入 `frontend-miniapp` 目录；
2. 在 `project.config.json` / 开发者工具中填写小程序 AppID；
3. 后端地址在 `frontend-miniapp/utils/config.js` 的 `BASE_URL` 中配置（开发默认 `http://127.0.0.1:8080/miaohome/api`）；
4. 编译运行即可。真机/生产环境需在小程序后台配置 `request`、`downloadFile`、`uploadFile` 合法域名。

---

## 环境配置

### 后端环境

| 配置文件 | 数据库账号（`DB_USERNAME`） | DDL 策略 | SQL 日志 | 存储 | 适用场景 |
|---|---|---|---|---|---|
| `application-dev.yml` | `miao_home_dev` | `update`（自动建表） | 开启 | 阿里云 OSS（地址硬编码青岛 region） | 本地开发 |
| `application-pro.yml` | `miao_home` | `validate`（仅校验） | 关闭 | 阿里云 OSS（地址走环境变量） | 生产部署 |

### 环境变量（密钥注入）

数据库密码、阿里云 AccessKey 等敏感信息**不写入配置文件**，全部通过环境变量注入。配置文件里的 `${VAR:}` 占位符未注入时为**空值**，应用会启动失败或相关功能不可用。

| 环境变量 | 说明 | 示例 |
|---|---|---|
| `DB_USERNAME` | 数据库账号（dev=`miao_home_dev`，pro=`miao_home`） | `miao_home_dev` |
| `DB_PASSWORD` | 数据库密码 | — |
| `STORAGE_TYPE` | 存储类型（`aliyun` / `local`，默认 `local`） | `aliyun` |
| `STORAGE_ALIYUN_ENDPOINT` | OSS 服务端点（pro 用） | `https://oss-cn-qingdao.aliyuncs.com` |
| `STORAGE_ALIYUN_REGION` | OSS 地域（pro 用） | `oss-cn-qingdao` |
| `STORAGE_ALIYUN_BUCKET` | 存储桶名称（pro 用） | `miao-home` |
| `STORAGE_ALIYUN_ACCESS_KEY_ID` | RAM 子账号 AccessKey ID | — |
| `STORAGE_ALIYUN_ACCESS_KEY_SECRET` | RAM 子账号 AccessKey Secret | — |
| `STORAGE_ALIYUN_ROLE_ARN` | STS 角色 ARN | `acs:ram::<uid>:role/<role>` |
| `ALIYUN_OSS_ROLE_SESSION_NAME` | STS 会话名（可选，默认 `miaohome`） | `miaohome` |
| `ALIYUN_OSS_PUBLIC_BASE_URL` | 公共读对象访问前缀（如 `https://<bucket>.<endpoint>`） | `https://miao-home.oss-cn-qingdao.aliyuncs.com` |
| `ALIYUN_OSS_URL_EXPIRATION` | 签名 URL 有效期秒数（默认 3600） | `3600` |
| `WECHAT_APPID` | 微信小程序 AppID | — |
| `WECHAT_SECRET` | 微信小程序 Secret | — |

#### 本地开发（IDEA）

在 Run/Debug Configuration → `Environment variables` 中填入上述变量即可，无需改动配置文件、无需创建本地配置文件。

#### 生产部署

按部署方式注入：

- **Docker**：`docker run -e DB_PASSWORD=xxx -e STORAGE_ALIYUN_ACCESS_KEY_ID=xxx ...`
- **systemd / 直接启动**：在服务单元或启动脚本里 `export`，或写入 `/etc/environment`（注意文件权限 `600`）
- **Kubernetes**：用 Secret 挂载为容器环境变量

> 安全提示：环境变量值不要打印进日志；AccessKey 使用 RAM 子账号并授最小权限，禁用主账号；密钥一旦泄露，需在阿里云控制台**禁用并重建**，仅改环境变量无效。

### 前端代理

Vite 开发服务器会自动将 `/miaohome/api` 开头的请求代理到 `http://localhost:8080`（见 `vite.config.ts`）。

---

## API 概览

所有接口统一返回 `ApiResult<T>`，路径前缀为 `/miaohome/api`。下方以 `/api` 简写。

### 用户认证 `/users`

| 路径 | 方法 | 说明 |
|---|---|---|
| `/api/users/register` | POST | 手机号注册 |
| `/api/users/login` | POST | 手机号登录 |
| `/api/users/logout` | POST | 登出 |
| `/api/users/me` | GET | 获取当前登录用户 |
| `/api/users/me/avatar` | PUT | 更新当前用户头像（回传 OSS 对象键） |
| `/api/users/{userId}` | GET | 获取用户公开信息 |
| `/api/users/wechat/login` | POST | 微信小程序登录/注册 |
| `/api/users/wechat/bind` | POST | 给当前用户绑定微信 |
| `/api/users/bindings/list` | GET | 当前用户已绑定的认证渠道列表 |

### 猫咪 `/cats`

| 路径 | 方法 | 说明 |
|---|---|---|
| `/api/cats/list` | GET | 猫咪列表（支持按楼栋筛选） |
| `/api/cats/{id}` | GET | 猫咪详情（含媒体、健康记录） |
| `/api/cats` | POST | 新增猫咪 |
| `/api/cats/{id}` | PUT | 更新猫咪 |
| `/api/cats/{id}` | DELETE | 删除猫咪 |
| `/api/cats/{id}/media` | POST | 上传猫咪媒体（后端代理上传） |
| `/api/cats/{id}/media/confirm` | POST | STS 直传后确认落库 |
| `/api/cats/media/{mediaId}` | DELETE | 删除媒体文件 |
| `/api/cats/{catId}/avatar/{mediaId}` | POST | 设置头像 |
| `/api/cats/{catId}/health/records/list` | GET | 健康记录列表 |
| `/api/cats/{catId}/health/records` | POST | 新增健康记录 |
| `/api/cats/{catId}/health/records/{recordId}` | PUT / DELETE | 更新 / 删除健康记录 |

### 评论与点赞

| 路径 | 方法 | 说明 |
|---|---|---|
| `/api/cats/{catId}/like` | POST | 点赞 / 取消点赞 |
| `/api/cats/{catId}/like/status` | GET | 查询当前用户是否已点赞 |
| `/api/cats/{catId}/comments/list` | GET | 猫咪评论列表 |
| `/api/cats/{catId}/comments` | POST | 发表评论/回复 |
| `/api/comments/{commentId}` | DELETE | 删除评论（仅创建者） |
| `/api/comments/{commentId}/like` | POST | 评论点赞 / 取消 |

### 认养 `/adopters`

| 路径 | 方法 | 说明 |
|---|---|---|
| `/api/adopters/list` | GET | 认养列表 |
| `/api/adopters/cat/{catId}/list` | GET | 某猫咪的认养人列表 |
| `/api/adopters/adopt/{catId}` | POST | 认养猫咪 |
| `/api/adopters/{id}` | DELETE | 取消认养 |
| `/api/adopters/search` | GET | 按户号搜索 |

### 通知 `/notifications`

| 路径 | 方法 | 说明 |
|---|---|---|
| `/api/notifications/list` | GET | 通知列表 |
| `/api/notifications/adopter/{adopterId}/list` | GET | 某认养人的通知列表 |
| `/api/notifications/adopter/{adopterId}/unread/count` | GET | 未读数量 |
| `/api/notifications/{id}/read` | PUT | 标记已读 |
| `/api/notifications/adopter/{adopterId}/read/all` | PUT | 全部标记已读 |
| `/api/notifications/send` | POST | 发送通知 |
| `/api/notifications/cat/{catId}/adopters/send` | POST | 给某猫咪所有认养人发送 |
| `/api/notifications/subscribe/{adopterId}` | GET | SSE 实时订阅 |

### 文件 `/files`

| 路径 | 方法 | 说明 |
|---|---|---|
| `/api/files/sts/credentials` | POST | 获取 OSS STS 临时凭证 |
| `/api/files/{*relativePath}` | GET | 本地存储模式的代理访问 |

### 租户 `/tenants` 与管理员 `/admin`

| 路径 | 方法 | 说明 |
|---|---|---|
| `/api/tenants/list` | GET | 租户列表 |
| `/api/tenants` | POST | 新增租户 |
| `/api/tenants/{id}` | DELETE | 删除租户 |
| `/api/admin/super/admins` | POST | 创建超级管理员 |

---

## 多租户说明

系统通过 HTTP Header `X-Tenant-Id` 实现多小区数据隔离，默认值为 `1`。前端页面顶部可选择切换小区，切换后刷新并重新加载对应小区数据。

---

## 文件上传与图片回显

图片统一走「阿里云 OSS STS 前端直传 + `public/` 目录公共读」方案，文件不流经后端：

1. 前端调用 `POST /files/sts/credentials` 获取临时凭证；
2. `ali-oss`（Web）/ `wx.uploadFile`（小程序，当前仍走后端代理）上传 OSS；
3. 上传成功后回传 objectKey 确认落库；
4. 回显走固定公开 URL（`publicBaseUrl + key`）直连 OSS，不再签发临时签名 URL。

上传前前端会对待上传图片做一次 WASM 压缩（`@jsquash`），在不明显损失画质的前提下缩小体积。详见设计文档。

---

## 编码规范

后端遵循 [JAVA_GUIDE](~/.claude/rules/JAVA_GUIDE.md) 规范：
- 禁止 Lombok，getter/setter 手写
- 所有类、方法、字段必须有注释
- Controller 仅做参数校验和返回，业务逻辑在 Service
- 统一返回 `ApiResult<T>` 格式
- 异常通过 `BusinessException` + `ErrorCode` 枚举管理
- 业务模块独立子包（如 `comment/`），不散落在各层顶层包

---

## 待办事项（TODO）

### 图片 CDN + 自定义域名 + 防盗链（暂缓，方案已定）

> 背景：当前图片走 OSS `public/` 目录公共读 + 固定 URL 直连回显，未接 CDN。以下为后续完整实施方案。

1. **域名准备**：注册并完成 ICP 备案一个用于图片加速的自定义域名，如 `img.miaohome.com`。
2. **OSS 绑定域名**：在 OSS 控制台为该 bucket 绑定自定义域名（供 CDN 回源使用）。
3. **开通 CDN**：创建 CDN 加速域名 `img.miaohome.com`，回源地址指向 OSS bucket 的 endpoint（或 OSS 绑定域名），回源方式选「OSS 私有 bucket 回源」（若 bucket 后续改为私有，需配置回源鉴权）。
4. **HTTPS**：为该域名申请证书（阿里云 SSL 证书或自有证书），在 CDN 配置 HTTPS 并开启强制跳转。
5. **防盗链**：在 CDN（或 OSS）配置 Referer 白名单，仅允许本站域名，防止第三方盗链刷流量。
6. **图片处理**：开启 OSS 图片处理能力，列表/头像使用缩略图参数 `?x-oss-process=image/resize,w_xxx`，避免加载原图。
7. **后端配置**：将 `storage.aliyun.public-base-url` 由 bucket 直连域名切换为 `https://img.miaohome.com`。
8. **小程序域名**：在微信公众平台配置 `downloadFile` 合法域名 = `https://img.miaohome.com`。
9. **缓存策略**：图片对象设置较长缓存 TTL（文件名已用 UUID 保证版本化，可放心长缓存）。

### OSS 上传回调 / 孤儿对象清理（暂缓）

- 前端「上传成功、confirm 失败」会残留无记录 OSS 对象，后续可用 OSS 上传回调 `x-oss-callback` 直接由 OSS 回调后端落库，或定时清理孤儿对象。

### HEIC 图片压缩（暂缓）

- iPhone 默认拍摄 HEIC/HEIF 格式，当前前端压缩链路对 HEIC 原样上传（Chrome 侧可能裂图）。后续引入 `heic2any` 前端转码补齐，见 [图片压缩设计文档](docs/image-compression-design.md)。

---

## 设计文档

- [OSS 文件上传与图片回显设计文档](docs/oss-upload-and-image-serving-design.md)
- [前端图片压缩设计文档](docs/image-compression-design.md)
