/**
 * @typedef {object} FeedbackStateProps
 * @property {"loading" | "error" | "empty"} state
 * @property {string} title
 * @property {string} [message]
 */

/**
 * @param {FeedbackStateProps} props
 */
export function FeedbackState({ state, title, message }) {
  return (
    <section
      aria-label={title}
      aria-live={state === "error" ? "assertive" : "polite"}
      className="feedback-state"
      role={state === "error" ? "alert" : "status"}
    >
      <strong>{title}</strong>
      {message ? <p>{message}</p> : null}
    </section>
  );
}
