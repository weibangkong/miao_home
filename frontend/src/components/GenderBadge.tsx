/**
 * 性别标识 — 纯文字 + 符号
 * ♂ 公 / ♀ 母 / ? 未知
 */

interface GenderBadgeProps {
  gender: string;
  /** @deprecated 保留兼容，不再使用 */
  iconOnly?: boolean;
}

const GENDER_MAP: Record<string, string> = {
  "公": "♂ 公",
  "母": "♀ 母",
};

const DEFAULT_LABEL = "? 未知";

export default function GenderBadge({ gender }: GenderBadgeProps) {
  return (
    <span className="gender-text">
      {GENDER_MAP[gender] || DEFAULT_LABEL}
    </span>
  );
}
