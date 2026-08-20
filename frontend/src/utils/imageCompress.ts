import { encode as encodeJpeg } from "@jsquash/jpeg";
import { encode as encodeWebp } from "@jsquash/webp";

/**
 * 前端图片压缩工具
 * 解码走浏览器原生能力（Image + Canvas），编码走 WASM 编码器（MozJPEG / libwebp），
 * 兼顾压缩率、质量与体积。仅处理图片，其它类型（含视频、动图）原样返回。
 */

/** 默认长边像素上限，各调用点可通过 options.maxDimension 重写 */
export const DEFAULT_MAX_DIMENSION = 2048;

/** JPEG 有损压缩质量（0-100） */
const JPEG_QUALITY = 78;

/** WebP 有损压缩质量（0-100），用于 PNG（有损模式）与 WebP 输入的重编码 */
const WEBP_QUALITY = 80;

/** PNG 是否转无损 WebP（true=无损保真含透明，false=有损更小） */
const PNG_TO_LOSSLESS_WEBP = true;

/** 明确跳过压缩的格式（动图、矢量图、HEIC 等），原样上传 */
const SKIP_TYPES = new Set(["image/gif", "image/svg+xml", "image/heic", "image/heif"]);

/** 参与压缩的图片格式 */
const COMPRESS_TYPES = new Set(["image/jpeg", "image/png", "image/webp"]);

/** 可重写的压缩参数 */
export interface CompressOptions {
  /** 长边像素上限，默认 {@link DEFAULT_MAX_DIMENSION} */
  maxDimension?: number;
}

/**
 * 去掉扩展名的文件名
 */
function baseName(name: string): string {
  const dot = name.lastIndexOf(".");
  return dot > 0 ? name.slice(0, dot) : name;
}

/**
 * 用浏览器原生能力解码图片并等比缩放到长边上限内，返回像素数据。
 * 借助 Image 元素的 EXIF 方向修正能力，避免手机照片上传后方向错乱。
 */
function decodeToImageData(file: File, maxDimension: number): Promise<ImageData> {
  return new Promise((resolve, reject) => {
    const url = URL.createObjectURL(file);
    const img = new Image();
    img.onload = () => {
      URL.revokeObjectURL(url);
      const scale = Math.min(1, maxDimension / Math.max(img.naturalWidth, img.naturalHeight));
      const width = Math.max(1, Math.round(img.naturalWidth * scale));
      const height = Math.max(1, Math.round(img.naturalHeight * scale));
      const canvas = document.createElement("canvas");
      canvas.width = width;
      canvas.height = height;
      const ctx = canvas.getContext("2d", { willReadFrequently: true });
      if (!ctx) {
        reject(new Error("canvas 2d 上下文不可用"));
        return;
      }
      ctx.drawImage(img, 0, 0, width, height);
      resolve(ctx.getImageData(0, 0, width, height));
    };
    img.onerror = () => {
      URL.revokeObjectURL(url);
      reject(new Error("图片解码失败"));
    };
    img.src = url;
  });
}

/**
 * 压缩图片：JPEG→JPEG，PNG→WebP，WebP→WebP（重编码）。
 * 非图片、动图、HEIC 等直接原样返回；压缩失败或结果更大时回退原文件，不阻断上传。
 * @param file 待压缩文件
 * @param options 可选，maxDimension 重写长边上限
 * @returns 压缩后的文件（可能为原文件）
 */
export async function compressImage(file: File, options?: CompressOptions): Promise<File> {
  if (!COMPRESS_TYPES.has(file.type)) {
    return file;
  }

  const maxDimension = options?.maxDimension ?? DEFAULT_MAX_DIMENSION;

  try {
    const imageData = await decodeToImageData(file, maxDimension);

    let buffer: ArrayBuffer;
    let type: string;
    let extension: string;

    if (file.type === "image/jpeg") {
      buffer = await encodeJpeg(imageData, { quality: JPEG_QUALITY });
      type = "image/jpeg";
      extension = "jpg";
    } else {
      const isPng = file.type === "image/png";
      buffer = await encodeWebp(imageData, {
        lossless: isPng && PNG_TO_LOSSLESS_WEBP ? 1 : 0,
        quality: WEBP_QUALITY,
      });
      type = "image/webp";
      extension = "webp";
    }

    const result = new File([buffer], `${baseName(file.name)}.${extension}`, { type });

    // 压缩后反而更大（如已是优化过的小图）时，保留原文件
    return result.size < file.size ? result : file;
  } catch (e) {
    // 压缩失败不阻断上传，回退原文件
    console.warn("图片压缩失败，使用原文件上传", file.name, e);
    return file;
  }
}
