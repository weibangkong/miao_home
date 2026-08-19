import type { CSSProperties, ReactNode } from "react";

interface CardProps {
  children: ReactNode;
  style?: CSSProperties;
  className?: string;
}

/**
 * 通用卡片组件
 * 16px 圆角，白色背景，极轻微阴影 — 匹配模板卡片规范
 */
export default function Card({ children, style, className }: CardProps) {
  return (
    <div
      className={className}
      style={{
        background: "#FFFFFF",
        borderRadius: 16,
        padding: 24,
        boxShadow: "0 1px 3px rgba(0,0,0,0.04)",
        ...style,
      }}
    >
      {children}
    </div>
  );
}
