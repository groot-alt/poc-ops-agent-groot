/**
 * @typedef {import("react").HTMLAttributes<HTMLElement> & {
 *   ariaLabel?: string
 * }} CardProps
 */

/**
 * @param {CardProps} props
 */
export function Card({ ariaLabel, className = "", ...props }) {
  return (
    <section
      aria-label={ariaLabel}
      className={`card ${className}`.trim()}
      {...props}
    />
  );
}
