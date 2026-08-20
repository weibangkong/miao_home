# 🐱 喵之家 — 小区流浪猫记录管理系统

小区流浪猫信息化管理平台，支持多小区（多租户）、猫咪档案、健康记录、认养管理、通知推送等功能。

---

## 技术栈

### 后端
| 技术 | 版本 |
|---|---|
| Spring Boot | 3.2.4 |
| Java | 17 |
| PostgreSQL | — |
| Spring Data JPA (Hibernate) | — |
| Maven | — |

### 前端
| 技术 | 版本 |
|---|---|
| React | 18.3 |
| TypeScript | 5.4 |
| Ant Design | 5.15 |
| React Router | 6.22 |
| Axios | 1.6 |
| Vite | 5.1 |

---

## 项目结构

```
miao_home/
├── backend/                          # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/miaohome/
│       │   ├── MiaoHomeApplication.java   # 启动入口
│       │   ├── config/                    # 配置类（CORS 等）
│       │   ├── controller/                # 控制器层
│       │   ├── service/                   # 业务逻辑层
│       │   ├── repository/                # 数据访问层（JPA）
│       │   ├── entity/                    # 数据库实体
│       │   ├── dto/                       # 请求/响应 DTO
│       │   ├── exception/                 # 异常处理 + 错误码
│       │   └── tenant/                    # 多租户上下文
│       └── resources/
│           ├── application.yml            # 公共配置（默认激活 dev）
│           ├── application-dev.yml        # 开发环境
│           └── application-pro.yml        # 生产环境
│
├── frontend/                         # React + Vite 前端
│   ├── index.html
│   ├── package.json
│   ├── vite.config.ts                     # Vite 配置（代理 + 端口）
│   └── src/
│       ├── main.tsx                       # 应用入口
│       ├── App.tsx                        # 根组件（布局 + 路由）
│       ├── api/index.ts                   # 后端 API 封装
│       ├── types/index.ts                 # TypeScript 类型定义
│       ├── pages/                         # 页面组件
│       │   ├── Dashboard.tsx              # 数据概览
│       │   ├── CatList.tsx                # 猫咪列表
│       │   ├── CatDetail.tsx              # 猫咪详情
│       │   ├── CatForm.tsx                # 新增/编辑猫咪
│       │   ├── AdopterList.tsx            # 认养管理
│       │   └── NotificationList.tsx       # 通知管理
│       └── components/                    # 可复用组件
│
├── sql/                               # 数据库脚本
│   ├── init.sql                        # 建表 + 初始数据
│   └── create_users.sql                # 数据库用户创建
│
└── uploads/                           # 文件上传目录
```

---

## 功能模块

| 模块 | 功能 |
|---|---|
| 🏠 **仪表盘** | 小区猫咪数量、认养率、绝育率统计 |
| 🐱 **猫咪管理** | 猫咪档案 CRUD、照片/视频上传、头像设置 |
| 🏥 **健康记录** | 猫咪病史记录、治疗措施管理 |
| 👤 **认养管理** | 认养登记、取消认养、按户号搜索 |
| 🔔 **通知管理** | 给认养人发送通知、标记已读/未读 |
| 🏘️ **多小区** | 多租户隔离，通过 Header `X-Tenant-Id` 区分 |

---

## 数据库

### 表结构

| 表 | 说明 |
|---|---|
| `tenants` | 租户表（小区信息） |
| `cats` | 猫咪基本信息 |
| `cat_media` | 猫咪照片/视频 |
| `cat_health_records` | 猫咪健康记录 |
| `adopters` | 认养记录 |
| `notifications` | 通知消息 |

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

### 1. 初始化数据库

```bash
# 用 postgres 超级用户登录，执行建表脚本
psql -h <数据库地址> -p 5432 -U postgres -d miao_home -f backend/sql/init.sql

# 创建应用用户并授权
psql -h <数据库地址> -p 5432 -U postgres -d miao_home -f backend/sql/create_users.sql
```

### 2. 启动后端

```bash
cd backend

# 开发环境（默认 profile = dev）
mvn spring-boot:run

# 生产环境
mvn spring-boot:run -Dspring-boot.run.profiles=pro
```

后端服务启动后运行在 `http://localhost:8080/miaohome/api`

### 3. 启动前端

```bash
cd frontend

# 安装依赖（仅首次）
npm install

# 启动开发服务器
npm run dev
```

前端开发服务器运行在 `http://localhost:3000`，API 请求自动代理到后端 8080 端口。

### 4. 访问系统

浏览器打开 `http://localhost:3000`，默认展示春天花园小区数据。

---

## 环境配置

### 后端环境

