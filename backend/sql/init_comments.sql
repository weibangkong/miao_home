-- ============================================================================
-- 评论与点赞功能 - 数据库迁移
-- 数据库：PostgreSQL
--
-- 注意：以下 DROP 会清空已有数据，生产环境谨慎执行
-- ============================================================================

DROP TABLE IF EXISTS mh_cat_comment_like CASCADE;
DROP TABLE IF EXISTS mh_cat_like CASCADE;
DROP TABLE IF EXISTS mh_cat_comment CASCADE;
DROP TABLE IF EXISTS mh_user_auth_provider CASCADE;
DROP TABLE IF EXISTS mh_user CASCADE;

-- ============================================================================

-- ============================================================================
-- 1. 用户表（生产版，手机号 + 密码登录）
-- ============================================================================
CREATE TABLE IF NOT EXISTS mh_user (
    id            BIGSERIAL    PRIMARY KEY,
    phone         VARCHAR(20)  UNIQUE,
    password_hash VARCHAR(200),
    nickname      VARCHAR(100) NOT NULL,
    avatar_url    VARCHAR(500),
    user_type     VARCHAR(2)   NOT NULL DEFAULT '02',
    created_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE mh_user IS '社区用户表 - 支持手机号密码登录及多渠道认证';
COMMENT ON COLUMN mh_user.id IS '主键 ID';
COMMENT ON COLUMN mh_user.phone IS '手机号，唯一。微信用户可为空';
COMMENT ON COLUMN mh_user.password_hash IS 'BCrypt 加密后的密码哈希。微信用户可为空';
COMMENT ON COLUMN mh_user.nickname IS '用户昵称，对外显示';
COMMENT ON COLUMN mh_user.avatar_url IS '头像地址';
COMMENT ON COLUMN mh_user.user_type IS '用户类型：00-超级管理员，01-管理员，02-普通用户';
COMMENT ON COLUMN mh_user.created_at IS '记录创建时间';

-- 迁移已有数据库：增加手机号和密码字段（新部署环境跳过，建表语句已包含）
ALTER TABLE mh_user ADD COLUMN IF NOT EXISTS phone VARCHAR(20);
ALTER TABLE mh_user ADD COLUMN IF NOT EXISTS password_hash VARCHAR(200) DEFAULT '';
-- 多渠道认证改造：phone 和 password_hash 改为可为空
ALTER TABLE mh_user ALTER COLUMN phone DROP NOT NULL;
ALTER TABLE mh_user ALTER COLUMN password_hash DROP NOT NULL;
-- 用户类型字段（新部署跳过，建表语句已包含）
ALTER TABLE mh_user ADD COLUMN IF NOT EXISTS user_type VARCHAR(2) NOT NULL DEFAULT '02';

-- ============================================================================
-- 2. 猫咪点赞表
-- ============================================================================
CREATE TABLE IF NOT EXISTS mh_cat_like (
    id          BIGSERIAL    PRIMARY KEY,
    cat_id      BIGINT       NOT NULL,
    user_id     BIGINT       NOT NULL,
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(cat_id, user_id)
);
COMMENT ON TABLE mh_cat_like IS '猫咪点赞表 - 用户对猫咪的点赞记录';
COMMENT ON COLUMN mh_cat_like.id IS '主键 ID';
COMMENT ON COLUMN mh_cat_like.cat_id IS '关联猫咪 ID，关联 mh_cats(id)';
COMMENT ON COLUMN mh_cat_like.user_id IS '点赞用户 ID，关联 mh_user(id)';
COMMENT ON COLUMN mh_cat_like.created_at IS '点赞时间';

ALTER TABLE mh_cat ADD COLUMN IF NOT EXISTS like_count INT DEFAULT 0;
COMMENT ON COLUMN mh_cat.like_count IS '猫咪被点赞总数，冗余字段用于快速排序';

-- ============================================================================
-- 3. 评论表（支持嵌套回复）
-- ============================================================================
CREATE TABLE IF NOT EXISTS mh_cat_comment (
    id          BIGSERIAL    PRIMARY KEY,
    cat_id      BIGINT       NOT NULL,
    tenant_id   BIGINT       NOT NULL,
    user_id     BIGINT       NOT NULL,
    parent_id   BIGINT,
    content     TEXT         NOT NULL,
    like_count  INT          DEFAULT 0,
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE mh_cat_comment IS '猫咪评论表 - 支持嵌套回复的评论';
COMMENT ON COLUMN mh_cat_comment.id IS '主键 ID';
COMMENT ON COLUMN mh_cat_comment.cat_id IS '关联猫咪 ID，关联 mh_cats(id)';
COMMENT ON COLUMN mh_cat_comment.tenant_id IS '所属小区 ID，关联 mh_tenants(id)';
COMMENT ON COLUMN mh_cat_comment.user_id IS '评论用户 ID，关联 mh_user(id)';
COMMENT ON COLUMN mh_cat_comment.parent_id IS '父评论 ID，关联 mh_cat_comment(id)，null 表示一级评论';
COMMENT ON COLUMN mh_cat_comment.content IS '评论内容';
COMMENT ON COLUMN mh_cat_comment.like_count IS '评论被点赞总数';
COMMENT ON COLUMN mh_cat_comment.created_at IS '评论创建时间';
COMMENT ON COLUMN mh_cat_comment.updated_at IS '评论最后更新时间';

-- ============================================================================
-- 4. 评论点赞表
-- ============================================================================
CREATE TABLE IF NOT EXISTS mh_cat_comment_like (
    id          BIGSERIAL    PRIMARY KEY,
    comment_id  BIGINT       NOT NULL,
    user_id     BIGINT       NOT NULL,
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(comment_id, user_id)
);
COMMENT ON TABLE mh_cat_comment_like IS '评论点赞表 - 用户对评论的点赞记录';
COMMENT ON COLUMN mh_cat_comment_like.id IS '主键 ID';
COMMENT ON COLUMN mh_cat_comment_like.comment_id IS '关联评论 ID，关联 mh_cat_comment(id)';
COMMENT ON COLUMN mh_cat_comment_like.user_id IS '点赞用户 ID，关联 mh_user(id)';
COMMENT ON COLUMN mh_cat_comment_like.created_at IS '点赞时间';

-- 索引
CREATE INDEX IF NOT EXISTS idx_mh_cat_like_cat_id ON mh_cat_like(cat_id);
CREATE INDEX IF NOT EXISTS idx_mh_cat_comment_cat_id ON mh_cat_comment(cat_id);
CREATE INDEX IF NOT EXISTS idx_mh_cat_comment_parent_id ON mh_cat_comment(parent_id);
CREATE INDEX IF NOT EXISTS idx_mh_cat_comment_created_at ON mh_cat_comment(cat_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_mh_cat_comment_like_count ON mh_cat_comment(cat_id, like_count DESC);
CREATE INDEX IF NOT EXISTS idx_mh_cat_comment_like_comment_id ON mh_cat_comment_like(comment_id);

-- ============================================================================
-- 5. 用户多渠道认证表
-- ============================================================================
CREATE TABLE IF NOT EXISTS mh_user_auth_provider (
    id            BIGSERIAL    PRIMARY KEY,
    user_id       BIGINT       NOT NULL,
    provider      VARCHAR(30)  NOT NULL,
    provider_key  VARCHAR(200) NOT NULL,
    credential    VARCHAR(500),
    created_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(provider, provider_key)
);
COMMENT ON TABLE mh_user_auth_provider IS '用户多渠道认证表 - 支持手机号、微信等多种登录方式';
COMMENT ON COLUMN mh_user_auth_provider.id IS '主键 ID';
COMMENT ON COLUMN mh_user_auth_provider.user_id IS '关联用户 ID，关联 mh_user(id)';
COMMENT ON COLUMN mh_user_auth_provider.provider IS '认证渠道，如 phone / wechat_miniapp';
COMMENT ON COLUMN mh_user_auth_provider.provider_key IS '渠道唯一标识：手机号 / 微信 openid';
COMMENT ON COLUMN mh_user_auth_provider.credential IS '渠道凭据：BCrypt 密码哈希 / 微信 unionid';
COMMENT ON COLUMN mh_user_auth_provider.created_at IS '绑定时间';

CREATE INDEX IF NOT EXISTS idx_auth_providers_user_id ON mh_user_auth_provider(user_id);
