# 控制面模块布局

控制面是模块化应用。代码应按业务模块组织，不采用全局 Controller、Service、Repository 技术分层。

计划中的内部模块：

```text
modules/
|-- identity/        # M01 身份认证
|-- policy/          # M02 策略授权
|-- audit/           # M02 审计
|-- skillregistry/   # M03 Skill 注册中心
|-- agentrouting/    # M04 Agent 路由
|-- workflow/        # M05 工作流
|-- orchestration/   # M06 编排
`-- events/          # M09 后端事件
```

每个模块应暴露精简的公共应用接口，并将领域和基础设施实现保持为内部细节。

建议的模块包结构：

```text
<module>/
|-- api/             # 入站 API 和模块公共契约
|-- application/     # 用例和编排
|-- domain/          # 业务规则和不可变值对象
`-- infrastructure/  # 持久化和外部适配器
```

规则：

- 模块只能通过其他模块的公共应用 API 或版本化异步契约进行调用。
- 模块不得访问其他模块的持久化适配器或数据表。
- 跨模块集成测试应放在消费方工作流中；构建结构 ADR 接受后，也可创建专用集成测试模块。

当前每个目录都应是标准 Maven 子模块，至少包含：

```text
<module>/
|-- pom.xml
`-- src/
    |-- main/java/
    `-- test/java/
```
