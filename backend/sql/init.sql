-- ============================================================================
-- 喵之家 - 小区流浪猫记录管理系统
-- 数据库：PostgreSQL
-- 说明：执行前需先创建数据库
--   CREATE DATABASE miao_home;
--
-- 注意：以下 DROP 会清空已有数据，生产环境谨慎执行
-- ============================================================================

DROP TABLE IF EXISTS mh_cat_health_record CASCADE;
DROP TABLE IF EXISTS mh_cat_location CASCADE;
DROP TABLE IF EXISTS mh_user_login_record CASCADE;
DROP TABLE IF EXISTS mh_notification CASCADE;
DROP TABLE IF EXISTS mh_adopter CASCADE;
DROP TABLE IF EXISTS mh_cat_media CASCADE;
DROP TABLE IF EXISTS mh_cat CASCADE;
DROP TABLE IF EXISTS mh_tenant CASCADE;

-- ============================================================================
-- 1. 租户表（小区）
-- ============================================================================
CREATE TABLE IF NOT EXISTS mh_tenant (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,                  -- 小区名称
    code        VARCHAR(50)  NOT NULL UNIQUE,           -- 小区编码
    building    VARCHAR(50),                            -- 楼栋列表（逗号分隔）
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP  -- 记录创建时间
);
COMMENT ON TABLE mh_tenant IS '租户表 - 小区信息';
COMMENT ON COLUMN mh_tenant.id IS '主键 ID';
COMMENT ON COLUMN mh_tenant.name IS '小区名称';
COMMENT ON COLUMN mh_tenant.code IS '小区编码，全局唯一，用于多租户路由';
COMMENT ON COLUMN mh_tenant.building IS '楼栋列表，逗号分隔，如 "A1,A2,B1"';
COMMENT ON COLUMN mh_tenant.created_at IS '记录创建时间';

