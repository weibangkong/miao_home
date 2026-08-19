/**
 * Ant Design 5 ConfigProvider theme token
 * 与 colors.ts 保持同步，确保 Ant Design 组件与自定义样式视觉一致
 *
 * 使用：在 main.tsx 的 ConfigProvider 中引入
 *   import { antdTheme } from '@/theme/antd-theme';
 *   <ConfigProvider theme={antdTheme}>...</ConfigProvider>
 */

import type { ThemeConfig } from 'antd';
import { colors } from './colors';

export const antdTheme: ThemeConfig = {
  token: {
    // 主色
    colorPrimary: colors.primary,
    colorPrimaryHover: colors.primaryHover,
    colorPrimaryBg: colors.primaryLight,

    // 错误/危险色 — 使用珊瑚强调色
    colorError: colors.accent,
    colorErrorHover: colors.accentHover,
    colorErrorBg: colors.accentLight,

    // 成功色
    colorSuccess: colors.success,

    // 文字
    colorText: colors.text,
    colorTextSecondary: colors.secondary,
    colorTextTertiary: colors.textLight,

    // 背景
    colorBgLayout: colors.bg,
    colorBgContainer: colors.white,

    // 边框
    colorBorder: colors.border,
    colorBorderSecondary: colors.borderLight,

    // 字号
    fontSize: 14,
    fontSizeSM: 12,
    fontSizeLG: 16,

    // 圆角
    borderRadius: 8,
    borderRadiusSM: 6,
    borderRadiusLG: 16,

    // 字体
    fontFamily: "'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif",
  },
  components: {
    Table: {
      headerBg: colors.bg,
      headerColor: colors.secondary,
      headerSplitColor: 'transparent',
      cellPaddingBlock: 10,
      cellPaddingInline: 16,
    },
    Card: {
      borderRadiusLG: 16,
    },
    Button: {
      borderRadius: 8,
      controlHeight: 36,
    },
    Input: {
      borderRadius: 8,
      controlHeight: 40,
    },
    Select: {
      borderRadius: 8,
      controlHeight: 40,
    },
  },
};
