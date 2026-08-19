/**
 * 喵之家 · 前端配色基础模板
 *
 * 四种主颜色：
 *   primary   — #1C85E8  主色（按钮、链接、选中态、聚焦）
 *   accent    — #FF886F  强调色（危险操作、高亮标记）
 *   bg        — #F5F5F5  背景色（页面底色）
 *   secondary — #737373  注释/小字颜色（辅助文案、标签、描述）
 *
 * 使用方式：
 *   import { colors } from '@/theme/colors';
 *   <div style={{ color: colors.primary }}>...</div>
 *
 * 也可配合 Ant Design ConfigProvider theme.token 使用，见 theme/antd-theme.ts
 */

export const colors = {
  // === 四种主颜色 ===

  /** 主色 #1C85E8 — 按钮、链接、选中态、聚焦边框 */
  primary: '#1C85E8',

  /** 主色悬浮态 */
  primaryHover: '#1565B8',

  /** 主色浅底 — 用于选中/悬浮背景 */
  primaryLight: '#E8F2FD',

  /** 强调色 #FF886F — 危险操作、高亮标记 */
  accent: '#FF886F',

  /** 强调色悬浮态 */
  accentHover: '#E67A63',

  /** 强调色浅底 */
  accentLight: '#FFF2EF',

  /** 背景色 #F5F5F5 — 页面底色 */
  bg: '#F5F5F5',

  /** 注释/小字 #737373 — 辅助文案、标签、描述 */
  secondary: '#737373',

  // === 派生色 ===

  /** 正文颜色 */
  text: '#1A1A1A',

  /** 更浅的辅助文字（比 secondary 更轻） */
  textLight: '#9CA3AF',

  /** 白色背景 */
  white: '#FFFFFF',

  /** 卡片底色 */
  surface: '#FAFAFA',

  /** 边框色 */
  border: '#E5E7EB',

  /** 更轻的边框/分割线 */
  borderLight: '#F3F4F6',

  /** 成功绿 */
  success: '#34C759',
} as const;

export type ColorToken = keyof typeof colors;