| 配置文件 | 数据库账号（`DB_USERNAME`） | DDL 策略 | SQL 日志 | 适用场景 |
|---|---|---|---|---|
| `application-dev.yml` | `miao_home_dev` | `update`（自动建表） | 开启 | 本地开发 |
| `application-pro.yml` | `miao_home` | `validate`（仅校验） | 关闭 | 生产部署 |

### 环境变量（密钥注入）

数据库密码、阿里云 AccessKey 等敏感信息**不写入配置文件**，全部通过环境变量注入。配置文件里的 `${VAR:}` 占位符未注入时为**空值**，应用会启动失败或相关功能不可用。

| 环境变量 | 说明 | 示例 |
|---|---|---|
| `DB_USERNAME` | 数据库账号（dev=`miao_home_dev`，pro=`miao_home`） | `miao_home_dev` |
| `DB_PASSWORD` | 数据库密码 | — |
| `STORAGE_ALIYUN_ENDPOINT` | OSS 服务端点 | `https://oss-cn-hangzhou.aliyuncs.com` |
| `STORAGE_ALIYUN_REGION` | OSS 地域 | `oss-cn-hangzhou` |
| `STORAGE_ALIYUN_BUCKET` | 存储桶名称 | — |
| `STORAGE_ALIYUN_ACCESS_KEY_ID` | RAM 子账号 AccessKey ID | — |
| `STORAGE_ALIYUN_ACCESS_KEY_SECRET` | RAM 子账号 AccessKey Secret | — |
| `STORAGE_ALIYUN_ROLE_ARN` | STS 角色 ARN | `acs:ram::<uid>:role/<role>` |
| `STORAGE_ALIYUN_ROLE_SESSION_NAME` | STS 会话名（可选，默认 `miaohome`） | `miaohome` |

#### 本地开发（IDEA）

在 Run/Debug Configuration → `Environment variables` 中填入上述变量即可，无需改动配置文件、无需创建本地配置文件。

#### 生产部署

按部署方式注入：

- **Docker**：`docker run -e DB_PASSWORD=xxx -e STORAGE_ALIYUN_ACCESS_KEY_ID=xxx ...`
- **systemd / 直接启动**：在服务单元或启动脚本里 `export`，或写入 `/etc/environment`（注意文件权限 `600`）
- **Kubernetes**：用 Secret 挂载为容器环境变量

> 安全提示：环境变量值不要打印进日志；AccessKey 使用 RAM 子账号并授最小权限，禁用主账号；密钥一旦泄露，需在阿里云控制台**禁用并重建**，仅改环境变量无效。

### 前端代理

Vite 开发服务器会自动将 `/api` 开头的请求代理到 `http://localhost:8080`（见 `vite.config.ts`）。

---

## API 概览

| 路径 | 方法 | 说明 |
|---|---|---|
| `/api/tenants` | GET | 获取租户列表 |
| `/api/cats` | GET | 猫咪列表（支持按楼栋筛选） |
| `/api/cats/{id}` | GET | 猫咪详情（含媒体和健康记录） |
| `/api/cats` | POST | 新增猫咪 |
| `/api/cats/{id}` | PUT | 更新猫咪 |
| `/api/cats/{id}` | DELETE | 删除猫咪 |
| `/api/cats/{id}/media` | POST | 上传猫咪媒体文件 |
| `/api/cats/{id}/avatar/{mediaId}` | POST | 设置头像 |
| `/api/cats/media/{mediaId}` | DELETE | 删除媒体文件 |
| `/api/cats/{id}/health-records` | GET / POST | 健康记录列表 / 新增 |
| `/api/cats/{id}/health-records/{rid}` | PUT / DELETE | 更新 / 删除健康记录 |
| `/api/adopters` | GET | 认养列表 |
| `/api/adopters/adopt/{catId}` | POST | 认养猫咪 |
| `/api/adopters/{id}` | DELETE | 取消认养 |
| `/api/notifications` | GET | 通知列表 |
| `/api/notifications/send` | POST | 发送通知 |
| `/api/notifications/{id}/read` | PUT | 标记已读 |

---

## 多租户说明

系统通过 HTTP Header `X-Tenant-Id` 实现多小区数据隔离，默认值为 `1`。前端页面顶部可选择切换小区，切换后刷新并重新加载对应小区数据。

---

## 编码规范

后端遵循 [JAVA_GUIDE](~/.claude/rules/JAVA_GUIDE.md) 规范：
- 禁止 Lombok，getter/setter 手写
- 所有类、方法、字段必须有注释
- Controller 仅做参数校验和返回，业务逻辑在 Service
- 统一返回 `ApiResult<T>` 格式
- 异常通过 `BusinessException` + `ErrorCode` 枚举管理

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

---

## 设计文档

- [OSS 文件上传与图片回显设计文档](docs/oss-upload-and-image-serving-design.md)
