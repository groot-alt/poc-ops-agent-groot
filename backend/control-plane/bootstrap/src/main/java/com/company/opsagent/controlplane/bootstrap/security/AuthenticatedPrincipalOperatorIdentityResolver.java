package com.company.opsagent.controlplane.bootstrap.security;

import com.company.opsagent.controlplane.bootstrap.config.SecurityProperties;
import com.company.opsagent.controlplane.modules.identity.IdentityClaimsMapper;
import com.company.opsagent.controlplane.modules.identity.OperatorIdentity;
import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 已认证主体到内部操作人身份的解析器。
 *
 * <p>该解析器用于把浏览器 OIDC 登录后的会话主体，转换成系统内部统一的
 * {@link OperatorIdentity}，从而让会话登录和 Bearer Token 登录共享同一套授权与审计链路。
 */
public class AuthenticatedPrincipalOperatorIdentityResolver {

  private final SecurityProperties securityProperties;
  private final IdentityClaimsMapper identityClaimsMapper;

  public AuthenticatedPrincipalOperatorIdentityResolver(
      SecurityProperties securityProperties,
      IdentityClaimsMapper identityClaimsMapper) {
    this.securityProperties = securityProperties;
    this.identityClaimsMapper = identityClaimsMapper;
  }

  /**
   * 从当前请求上下文中解析已登录主体。
   *
   * @param exchange 当前 WebFlux 请求上下文
   * @return 成功时返回标准化后的操作人身份，否则返回空流
   */
  public Mono<OperatorIdentity> resolve(ServerWebExchange exchange) {
    return exchange.getPrincipal()
        .flatMap(this::resolve)
        .switchIfEmpty(ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .flatMap(this::resolve))
        .onErrorResume(IllegalArgumentException.class, exception -> Mono.empty());
  }

  /**
   * 从原始 Principal 解析内部身份。
   */
  public Mono<OperatorIdentity> resolve(Principal principal) {
    if (principal instanceof Authentication authentication && authentication.isAuthenticated()) {
      return resolve(authentication.getPrincipal(), authentication.getName());
    }
    return Mono.empty();
  }

  private Mono<OperatorIdentity> resolve(Object principal, String fallbackName) {
    if (principal instanceof OperatorIdentity identity) {
      return Mono.just(identity);
    }
    if (principal instanceof OidcUser oidcUser) {
      return Mono.just(fromClaims(oidcUser.getSubject(), oidcUser.getClaims(), fallbackName));
    }
    if (principal instanceof OAuth2AuthenticatedPrincipal oauthPrincipal) {
      return Mono.just(fromClaims(readSubject(oauthPrincipal, fallbackName), oauthPrincipal.getAttributes(), fallbackName));
    }
    return Mono.empty();
  }

  private OperatorIdentity fromClaims(
      String subject,
      Map<String, Object> claims,
      String fallbackName) {
    String resolvedSubject = hasText(subject) ? subject : fallbackName;
    return identityClaimsMapper.fromClaims(
        resolvedSubject,
        readUsernameClaim(claims),
        readRolesClaim(claims));
  }

  private String readSubject(OAuth2AuthenticatedPrincipal principal, String fallbackName) {
    Object subject = principal.getAttribute("sub");
    if (subject instanceof String subjectValue && !subjectValue.isBlank()) {
      return subjectValue;
    }
    return fallbackName;
  }

  private String readUsernameClaim(Map<String, Object> claims) {
    String usernameClaim = hasText(securityProperties.usernameClaim())
        ? securityProperties.usernameClaim()
        : "preferred_username";
    Object value = claims.get(usernameClaim);
    if (value instanceof String stringValue && !stringValue.isBlank()) {
      return stringValue;
    }
    return null;
  }

  private List<String> readRolesClaim(Map<String, Object> claims) {
    String roleClaim = hasText(securityProperties.roleClaim())
        ? securityProperties.roleClaim()
        : "roles";
    Object value = claims.get(roleClaim);
    if (value instanceof Collection<?> collection) {
      return collection.stream()
          .filter(Objects::nonNull)
          .map(Object::toString)
          .toList();
    }
    if (value instanceof String stringValue && !stringValue.isBlank()) {
      return java.util.Arrays.stream(stringValue.split("[,\\s]+"))
          .filter(part -> !part.isBlank())
          .toList();
    }
    return List.of();
  }

  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }
}
