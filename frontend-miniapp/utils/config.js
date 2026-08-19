/**
 * 全局配置
 */

// 后端 API 基础地址（开发环境）
const BASE_URL = 'http://127.0.0.1:8080/miaohome/api';

// 文件上传访问基础地址
const UPLOAD_BASE = BASE_URL + '/files';

// 默认租户 ID
const DEFAULT_TENANT_ID = 1;

module.exports = {
  BASE_URL,
  UPLOAD_BASE,
  DEFAULT_TENANT_ID
};
