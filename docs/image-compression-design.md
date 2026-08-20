# 前端图片压缩设计文档

> 版本：v1.0　日期：2026-08-20　状态：已实现（HEIC 待补全）

## 1. 背景与目标

猫咪照片/头像走阿里云 OSS STS 前端直传（见 [oss-upload-and-image-serving-design.md](./oss-upload-and-image-serving-design.md)），文件不经后端。手机拍摄的原图动辄 3–10MB，直传慢、占带宽、存费高。

本次目标：**上传前在前端对图片做一次压缩**，在不明显损失画质的前提下显著缩小体积，同时保持零后端改动、零新增服务。

约束与取舍（需求方明确）：
- 压缩效率高、损失率小、格式支持多 → 用 WASM 编码器。
- 越轻量越好 → 编码器按格式拆包，按需引入；解码复用浏览器原生能力，不额外引入解码器。
- 视频不压缩；动图（GIF）不压缩。

## 2. 选型

### 2.1 对比

| 维度 | A. 原生 Canvas | B. @squoosh/lib | C. @jsquash 系列 | D. wasm-vips |
|---|---|---|---|---|
| 原理 | `canvas.toBlob` | WASM 编码器全家桶 | 与 Squoosh 同源，拆成独立小包 | libvips 编译到 WASM |
| 体积 | ~5–20KB，0 依赖 | 核心小，codec 按需加载（每 ~150KB–1MB） | 按需引入，单个 codec ~150–600KB | 数 MB |
| 输出格式 | JPEG/PNG/WebP，无 AVIF | JPEG/PNG/WebP/AVIF | 同左，按需装 | 最全 |
| 压缩率 / 损失 | 中 / 中 | 高 / 低 | 高 / 低 | 最高 / 最低 |
| 易用性 | 最简单 | 官方、API 友好 | API 略底层，需自己组合 | 重 |

### 2.2 结论

**采用 C（@jsquash 系列）**，且只引入实际用到的两个编码器：

- `@jsquash/jpeg`（MozJPEG 编码器）—— JPEG 重编码用。
- `@jsquash/webp`（libwebp 编码器）—— PNG/WebP 转 WebP 用。

理由：在「高质量多格式」与「轻量」之间平衡最好——编码质量与 Squoosh 同级，但按需只装两个 codec，解码完全复用浏览器原生能力，进一步压减体积。

## 3. 方案设计

### 3.1 格式映射

| 输入 MIME | 输出 | 编码器 | 说明 |
|---|---|---|---|
| `image/jpeg` | JPEG | MozJPEG | 有损 q=78，长边≤2048 |
| `image/png` | WebP | libwebp | 默认无损（保透明与细节），可切有损 |
| `image/webp` | WebP | libwebp | 有损 q=80 重编码，若结果更大则回退原文件 |
| `image/gif` | 原样 | — | 动图跳过（canvas/WASM 只取首帧） |
| `image/svg+xml` | 原样 | — | 矢量图不参与位图压缩 |
| `image/heic` / `image/heif` | 原样 | — | v1 跳过，见 §6 |
| 其它 | 原样 | — | 兜底不处理 |

### 3.2 处理流程

```
compressImage(file, options?)
  ├─ 非 COMPRESS_TYPES（视频 / GIF / HEIC / SVG / 其它）→ 原样返回
  ├─ 解码：Image + createObjectURL  → 浏览器原生解码 + EXIF 方向修正
  ├─ 缩放：长边 > maxDimension 时等比缩小（默认 2048）
  ├─ 取像素：canvas.getImageData
  ├─ 编码：JPEG→encodeJpeg(q=78)；PNG/WebP→encodeWebp
  ├─ 封装：new File(buffer, `${basename}.jpg|.webp`, { type })
  ├─ 若结果 ≥ 原文件 → 回退原文件
  └─ 任一步异常 → 回退原文件（fail-open，不阻断上传）
```

