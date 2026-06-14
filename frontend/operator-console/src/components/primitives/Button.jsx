import styles from "./Button.module.css";

/**
 * @typedef {import("react").ButtonHTMLAttributes<HTMLButtonElement> & {
 *   variant?: "primary" | "secondary" | "danger"
 * }} ButtonProps
 */

/**
 * @param {ButtonProps} props
 */
export function Button({
  className = "",
  variant = "primary",
  type = "button",
  ...props
}) {
  return (
    <button
      className={`${styles.button} ${styles[variant]} ${className}`.trim()}
      type={type}
      {...props}
    />
  );
}
