/**
 * 阿里云 OSS 前端 SDK（ali-oss）最小类型声明
 * 仅声明本项目使用到的构造参数与 put 方法。
 */
declare module "ali-oss" {
  interface OSSOptions {
    /** OSS 地域，如 oss-cn-hangzhou */
    region?: string;
    /** 临时访问密钥 ID */
    accessKeyId?: string;
    /** 临时访问密钥 Secret */
    accessKeySecret?: string;
    /** STS 安全令牌 */
    stsToken?: string;
    /** 存储桶名称 */
    bucket?: string;
    /** OSS 服务端点 */
    endpoint?: string;
    /** 是否使用 HTTPS */
    secure?: boolean;
  }

  interface PutResult {
    /** 对象名 */
    name: string;
    /** 完整访问 URL */
    url: string;
    /** 响应状态码 */
    status?: number;
    /** 原始响应 */
    res?: unknown;
  }

  export default class OSS {
    constructor(options: OSSOptions);
    put(name: string, file: File | Blob): Promise<PutResult>;
  }
}