### 3.3 参数（集中在 `imageCompress.ts` 顶部常量）

| 常量 | 默认值 | 含义 |
|---|---|---|
| `DEFAULT_MAX_DIMENSION` | `2048` | 长边像素上限，可被各调用点重写 |
| `JPEG_QUALITY` | `78` | JPEG 有损质量 0–100 |
| `WEBP_QUALITY` | `80` | WebP 有损质量 0–100 |
| `PNG_TO_LOSSLESS_WEBP` | `true` | PNG 是否转无损 WebP |

## 4. 样例代码

### 4.1 核心工具 `frontend/src/utils/imageCompress.ts`

```ts
import { encode as encodeJpeg } from "@jsquash/jpeg";
import { encode as encodeWebp } from "@jsquash/webp";

export const DEFAULT_MAX_DIMENSION = 2048;
const JPEG_QUALITY = 78;
const WEBP_QUALITY = 80;
const PNG_TO_LOSSLESS_WEBP = true;
const SKIP_TYPES = new Set(["image/gif", "image/svg+xml", "image/heic", "image/heif"]);
const COMPRESS_TYPES = new Set(["image/jpeg", "image/png", "image/webp"]);

export interface CompressOptions {
  maxDimension?: number;
}

export async function compressImage(file: File, options?: CompressOptions): Promise<File> {
  if (!COMPRESS_TYPES.has(file.type)) return file;
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
    return result.size < file.size ? result : file;
  } catch (e) {
    console.warn("图片压缩失败，使用原文件上传", file.name, e);
    return file;
  }
}
```

### 4.2 接入上传链路 `frontend/src/api/index.ts`

```ts
import { compressImage } from "../utils/imageCompress";

export const uploadCatMedia = async (
  catId: number,
  file: File,
  ageStage?: string,
  isAvatar?: boolean,
  maxDimension?: number
) => {
  const compressed = await compressImage(file, maxDimension ? { maxDimension } : undefined);
  const objectKey = await uploadToOss(compressed);
  return confirmCatMediaObject(catId, {
    objectKey,
    fileName: compressed.name,
    fileSize: compressed.size,
    mediaType: compressed.type.startsWith("video") ? "VIDEO" : "PHOTO",
    ageStage,
    isAvatar: isAvatar ?? false,
  });
};

export const uploadUserAvatar = async (file: File, maxDimension?: number) => {
  const compressed = await compressImage(file, maxDimension ? { maxDimension } : undefined);
  const objectKey = await uploadToOss(compressed);
  return updateMyAvatar(objectKey);
};
```

> 说明：压缩在 `api` 层（而非 `uploadToOss` 内）完成，保证 `fileName / fileSize / mediaType` 与落库的对象键一致——不会出现「DB 记 `photo.jpg` 但 key 是 `.webp`」的错位。视频 `type` 非 `image/*`，`compressImage` 原样返回，逻辑不受影响。

## 5. 使用与重写指南

### 5.1 调整全局默认值

改 `frontend/src/utils/imageCompress.ts` 顶部常量即可，全站生效：

```ts
export const DEFAULT_MAX_DIMENSION = 2048; // 改长边上限
const JPEG_QUALITY = 78;                   // 改 JPEG 质量
const WEBP_QUALITY = 80;                   // 改 WebP 质量
const PNG_TO_LOSSLESS_WEBP = true;         // PNG 无损↔有损切换
```

### 5.2 单个调用点重写长边上限

上传函数的最后一个可选参数 `maxDimension` 即为重写口。示例：

```ts
// 头像更小，用 512
await uploadUserAvatar(file, 512);

// 详情页大图用 2560
await uploadCatMedia(catId, file, ageStage, isAvatar, 2560);

// 不传则用全局默认 2048
await uploadCatMedia(catId, file, ageStage, isAvatar);
```

