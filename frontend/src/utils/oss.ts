import axios from "axios";
import OSS from "ali-oss";

/**
 * 阿里云 OSS 前端直传工具
 * 通过后端签发的 STS 临时凭证直接上传，避免文件流经后端。
 */

const http = axios.create({
  baseURL: "/miaohome/api",
  withCredentials: true,
});

interface StsCredentials {
  accessKeyId: string;
  accessKeySecret: string;
  securityToken: string;
  expiration: string;
  region: string;
  bucket: string;
  endpoint: string;
  dir: string;
}

interface StsResult {
  code: number;
  message: string;
  data: StsCredentials;
}

let cache: { credentials: StsCredentials; expireAt: number } | null = null;
let client: OSS | null = null;

function uuid(): string {
  return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0;
    const v = c === "x" ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}

function extOf(name: string): string {
  const i = name.lastIndexOf(".");
  return i >= 0 ? name.slice(i) : "";
}

/** 获取可复用的 OSS 客户端，凭证过期前 1 分钟自动刷新 */
async function ensureClient(): Promise<OSS> {
  const now = Date.now();
  if (cache && client && cache.expireAt > now + 60_000) {
    return client;
  }

  const res = await http.post<StsResult>("/files/sts/credentials");
  if (res.data.code !== 200 || !res.data.data) {
    throw new Error(res.data.message || "获取上传凭证失败");
  }

  const credentials = res.data.data;
  client = new OSS({
    region: credentials.region,
    accessKeyId: credentials.accessKeyId,
    accessKeySecret: credentials.accessKeySecret,
    stsToken: credentials.securityToken,
    bucket: credentials.bucket,
    endpoint: credentials.endpoint,
    secure: true,
  });
  cache = { credentials, expireAt: new Date(credentials.expiration).getTime() };
  return client;
}

/** 单文件上传大小上限（字节），50MB */
export const MAX_UPLOAD_BYTES = 50 * 1024 * 1024;

/**
 * 校验上传文件大小，超过上限抛出错误
 * @param file 待上传文件
 */
export function assertUploadSize(file: File): void {
  if (file.size > MAX_UPLOAD_BYTES) {
    throw new Error(`文件大小不能超过 ${MAX_UPLOAD_BYTES / 1024 / 1024}MB`);
  }
}

/**
 * 直传文件到 OSS
 * @param file 待上传文件
 * @returns 上传后的对象键（相对路径）
 */
export async function uploadToOss(file: File): Promise<string> {
  const ossClient = await ensureClient();
  const dir = cache!.credentials.dir;
  const key = `${dir}${uuid()}${extOf(file.name)}`;
  await ossClient.put(key, file);
  return key;
}

/**
 * 获取媒体访问 URL
 * <p>后端对私有读对象存储已返回完整签名 URL，此处直接使用；
 * 本地存储兜底时返回相对路径，拼后端 /files 代理访问。</p>
 * @param url 后端返回的 url（签名 URL 或相对路径）
 */
export function getMediaUrl(url: string): string {
  if (!url) return "";
  if (/^https?:\/\//i.test(url)) return url;
  return `/miaohome/api/files/${url}`;
}
