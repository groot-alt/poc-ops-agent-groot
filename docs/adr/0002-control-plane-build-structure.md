# ADR 0002：控制面 Maven 多模块构建结构

- 状态：Accepted
- 日期：2026-06-06
- 负责人：架构负责人
- 相关模块：M01-M06、M07、M09、M11
- 相关任务：T005、T007

## 背景

仓库已经确定采用 Maven 多模块构建，但 `backend/control-plane` 和 `backend/execution-worker` 最初只有说明目录，不是标准 Maven 工程。这会导致模块边界虽被规划，却无法通过构建结构稳定表达，也不利于后续按模块推进实现和测试。

## 决策

采用以下构建结构：

1. 根目录 `pom.xml` 作为仓库聚合父工程。
2. `backend/control-plane` 作为控制面聚合父模块。
3. 控制面下新增 `bootstrap` 模块，作为未来控制面部署单元入口。
4. 控制面业务模块按 `modules/<name>` 组织，每个业务模块都是独立 Maven 子模块。
5. `backend/execution-worker` 作为独立 Maven 模块。

## 结果结构

```text
backend/
|-- control-plane/
|   |-- pom.xml
|   |-- bootstrap/
|   `-- modules/
|       |-- identity/
|       |-- policy/
|       |-- audit/
|       |-- skillregistry/
|       |-- agentrouting/
|       |-- workflow/
|       |-- orchestration/
|       `-- events/
`-- execution-worker/
```

每个 Maven 模块都必须遵循标准目录：

```text
src/
|-- main/
|   `-- java/
`-- test/
    `-- java/
```

## 影响

- 模块边界可以通过依赖关系直接表达。
- 控制面仍是单一部署单元，但内部实现可以按模块独立演进。
- 后续 T007 可以在现有骨架上继续补充 Spring Boot 和实际业务代码，而不需要再次调整目录结构。
