/**
 * 数字展示组件 — 纯数字
 */

interface BubbleCountProps {
  count: number;
  /** @deprecated 保留兼容，不再使用 */
  color?: string;
  /** @deprecated 保留兼容，不再使用 */
  size?: number;
}

export default function BubbleCount({ count }: BubbleCountProps) {
  return <span>{count}</span>;
}
