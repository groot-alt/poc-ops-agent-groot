# Skill

本目录保存仓库内置的 Skill 清单和样例实现占位。

当前阶段与 M03 的关系是：

- 每个 Skill 目录至少提供一个 `manifest.json`
- 每个 Skill 目录必须提供一个 `manifest.signature.json`
- 两份文件分别满足：
  - [skill-manifest.schema.json](C:/Users/Lenovo/Documents/ops-agent/backend/contracts/skills/skill-manifest.schema.json)
  - [skill-publication.schema.json](C:/Users/Lenovo/Documents/ops-agent/backend/contracts/skills/skill-publication.schema.json)
- 控制面启动时会扫描本目录，并在通过摘要与签名校验后注册到内存注册中心

## 每个 Skill 的必备内容

- 责任人和版本
- 分类和目标系统
- 风险等级和只读属性
- 执行器和超时
- 输入和输出 Schema
- 必要权限和治理拦截器
- 发布摘要和签名
- 审计字段和脱敏要求
- 正常、异常和授权测试

## P1 阶段约束

- 只允许登记只读诊断 Skill
- 不允许任何生产写操作
- 不允许绕过契约直接接入匿名脚本
