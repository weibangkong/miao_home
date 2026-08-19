/**
 * 状态指示器 — 纯文字 + 小圆点
 *
 * variant:  success → 实心深色点（已认养/已绝育）
 *           warning → 空心点（待认养）
 *           danger  → 红色实心点（异常/未读）
 *           info    → 实心深色点
 *           neutral → 空心点（未绝育/非活跃）
 */

interface BubbleTagProps {
  variant?: "success" | "warning" | "danger" | "info" | "neutral";
  /** @deprecated 保留兼容，不再使用 */
  dot?: boolean;
  children: React.ReactNode;
  style?: React.CSSProperties;
}

/**
 * variant → 点样式映射
 * success: 实心深色 → 肯定状态
 * warning: 空心 → 待定状态
 * danger:  红色实心 → 异常状态
 * info:    实心深色 → 一般标记
 * neutral: 空心 → 非活跃/否定状态
 */
const DOT_CLASS: Record<string, string> = {
  success: "active",
  warning: "inactive",
  danger: "danger",
  info: "active",
  neutral: "inactive",
};

export default function BubbleTag({ variant = "neutral", children, style }: BubbleTagProps) {
  return (
    <span className="status-dot" style={style}>
      <span className={`dot ${DOT_CLASS[variant]}`} />
      {children}
    </span>
  );
}
