package com.company.opsagent.controlplane.modules.skillregistry;

/**
 * Skill 调用链路中必须启用的拦截器类型。
 *
 * <p>这里的拦截器是契约层声明，不等价于具体框架实现。注册中心通过它告诉控制面：
 * 某个 Skill 在真正运行前后必须经过哪些安全或治理环节。
 */
public enum SkillInterceptorType {
  /**
   * 权限校验。
   */
  AUTHORIZATION,

  /**
   * 审计留痕。
   */
  AUDIT,

  /**
   * 敏感信息脱敏。
   */
  SENSITIVE_DATA_MASKING,

  /**
   * 幂等、频控或配额保护。
   */
  RATE_LIMIT
}
