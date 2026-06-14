import { NavLink } from "react-router-dom";

import styles from "./AppShell.module.css";

const navigation = [
  { label: "Agent 工作台", to: "/agent" },
  { label: "Skill 注册中心", to: "/skills" },
  { label: "SQL 工作台", to: "/sql" },
];

/**
 * @typedef {object} AppShellProps
 * @property {import("react").ReactNode} children
 */

/**
 * @param {AppShellProps} props
 */
export function AppShell({ children }) {
  return (
    <div className={styles.shell}>
      <aside className={styles.sidebar}>
        <NavLink aria-label="企业智能 Agent 首页" className={styles.brand} to="/agent">
          <span aria-hidden="true" className={styles.logo}>
            EA
          </span>
          <span>
            <strong>企业智能 Agent</strong>
            <small>Operator Console</small>
          </span>
        </NavLink>

        <nav aria-label="主导航" className={styles.nav}>
          {navigation.map((item, index) => (
            <NavLink
              className={({ isActive }) =>
                `${styles.navLink} ${isActive ? styles.active : ""}`
              }
              key={item.to}
              to={item.to}
            >
              <span aria-hidden="true" className={styles.navIcon}>
                {String(index + 1).padStart(2, "0")}
              </span>
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div className={styles.sidebarFooter}>
          <div className={styles.safety}>
            <span aria-hidden="true" className={styles.safetyIndicator} />
            <span>
              <strong>安全模式</strong>
              <small>P1 只读控制面</small>
            </span>
          </div>
          <div aria-label="当前会话" className={styles.session}>
            <span aria-hidden="true" className={styles.avatar}>
              OP
            </span>
            <span>
              <strong>当前会话</strong>
              <small>会话信息将在登录后接入</small>
            </span>
          </div>
        </div>
      </aside>
      <main className={styles.content}>{children}</main>
    </div>
  );
}
