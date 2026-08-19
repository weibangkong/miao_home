/**
 * iOS 风格 TabBar 图标生成器
 * 使用像素级绘制，生成简洁的 SF 风格图标
 * Run: node gen_icons.js
 */
const zlib = require('zlib');
const fs = require('fs');
const path = require('path');

const W = 81, H = 81;
const cx = 40, cy = 40;

// ====== PNG 基础设施 ======

function chunk(type, data) {
  const len = Buffer.alloc(4);
  len.writeUInt32BE(data.length, 0);
  const typeB = Buffer.from(type, 'ascii');
  const crcData = Buffer.concat([typeB, data]);
  const crcVal = crc32(crcData);
  const crc = Buffer.alloc(4);
  crc.writeUInt32BE(crcVal >>> 0, 0);
  return Buffer.concat([len, typeB, data, crc]);
}

function crc32(buf) {
  let crc = 0xffffffff;
  for (let i = 0; i < buf.length; i++) {
    crc ^= buf[i];
    for (let j = 0; j < 8; j++) {
      if (crc & 1) crc = (crc >>> 1) ^ 0xedb88320;
      else crc = crc >>> 1;
    }
  }
  return ~crc;
}

function writePNG(filename, pixels) {
  const rawRows = [];
  for (let y = 0; y < H; y++) {
    const row = [0]; // filter byte
    for (let x = 0; x < W; x++) {
      const i = (y * W + x) * 4;
      row.push(pixels[i], pixels[i + 1], pixels[i + 2], pixels[i + 3]);
    }
    rawRows.push(Buffer.from(row));
  }
  const raw = Buffer.concat(rawRows);
  const compressed = zlib.deflateSync(raw);

  const sig = Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]);
  const ihdrData = Buffer.alloc(13);
  ihdrData.writeUInt32BE(W, 0);
  ihdrData.writeUInt32BE(H, 4);
  ihdrData.writeUInt8(8, 8);
  ihdrData.writeUInt8(6, 9);   // RGBA
  ihdrData.writeUInt8(0, 10);
  ihdrData.writeUInt8(0, 11);
  ihdrData.writeUInt8(0, 12);

  const png = Buffer.concat([sig, chunk('IHDR', ihdrData), chunk('IDAT', compressed), chunk('IEND', Buffer.alloc(0))]);
  fs.writeFileSync(path.join(__dirname, filename), png);
  console.log(`Created ${filename} (${png.length} bytes)`);
}

// ====== 像素绘制函数 ======

function newCanvas() {
  const pixels = Buffer.alloc(W * H * 4, 0); // RGBA
  for (let i = 0; i < W * H; i++) {
    pixels[i * 4 + 3] = 0; // transparent by default
  }
  return pixels;
}

function setPixel(pixels, x, y, r, g, b, a = 1) {
  if (x < 0 || x >= W || y < 0 || y >= H) return;
  const i = (Math.round(y) * W + Math.round(x)) * 4;
  // alpha blend with existing
  const oldA = pixels[i + 3] / 255;
  const newA = a;
  const outA = newA + oldA * (1 - newA);
  if (outA > 0) {
    pixels[i] = Math.round((r * newA + pixels[i] * oldA * (1 - newA)) / outA);
    pixels[i + 1] = Math.round((g * newA + pixels[i + 1] * oldA * (1 - newA)) / outA);
    pixels[i + 2] = Math.round((b * newA + pixels[i + 2] * oldA * (1 - newA)) / outA);
    pixels[i + 3] = Math.round(outA * 255);
  }
}

function fillCircle(pixels, x, y, r, color) {
  for (let dy = -r - 2; dy <= r + 2; dy++) {
    for (let dx = -r - 2; dx <= r + 2; dx++) {
      const dist = Math.sqrt(dx * dx + dy * dy);
      if (dist < r - 0.5) {
        setPixel(pixels, x + dx, y + dy, ...color, 1);
      } else if (dist < r + 0.5) {
        const aa = Math.max(0, r + 0.5 - dist);
        setPixel(pixels, x + dx, y + dy, ...color, aa);
      }
    }
  }
}

