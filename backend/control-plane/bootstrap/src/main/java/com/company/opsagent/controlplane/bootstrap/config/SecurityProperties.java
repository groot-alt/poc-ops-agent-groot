package com.company.opsagent.controlplane.bootstrap.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 认证与令牌处理配置属性。
 *
 * @param authMode 认证模式，当前支持开发态共享密钥模式和 OIDC 模式
 * @param issuer 期望的令牌签发方标识
 * @param audience 期望的受众标识
 * @param sharedSecret 开发态 HS256 模式使用的共享密钥
 * @param issuerUri OIDC 发现地址
 * @param jwkSetUri 直接指定的 JWK 集地址
 * @param usernameClaim 用于映射用户名的 claim 名称
 * @param roleClaim 用于映射角色列表的 claim 名称
 * @param rolePrefix 角色标准化时使用的前缀
 * @param browserLoginEnabled 是否启用标准 OIDC 浏览器登录流程
 * @param browserRegistrationId 浏览器登录使用的 OIDC Client Registration ID
 * @param browserLoginSuccessUri 浏览器登录成功后的本地跳转地址
 * @param browserLogoutSuccessUri 浏览器退出登录后的本地跳转地址
 */
@ConfigurationProperties(prefix = "ops-agent.security")
public record SecurityProperties(
    String authMode,
    String issuer,
    String audience,
    String sharedSecret,
    String issuerUri,
    String jwkSetUri,
    String usernameClaim,
    String roleClaim,
    String rolePrefix,
    boolean browserLoginEnabled,
    String browserRegistrationId,
    String browserLoginSuccessUri,
    String browserLogoutSuccessUri) {
}
