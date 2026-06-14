/**
 * @typedef {object} PageHeaderProps
 * @property {string} title
 * @property {string} [description]
 * @property {import("react").ReactNode} [actions]
 */

/**
 * @param {PageHeaderProps} props
 */
export function PageHeader({ title, description, actions }) {
  return (
    <header>
      <div>
        <h1>{title}</h1>
        {description ? <p>{description}</p> : null}
      </div>
      {actions ? <div aria-label="页面操作">{actions}</div> : null}
    </header>
  );
}