function strokeCircle(pixels, x, y, r, color, width = 3) {
  for (let dy = -r - width; dy <= r + width; dy++) {
    for (let dx = -r - width; dx <= r + width; dx++) {
      const dist = Math.sqrt(dx * dx + dy * dy);
      const inner = r - width / 2;
      const outer = r + width / 2;
      if (dist >= inner && dist <= outer) {
        const aa = Math.min(1, Math.min(dist - (inner - 0.5), (outer + 0.5) - dist));
        setPixel(pixels, x + dx, y + dy, ...color, Math.max(0, aa));
      }
    }
  }
}

function fillRoundedRect(pixels, x, y, w, h, r, color) {
  const left = x - w / 2, right = x + w / 2;
  const top = y - h / 2, bottom = y + h / 2;

  for (let py = top - 1; py <= bottom + 1; py++) {
    for (let px = left - 1; px <= right + 1; px++) {
      // Find distance to the rounded rect
      let dx = 0, dy = 0;
      if (px < left + r) dx = left + r - px;
      else if (px > right - r) dx = px - (right - r);
      if (py < top + r) dy = top + r - py;
      else if (py > bottom - r) dy = py - (bottom - r);

      if (dx <= 0 && dy <= 0) {
        setPixel(pixels, px, py, ...color, 1);
      } else if (dx > 0 || dy > 0) {
        const dist = Math.sqrt(dx * dx + dy * dy);
        if (dist < r - 0.5) {
          setPixel(pixels, px, py, ...color, 1);
        } else if (dist < r + 0.5) {
          setPixel(pixels, px, py, ...color, Math.max(0, r + 0.5 - dist));
        }
      }
    }
  }
}

function fillRect(pixels, x, y, w, h, color) {
  for (let py = y - h / 2; py < y + h / 2; py++) {
    for (let px = x - w / 2; px < x + w / 2; px++) {
      setPixel(pixels, px, py, ...color, 1);
    }
  }
}

function strokeLine(pixels, x1, y1, x2, y2, color, width = 3) {
  const len = Math.sqrt((x2 - x1) ** 2 + (y2 - y1) ** 2);
  const steps = Math.ceil(len * 2);
  for (let i = 0; i <= steps; i++) {
    const t = i / steps;
    const px = x1 + (x2 - x1) * t;
    const py = y1 + (y2 - y1) * t;
    for (let d = -width / 2; d <= width / 2; d += 0.5) {
      const a = Math.max(0, 1 - Math.abs(d / (width / 2)));
      // perpendicular offset
      const angle = Math.atan2(y2 - y1, x2 - x1) + Math.PI / 2;
      const ox = Math.cos(angle) * d;
      const oy = Math.sin(angle) * d;
      setPixel(pixels, px + ox, py + oy, ...color, a);
    }
  }
}

function fillPath(pixels, points, color) {
  // Simple scanline fill for convex-ish shapes defined by outline points
  if (points.length < 3) return;

  let minY = Infinity, maxY = -Infinity;
  for (const p of points) { minY = Math.min(minY, p[1]); maxY = Math.max(maxY, p[1]); }

  for (let y = Math.floor(minY); y <= Math.ceil(maxY); y++) {
    const intersections = [];
    for (let i = 0; i < points.length; i++) {
      const j = (i + 1) % points.length;
      const p1 = points[i], p2 = points[j];
      if ((p1[1] <= y && p2[1] > y) || (p2[1] <= y && p1[1] > y)) {
        const t = (y - p1[1]) / (p2[1] - p1[1]);
        intersections.push(p1[0] + t * (p2[0] - p1[0]));
      }
    }
    intersections.sort((a, b) => a - b);
    for (let k = 0; k < intersections.length - 1; k += 2) {
      const x1 = Math.ceil(intersections[k]);
      const x2 = Math.floor(intersections[k + 1]);
      for (let x = x1; x <= x2; x++) {
        setPixel(pixels, x, y, ...color, 1);
      }
    }
  }
}

// ====== 图标绘制函数 ======