组件层（`CatForm.tsx` / `CatDetail.tsx`）如需各自不同上限，在调用处传对应数值即可，无需改工具函数。

### 5.3 新增压缩格式

以「新增 AVIF 输出」为例：

1. `npm i @jsquash/avif`；
2. 在 `COMPRESS_TYPES` 中加目标输入类型（如 `image/avif`）；
3. 在 `compressImage` 的编码分支中加一个 `else if` 调 `encodeAvif`；
4. 若该格式有独立参数（如 AVIF 的 `cqLevel`），在顶部加对应常量，保持「常量集中、可重写」的约定。

### 5.4 切换 PNG 有损/无损

- 保真优先（截图、Logo、含透明精细图）：`PNG_TO_LOSSLESS_WEBP = true`（当前默认，仍比 PNG 小约 25–30%）。
- 体积优先（照片型 PNG）：改 `false`，走 `WEBP_QUALITY` 有损压缩，通常小 70%+。

## 6. HEIC 后续补全方案

### 6.1 现状与问题

iPhone 默认拍摄格式为 HEIC/HEIF。浏览器解码支持不一致：

| 浏览器 | HEIC 原生解码 |
|---|---|
| Safari（macOS/iOS） | ✅ 支持（`<img>` 可解码） |
| Chrome / Firefox / Edge | ❌ 不支持，`<img>` 加载失败 |

当前 v1 策略：HEIC 命中 `SKIP_TYPES` 原样上传。后果：Safari 可正常回显，Chrome 侧 `<img>` 加载失败显示裂图。

### 6.2 补全方案（推荐）

引入 `heic2any`（封装 libheif WASM），在 `compressImage` 前置一层转码：

```
HEIC 文件 → heic2any 转 JPEG/PNG Blob → 走现有 JPEG/WebP 压缩链路 → 上传
```

- 新增依赖：`heic2any`（约几十 KB + libheif WASM，建议按需动态 `import()` 懒加载，避免首屏体积）。
- 改动点：`SKIP_TYPES` 移除 HEIC/HEIF；`compressImage` 入口加 HEIC 分支，先 `heic2any` 转 `image/jpeg`，再复用 JPEG 分支。
- 待确认项：转码后是否保留原 HEIC 作为「原图」备用；转码质量参数（默认建议 JPEG q≈0.85，避免二次压缩叠加损失）。

### 6.3 备选方案

| 方案 | 优点 | 缺点 |
|---|---|---|
| `heic2any`（libheif WASM） | 纯前端、无服务端 | 体积增、转码耗时（单张 ~1–2s） |
| 后端转码（阿里云 IMM/自定义） | 前端零负担 | 引入服务端依赖与成本 |
| 引导用户拍 JPEG | 零成本 | 依赖用户行为，不可控 |

结论：优先 `heic2any` 前端转码，与当前「前端直传、零后端」架构一致。

## 7. 已知限制与注意事项

1. **主线程阻塞**：WASM 编码为 CPU 密集操作（单张约几十~几百 ms），当前在主线程执行。上传已有 loading 态，可接受；若后续批量/大图场景卡顿，可迁到 Web Worker。
2. **GIF 动图不压缩**：canvas/WASM 均只取首帧，压缩会丢动画，故原样上传。
3. **EXIF 方向**：解码走 `Image` 元素，浏览器已按 EXIF 方向校正，输出不会旋转错乱。
4. **结果更大即回退**：已优化的小图、以及无损 WebP 对极小的 PNG，可能不划算，统一回退原文件。
5. **WASM 加载**：`@jsquash/*` 生成的 glue code 兼容 Vite/Rollup（`import.meta.url` 定位 wasm），首次编码前会异步加载对应 wasm，无额外配置。
6. **WebP 兼容性**：输出 WebP 依赖现代浏览器，2026 年主流浏览器均已支持；如需兼容极老设备，可在 §3.1 将 PNG/WebP 统一改回 PNG/JPEG 输出。
