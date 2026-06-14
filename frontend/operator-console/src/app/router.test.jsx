import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it } from "vitest";
import { MemoryRouter } from "react-router-dom";

import App from "./App.jsx";
import { AppProviders } from "./providers.jsx";

/**
 * @param {string} path
 */
function renderAt(path) {
  return render(
    <AppProviders Router={MemoryRouter} routerProps={{ initialEntries: [path] }}>
      <App />
    </AppProviders>,
  );
}

describe("operator console routes", () => {
  it("shows only implemented protected-page navigation", () => {
    renderAt("/agent");

    expect(screen.getByRole("navigation", { name: "主导航" })).toBeVisible();
    expect(
      screen.getByRole("link", { name: "Agent 工作台" }),
    ).toBeInTheDocument();
    expect(
      screen.getByRole("link", { name: "Skill 注册中心" }),
    ).toBeInTheDocument();
    expect(
      screen.getByRole("link", { name: "SQL 工作台" }),
    ).toBeInTheDocument();
    expect(
      screen.queryByRole("link", { name: "审计记录" }),
    ).not.toBeInTheDocument();
    expect(screen.getByText("安全模式")).toBeInTheDocument();
    expect(screen.getByText("当前会话")).toBeInTheDocument();
  });

  it("navigates between implemented protected pages", async () => {
    const user = userEvent.setup();
    renderAt("/agent");

    await user.click(screen.getByRole("link", { name: "SQL 工作台" }));

    expect(
      screen.getByRole("heading", { name: "SQL 工作台" }),
    ).toBeInTheDocument();
  });

  it("redirects the root route to login", () => {
    renderAt("/");

    expect(screen.getByRole("heading", { name: "登录页" })).toBeInTheDocument();
  });
});
