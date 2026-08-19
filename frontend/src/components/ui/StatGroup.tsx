import type { ReactNode } from "react";

interface StatGroupProps {
  children: ReactNode;
}

/**
 * 横向排列的统计卡片组
 * 自动使用 CSS Grid 响应式布局
 */
export default function StatGroup({ children }: StatGroupProps) {
  return (
    <div
      style={{
        display: "grid",
        gridTemplateColumns: "repeat(auto-fit, minmax(180px, 1fr))",
        gap: 20,
        marginBottom: 24,
      }}
    >
      {children}
    </div>
  );
}
