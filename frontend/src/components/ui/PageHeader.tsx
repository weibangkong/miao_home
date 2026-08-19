import type { ReactNode } from "react";

interface PageHeaderProps {
  /** 页面标题 — 24px / 700 */
  title: ReactNode;
  /** 副标题 — 13px / #9CA3AF */
  subtitle?: string;
  /** 右侧操作区 */
  extra?: ReactNode;
}

/**
 * 页面标题区
 * 左对齐大标题 + 副标题，右侧操作区
 */
export default function PageHeader({ title, subtitle, extra }: PageHeaderProps) {
  return (
    <div
      style={{
        display: "flex",
        alignItems: "flex-start",
        justifyContent: "space-between",
        marginBottom: 24,
        flexWrap: "wrap",
        gap: 12,
      }}
    >
      <div>
        <h1
          style={{
            fontSize: 24,
            fontWeight: 700,
            color: "#1A1A1A",
            margin: 0,
            lineHeight: 1.3,
          }}
        >
          {title}
        </h1>
        {subtitle && (
          <p style={{ fontSize: 13, color: "#9CA3AF", margin: "4px 0 0" }}>
            {subtitle}
          </p>
        )}
      </div>
      {extra && <div style={{ display: "flex", alignItems: "center", gap: 12, flexWrap: "wrap" }}>{extra}</div>}
    </div>
  );
}