-- ============================================================================
-- 2. 猫咪表
-- ============================================================================
CREATE TABLE IF NOT EXISTS mh_cat (
    id          BIGSERIAL    PRIMARY KEY,
    tenant_id   BIGINT       NOT NULL,
    name        VARCHAR(100),
    color       VARCHAR(50),
    gender      VARCHAR(10),
    birth_year  INT,
    description TEXT,
    avatar_url  VARCHAR(500),
    is_adopted  BOOLEAN      DEFAULT FALSE,
    is_neutered BOOLEAN      DEFAULT FALSE,
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE mh_cat IS '猫咪表 - 小区流浪猫基本信息';
COMMENT ON COLUMN mh_cat.id IS '主键 ID';
COMMENT ON COLUMN mh_cat.tenant_id IS '所属小区 ID，关联 mh_tenant(id)';
COMMENT ON COLUMN mh_cat.name IS '猫咪名称，如 "小花"、"橘座"';
COMMENT ON COLUMN mh_cat.color IS '毛色，如 "橘色"、"黑白相间"、"狸花"';
COMMENT ON COLUMN mh_cat.gender IS '性别：公 / 母 / 未知';
COMMENT ON COLUMN mh_cat.birth_year IS '出生年份，用于估算年龄';
COMMENT ON COLUMN mh_cat.description IS '猫咪描述，含特征、习性、健康状况等';
COMMENT ON COLUMN mh_cat.avatar_url IS '头像文件存储相对路径';
COMMENT ON COLUMN mh_cat.is_adopted IS '是否已被认养，true=已认养';
COMMENT ON COLUMN mh_cat.is_neutered IS '是否已绝育，true=已绝育';
COMMENT ON COLUMN mh_cat.created_at IS '记录创建时间';
COMMENT ON COLUMN mh_cat.updated_at IS '记录最后更新时间';

-- ============================================================================
-- 3. 猫咪媒体表（照片 / 视频）
-- ============================================================================
CREATE TABLE IF NOT EXISTS mh_cat_media (
    id          BIGSERIAL    PRIMARY KEY,
    cat_id      BIGINT       NOT NULL,
    tenant_id   BIGINT       NOT NULL,
    media_type  VARCHAR(10)  NOT NULL CHECK (media_type IN ('PHOTO', 'VIDEO')),
    url         VARCHAR(500) NOT NULL,
    age_stage   VARCHAR(50),
    is_avatar   BOOLEAN      DEFAULT FALSE,
    file_name   VARCHAR(255),
    file_size   BIGINT,
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE mh_cat_media IS '猫咪媒体表 - 猫咪的照片和视频';
COMMENT ON COLUMN mh_cat_media.id IS '主键 ID';
COMMENT ON COLUMN mh_cat_media.cat_id IS '关联猫咪 ID，关联 mh_cat(id)';
COMMENT ON COLUMN mh_cat_media.tenant_id IS '所属小区 ID，关联 mh_tenant(id)';
COMMENT ON COLUMN mh_cat_media.media_type IS '媒体类型：PHOTO（照片）/ VIDEO（视频）';
COMMENT ON COLUMN mh_cat_media.url IS '文件存储相对路径，格式 yyyyMMdd/uuid.ext';
COMMENT ON COLUMN mh_cat_media.age_stage IS '拍摄时猫咪所处的年龄阶段：幼猫/少年/成年/老年';
COMMENT ON COLUMN mh_cat_media.is_avatar IS '是否为猫咪头像，每只猫最多一条为 true';
COMMENT ON COLUMN mh_cat_media.file_name IS '上传时的原始文件名';
COMMENT ON COLUMN mh_cat_media.file_size IS '文件大小，单位字节';
COMMENT ON COLUMN mh_cat_media.created_at IS '记录创建时间';

-- ============================================================================
-- 4. 认养表
-- ============================================================================
CREATE TABLE IF NOT EXISTS mh_adopter (
    id               BIGSERIAL    PRIMARY KEY,
    cat_id           BIGINT       NOT NULL,
    tenant_id        BIGINT       NOT NULL,
    user_id          BIGINT,
    household_number VARCHAR(50)  NOT NULL,
    adopter_name     VARCHAR(100),
    phone            VARCHAR(20),
    building         VARCHAR(50),
    unit_number      VARCHAR(20),
    adopted_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    is_active        BOOLEAN      DEFAULT TRUE
);
COMMENT ON TABLE mh_adopter IS '认养表 - 猫咪认养记录';
COMMENT ON COLUMN mh_adopter.id IS '主键 ID';
COMMENT ON COLUMN mh_adopter.cat_id IS '认养的猫咪 ID，关联 mh_cat(id)';
COMMENT ON COLUMN mh_adopter.tenant_id IS '所属小区 ID，关联 mh_tenant(id)';
COMMENT ON COLUMN mh_adopter.user_id IS '认养操作人用户 ID，关联 mh_user(id)';
COMMENT ON COLUMN mh_adopter.household_number IS '认养户号，如 A1-101，小区内唯一标识住户';
COMMENT ON COLUMN mh_adopter.adopter_name IS '认养人姓名';
COMMENT ON COLUMN mh_adopter.phone IS '联系电话';
COMMENT ON COLUMN mh_adopter.building IS '认养人所在楼栋';
COMMENT ON COLUMN mh_adopter.unit_number IS '认养人房间单元号';
COMMENT ON COLUMN mh_adopter.adopted_at IS '认养时间';
COMMENT ON COLUMN mh_adopter.is_active IS '认养是否有效，true=认养中，false=已取消（软删除）';

-- ============================================================================
-- 5. 通知表
-- ============================================================================
CREATE TABLE IF NOT EXISTS mh_notification (
    id          BIGSERIAL    PRIMARY KEY,
    tenant_id   BIGINT       NOT NULL,
    adopter_id  BIGINT,
    cat_id      BIGINT,
    title       VARCHAR(200) NOT NULL,
    content     TEXT,
    is_read     BOOLEAN      DEFAULT FALSE,
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE mh_notification IS '通知表 - 推送给认养人的消息';
COMMENT ON COLUMN mh_notification.id IS '主键 ID';
COMMENT ON COLUMN mh_notification.tenant_id IS '所属小区 ID，关联 mh_tenant(id)';
COMMENT ON COLUMN mh_notification.adopter_id IS '目标认养人 ID，关联 mh_adopter(id)，null 表示全局通知';
COMMENT ON COLUMN mh_notification.cat_id IS '关联猫咪 ID，关联 mh_cat(id)';
COMMENT ON COLUMN mh_notification.title IS '通知标题，如 "猫咪疫苗接种提醒"';
COMMENT ON COLUMN mh_notification.content IS '通知正文';
COMMENT ON COLUMN mh_notification.is_read IS '是否已读';
COMMENT ON COLUMN mh_notification.created_at IS '记录创建时间';

-- ============================================================================
-- 6. 猫咪出没地点表（JSONB 格式，catId+tenantId 联合主键）
-- ============================================================================
CREATE TABLE IF NOT EXISTS mh_cat_location (
    cat_id      BIGINT    NOT NULL,
    tenant_id   BIGINT    NOT NULL,
    building    JSONB     NOT NULL DEFAULT '[]',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (cat_id, tenant_id)
);
COMMENT ON TABLE mh_cat_location IS '猫咪出没地点表 - 记录猫咪在各小区的出现楼栋（JSONB格式），catId+tenantId联合主键';
COMMENT ON COLUMN mh_cat_location.cat_id IS '关联猫咪 ID，联合主键，关联 mh_cat(id)';
COMMENT ON COLUMN mh_cat_location.tenant_id IS '猫咪所属小区 ID，联合主键，关联 mh_tenant(id)';
COMMENT ON COLUMN mh_cat_location.building IS '出没楼栋列表，JSONB格式：[{"tenantId":1,"building":"A1"}]';
COMMENT ON COLUMN mh_cat_location.created_at IS '记录创建时间';
COMMENT ON COLUMN mh_cat_location.updated_at IS '记录最后更新时间';

-- ============================================================================
-- 7. 猫咪健康记录表
-- ============================================================================
CREATE TABLE IF NOT EXISTS mh_cat_health_record (
    id           BIGSERIAL    PRIMARY KEY,
    cat_id       BIGINT       NOT NULL,
    tenant_id    BIGINT       NOT NULL,
    is_sick      BOOLEAN      DEFAULT TRUE,
    disease_name VARCHAR(200),
    description  TEXT,
    treatment    TEXT,
    record_date  DATE         DEFAULT CURRENT_DATE,
    created_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE mh_cat_health_record IS '猫咪健康记录表 - 病史与健康检查记录';
COMMENT ON COLUMN mh_cat_health_record.id IS '主键 ID';
COMMENT ON COLUMN mh_cat_health_record.cat_id IS '关联猫咪 ID，关联 mh_cat(id)';
COMMENT ON COLUMN mh_cat_health_record.tenant_id IS '所属小区 ID，关联 mh_tenant(id)';
COMMENT ON COLUMN mh_cat_health_record.is_sick IS '是否健康异常，true=生病/有恙，false=健康';
COMMENT ON COLUMN mh_cat_health_record.disease_name IS '疾病名称或诊断结果';
COMMENT ON COLUMN mh_cat_health_record.description IS '症状描述，详细说明异常情况';
COMMENT ON COLUMN mh_cat_health_record.treatment IS '治疗措施';
COMMENT ON COLUMN mh_cat_health_record.record_date IS '记录日期';
COMMENT ON COLUMN mh_cat_health_record.created_at IS '记录创建时间';

-- ============================================================================
-- 8. 用户登录记录表
-- ============================================================================
CREATE TABLE IF NOT EXISTS mh_user_login_record (
    user_id       BIGINT    PRIMARY KEY,
    last_login_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE mh_user_login_record IS '用户登录记录表 - 记录每个用户的最近一次登录时间';
COMMENT ON COLUMN mh_user_login_record.user_id IS '用户 ID，主键，关联 mh_user(id)';
COMMENT ON COLUMN mh_user_login_record.last_login_at IS '最近一次登录时间';
COMMENT ON COLUMN mh_user_login_record.updated_at IS '记录最后更新时间';

-- ============================================================================
-- 索引
-- ============================================================================
CREATE INDEX IF NOT EXISTS idx_mh_cat_tenant_id          ON mh_cat(tenant_id);
CREATE INDEX IF NOT EXISTS idx_mh_cat_media_cat_id        ON mh_cat_media(cat_id);
CREATE INDEX IF NOT EXISTS idx_mh_cat_media_tenant_id     ON mh_cat_media(tenant_id);
CREATE INDEX IF NOT EXISTS idx_mh_adopter_tenant_id      ON mh_adopter(tenant_id);
CREATE INDEX IF NOT EXISTS idx_mh_adopter_cat_id         ON mh_adopter(cat_id);
CREATE INDEX IF NOT EXISTS idx_mh_adopter_household_number ON mh_adopter(household_number);
CREATE INDEX IF NOT EXISTS idx_mh_notification_tenant_id ON mh_notification(tenant_id);
CREATE INDEX IF NOT EXISTS idx_mh_notification_adopter_id ON mh_notification(adopter_id);
CREATE INDEX IF NOT EXISTS idx_mh_cat_health_record_cat_id    ON mh_cat_health_record(cat_id);
CREATE INDEX IF NOT EXISTS idx_mh_cat_health_record_tenant_id ON mh_cat_health_record(tenant_id);

-- ============================================================================
-- 示例数据
-- ============================================================================
INSERT INTO mh_tenant (name, code, building) VALUES
('正弘·云庭壹号', 'mht_0000001', '1,2,3,5,6,7,8,9,10,11,12,13,14'),
('仁和佳苑', 'mht_0000002', '1,2,3,5,6,7,8,10,11,12,13,14,15,16,17,18,19,20'),
('颐青园', 'mht_0000003', '1,2,3,5,6,7,8,10,11,12,13,14,15,16,17,18,19,20')
ON CONFLICT (code) DO NOTHING;
