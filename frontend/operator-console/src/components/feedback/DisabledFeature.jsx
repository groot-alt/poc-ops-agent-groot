import { Button } from "../primitives/Button.jsx";

/**
 * @typedef {object} DisabledFeatureProps
 * @property {string} title
 * @property {string} reason
 * @property {string} [actionLabel]
 */

/**
 * @param {DisabledFeatureProps} props
 */
export function DisabledFeature({
  title,
  reason,
  actionLabel = "暂不可用",
}) {
  return (
    <section className="disabled-feature">
      <strong>{title}</strong>
      <p>{reason}</p>
      <Button disabled variant="secondary">
        {actionLabel}
      </Button>
    </section>
  );
}
