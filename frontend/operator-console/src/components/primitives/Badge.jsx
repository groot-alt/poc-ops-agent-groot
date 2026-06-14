/**
 * @typedef {import("react").HTMLAttributes<HTMLSpanElement> & {
 *   tone?: "neutral" | "info" | "success" | "warning" | "danger"
 * }} BadgeProps
 */

/**
 * @param {BadgeProps} props
 */
export function Badge({ className = "", tone = "neutral", ...props }) {
  return (
    <span
      className={`badge badge--${tone} ${className}`.trim()}
      {...props}
    />
  );
}
