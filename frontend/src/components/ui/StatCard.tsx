import type { ReactNode } from "react";

interface StatCardProps {
  label: string;
  value: string | number;
  /** 左侧彩色圆角图标块中的图标 */
  icon?: ReactNode;
  /** 图标块配色 */
  tone?: "blue" | "orange" | "green";
  /** 主数值下方的补充说明（如认养卡片的待认养数） */
  secondary?: string;
}

/** tone → 图标块背景/前景色映射 */
const TONE_STYLE: Record<"blue" | "orange" | "green", { background: string; color: string }> = {
  blue: { background: "#E8F2FD", color: "#1C85E8" },
  orange: { background: "#FFF1EC", color: "#FF886F" },
  green: { background: "#E6F6EF", color: "#1BB981" },
};

/**
 * 统计数字卡片 — 左侧图标 + 右侧标签/大数字
 * 数字 22px / 700 / #1A1A1A
 * 标签 12px / 500 / #9CA3AF
 * 副行 12px / 500 / #737373
 */
export default function StatCard({ label, value, icon, tone = "blue", secondary }: StatCardProps) {
  const iconStyle = TONE_STYLE[tone];

  return (
    <div
      style={{
        background: "#FFFFFF",
        borderRadius: 16,
        padding: "20px 24px",
        boxShadow: "0 1px 3px rgba(0,0,0,0.04)",
        display: "flex",
        alignItems: "center",
        gap: 16,
      }}
    >
      {icon && (
        <div
          style={{
            width: 44,
            height: 44,
            borderRadius: 12,
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            fontSize: 20,
            flexShrink: 0,
            background: iconStyle.background,
            color: iconStyle.color,
          }}
        >
          {icon}
        </div>
      )}
      <div style={{ display: "flex", flexDirection: "column", gap: 4, minWidth: 0 }}>
        <div style={{ fontSize: 12, fontWeight: 500, color: "#9CA3AF" }}>
          {label}
        </div>
        <div style={{ fontSize: 22, fontWeight: 700, color: "#1A1A1A" }}>
          {typeof value === "number" ? value.toLocaleString() : value}
        </div>
        {secondary && (
          <div style={{ fontSize: 12, fontWeight: 500, color: "#737373" }}>
            {secondary}
          </div>
        )}
      </div>
    </div>
  );
}
