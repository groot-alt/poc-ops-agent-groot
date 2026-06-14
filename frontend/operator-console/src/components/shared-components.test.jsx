import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";

import { DisabledFeature } from "./feedback/DisabledFeature.jsx";
import { FeedbackState } from "./feedback/FeedbackState.jsx";
import { Badge } from "./primitives/Badge.jsx";
import { Button } from "./primitives/Button.jsx";
import { Card } from "./primitives/Card.jsx";

describe("shared primitives", () => {
  it("applies the button variant and forwards native button attributes", () => {
    render(
      <Button
        aria-describedby="deployment-note"
        className="deployment-action"
        data-testid="deployment-button"
        disabled
        name="deployment"
        variant="danger"
      >
        部署
      </Button>,
    );

    const button = screen.getByRole("button", { name: "部署" });
    expect(button).toBeDisabled();
    expect(button).toHaveAttribute("aria-describedby", "deployment-note");
    expect(button).toHaveAttribute("name", "deployment");
    expect(button).toHaveClass("deployment-action");
    expect(button.className).toContain("danger");
  });

  it("forwards native attributes and custom classes from card and badge", () => {
    render(
      <Card aria-labelledby="summary-title" className="summary-card" id="summary">
        <h2 id="summary-title">摘要</h2>
        <Badge className="readonly-badge" data-testid="risk-badge" tone="success">
          只读
        </Badge>
      </Card>,
    );

    expect(screen.getByRole("region", { name: "摘要" })).toHaveClass(
      "card",
      "summary-card",
    );
    expect(screen.getByTestId("risk-badge")).toHaveClass(
      "badge",
      "badge--success",
      "readonly-badge",
    );
  });
});

describe("shared feedback", () => {
  it("explains why a feature is disabled and renders a disabled action", () => {
    render(
      <DisabledFeature
        actionLabel="开始执行"
        reason="当前阶段仅允许只读诊断。"
        title="生产执行"
      />,
    );

    expect(screen.getByText("当前阶段仅允许只读诊断。")).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: "开始执行" }),
    ).toBeDisabled();
  });

  it("uses status semantics for non-error feedback", () => {
    render(<FeedbackState state="loading" title="正在加载" />);

    expect(screen.getByRole("status", { name: "正在加载" })).toHaveAttribute(
      "aria-live",
      "polite",
    );
  });

  it("uses alert semantics for error feedback", () => {
    render(
      <FeedbackState
        message="请稍后重试。"
        state="error"
        title="加载失败"
      />,
    );

    expect(screen.getByRole("alert", { name: "加载失败" })).toHaveAttribute(
      "aria-live",
      "assertive",
    );
  });
});
