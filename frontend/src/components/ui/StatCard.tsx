interface StatCardProps {
  label: string;
  value: string | number;
}

/**
 * 统计数字卡片 — 大数字 + 小标签
 * 数字 22px / 700 / #1A1A1A
 * 标签 12px / 500 / #9CA3AF
 */
export default function StatCard({ label, value }: StatCardProps) {
  return (
    <div
      style={{
        background: "#FFFFFF",
        borderRadius: 16,
        padding: "20px 24px",
        boxShadow: "0 1px 3px rgba(0,0,0,0.04)",
        display: "flex",
        flexDirection: "column",
        gap: 6,
      }}
    >
      <div style={{ fontSize: 12, fontWeight: 500, color: "#9CA3AF" }}>
        {label}
      </div>
      <div style={{ fontSize: 22, fontWeight: 700, color: "#1A1A1A" }}>
        {typeof value === "number" ? value.toLocaleString() : value}
      </div>
    </div>
  );
}
