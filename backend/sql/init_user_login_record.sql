-- ============================================================================
-- 喵之家 - 用户登录记录表
-- 数据库：PostgreSQL
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