// 🐱 猫咪 — 猫脸轮廓（耳朵+圆脸+眼睛+鼻子）
function drawCat(pixels, color) {
  const [r, g, b] = color;
  const s = 1; // scale

  // 左耳三角形
  fillPath(pixels, [
    [cx - 20 * s, cy - 10 * s],
    [cx - 12 * s, cy - 33 * s],
    [cx - 3 * s, cy - 12 * s],
  ], color);

  // 右耳三角形
  fillPath(pixels, [
    [cx + 3 * s, cy - 12 * s],
    [cx + 12 * s, cy - 33 * s],
    [cx + 20 * s, cy - 10 * s],
  ], color);

  // 脸（圆）
  fillCircle(pixels, cx, cy + 2 * s, 22 * s, color);

  // 眼睛（白色挖空）
  fillCircle(pixels, cx - 9 * s, cy - 2 * s, 4 * s, [255, 255, 255]);
  fillCircle(pixels, cx + 9 * s, cy - 2 * s, 4 * s, [255, 255, 255]);

  // 瞳孔
  fillCircle(pixels, cx - 8 * s, cy - 2 * s, 2 * s, color);
  fillCircle(pixels, cx + 8 * s, cy - 2 * s, 2 * s, color);

  // 鼻子
  fillCircle(pixels, cx, cy + 8 * s, 2.5 * s, [255, 180, 180]);
}

// 📍 定位标记 — iOS 风格 map pin（圆形头+尖底）
function drawLocation(pixels, color) {
  const [r, g, b] = color;

  // 主体：圆形 + 下方三角尖
  // 上半圆
  fillCircle(pixels, cx, cy - 4, 17, color);

  // 下方三角（从圆的底部延伸到尖端）
  fillPath(pixels, [
    [cx - 13, cy - 4],
    [cx, cy + 28],
    [cx + 13, cy - 4],
  ], color);

  // 中心白色圆孔
  fillCircle(pixels, cx, cy - 6, 6, [255, 255, 255]);
}

// 🔔 通知 — iOS 风格铃铛
function drawBell(pixels, color) {
  const [r, g, b] = color;

  // 铃身主体：底部加宽的倒U形
  // 用圆角矩形+下半圆弧近似
  fillRoundedRect(pixels, cx, cy - 2, 26, 24, 10, color);

  // 顶部小圆+连线
  fillCircle(pixels, cx, cy - 20, 5, color);
  fillRect(pixels, cx, cy - 16, 5, 6, color);

  // 底部铃口横线（挖空效果）
  fillRect(pixels, cx, cy + 14, 16, 3, [255, 255, 255]);

  // 铃锤（底部小圆）
  fillCircle(pixels, cx, cy + 15, 5, color);
  fillCircle(pixels, cx, cy + 15, 2.5, [255, 255, 255]);

  // 右上角通知小圆点 (badge)
  fillCircle(pixels, cx + 18, cy - 20, 7, [255, 80, 80]);
}

// 👤 我的 — iOS 风格人物剪影
function drawPerson(pixels, color) {
  const [r, g, b] = color;

  // 头
  fillCircle(pixels, cx, cy - 15, 12, color);

  // 身体（半圆+矩形组合）
  // 肩膀弧线到身体
  fillPath(pixels, [
    [cx - 22, cy + 30],
    [cx - 24, cy + 5],
    [cx - 16, cy - 4],
    [cx + 16, cy - 4],
    [cx + 24, cy + 5],
    [cx + 22, cy + 30],
  ], color);
}

// ====== 生成图标 ======

function makeIcon(filename, drawFn, color) {
  const pixels = newCanvas();
  drawFn(pixels, color);
  writePNG(filename, pixels);
}

const activeColor   = [102, 126, 234]; // 品牌紫蓝 (selected)
const inactiveColor = [170, 170, 170]; // 浅灰 (unselected)

// 猫咪图标
makeIcon('tab-cat.png',         drawCat,      inactiveColor);
makeIcon('tab-cat-active.png',  drawCat,      activeColor);

// 定位图标
makeIcon('tab-range.png',        drawLocation, inactiveColor);
makeIcon('tab-range-active.png', drawLocation, activeColor);

// 铃铛图标
makeIcon('tab-notify.png',        drawBell,     inactiveColor);
makeIcon('tab-notify-active.png', drawBell,     activeColor);

// 人物图标
makeIcon('tab-mine.png',         drawPerson,   inactiveColor);
makeIcon('tab-mine-active.png',  drawPerson,   activeColor);

console.log('All iOS-style icons generated!');
