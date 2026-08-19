-- ============================================================================
-- 喵之家 - 数据库用户创建与授权脚本
-- 数据库：PostgreSQL
-- 数据库名：miao_home
-- 执行方式：用 postgres 超级用户连接后执行本脚本
-- ============================================================================

-- ============================================================================
-- 1. 创建 miao_home 用户（仅 DML 权限，给生产环境应用使用）
-- ============================================================================
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'miao_home') THEN
        CREATE USER miao_home WITH PASSWORD 'miao_home_2024_DML';
    END IF;
END
$$;

-- 允许连接数据库
GRANT CONNECT ON DATABASE miao_home TO miao_home;

-- 允许使用 public schema
GRANT USAGE ON SCHEMA public TO miao_home;

-- 给已有的所有表授予 DML 权限
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO miao_home;

-- 给已有的所有序列授予使用权限（BIGSERIAL 自增主键需要）
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO miao_home;

-- 给未来新建的表和序列也自动授权
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO miao_home;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT USAGE, SELECT ON SEQUENCES TO miao_home;


-- ============================================================================
-- 2. 创建 miao_home_dev 用户（DML + DDL 权限，给开发环境应用使用）
--   - 可通过 Hibernate ddl-auto=update 自动建表/改表
--   - 可手动执行 CREATE / ALTER / DROP TABLE、INDEX、SEQUENCE
-- ============================================================================
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'miao_home_dev') THEN
        CREATE USER miao_home_dev WITH PASSWORD 'miao_home_dev_2024_DDL';
    END IF;
END
$$;

-- 允许连接数据库
GRANT CONNECT ON DATABASE miao_home TO miao_home_dev;

-- 将 public schema 的属主转移给 miao_home_dev，使其拥有 schema 的完全控制权
ALTER SCHEMA public OWNER TO miao_home_dev;

-- 给已有的所有表授予 DML 权限 + 转移属主（属主才可 ALTER / DROP）
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO miao_home_dev;
ALTER TABLE IF EXISTS mh_tenant            OWNER TO miao_home_dev;
ALTER TABLE IF EXISTS mh_cat              OWNER TO miao_home_dev;
ALTER TABLE IF EXISTS mh_cat_media          OWNER TO miao_home_dev;
ALTER TABLE IF EXISTS mh_adopter           OWNER TO miao_home_dev;
ALTER TABLE IF EXISTS mh_notification      OWNER TO miao_home_dev;
ALTER TABLE IF EXISTS mh_cat_location      OWNER TO miao_home_dev;
ALTER TABLE IF EXISTS mh_cat_health_record OWNER TO miao_home_dev;
ALTER TABLE IF EXISTS mh_user          OWNER TO miao_home_dev;
ALTER TABLE IF EXISTS mh_cat_like          OWNER TO miao_home_dev;
ALTER TABLE IF EXISTS mh_cat_comment       OWNER TO miao_home_dev;
ALTER TABLE IF EXISTS mh_cat_comment_like  OWNER TO miao_home_dev;
ALTER TABLE IF EXISTS mh_user_auth_provider OWNER TO miao_home_dev;

-- 给已有的所有序列转移属主（属主才可 ALTER / DROP）
ALTER SEQUENCE IF EXISTS mh_tenant_id_seq            OWNER TO miao_home_dev;
ALTER SEQUENCE IF EXISTS cats_id_seq               OWNER TO miao_home_dev;
ALTER SEQUENCE IF EXISTS mh_cat_media_id_seq          OWNER TO miao_home_dev;
ALTER SEQUENCE IF EXISTS mh_adopter_id_seq           OWNER TO miao_home_dev;
ALTER SEQUENCE IF EXISTS mh_notification_id_seq      OWNER TO miao_home_dev;
ALTER SEQUENCE IF EXISTS mh_cat_location_id_seq      OWNER TO miao_home_dev;
ALTER SEQUENCE IF EXISTS mh_cat_health_record_id_seq OWNER TO miao_home_dev;
ALTER SEQUENCE IF EXISTS mh_user_id_seq          OWNER TO miao_home_dev;

-- 给未来新建的表和序列也自动授权给 miao_home（生产只读用户）
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO miao_home;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT USAGE, SELECT ON SEQUENCES TO miao_home;


-- ============================================================================
-- 验证：查看已创建的用户权限
-- ============================================================================
SELECT
    grantee,
    privilege_type,
    table_schema,
    table_name
FROM information_schema.table_privileges
WHERE grantee IN ('miao_home', 'miao_home_dev')
ORDER BY grantee, table_name, privilege_type;
