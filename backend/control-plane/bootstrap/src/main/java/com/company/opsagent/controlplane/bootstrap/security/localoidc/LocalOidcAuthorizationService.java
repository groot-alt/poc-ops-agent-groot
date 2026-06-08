package com.company.opsagent.controlplane.bootstrap.security.localoidc;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 本地模拟 OIDC Provider 的一次性授权码服务。
 */
public class LocalOidcAuthorizationService {

  private final Duration authorizationCodeTtl;
  private final ConcurrentMap<String, AuthorizationCodeGrant> grants = new ConcurrentHashMap<>();

  public LocalOidcAuthorizationService(Duration authorizationCodeTtl) {
    this.authorizationCodeTtl = authorizationCodeTtl;
  }

  public String issueCode(
      String clientId,
      String redirectUri,
      String state,
      String nonce,
      String subject,
      String username,
      List<String> roles) {
    String code = UUID.randomUUID().toString();
    grants.put(code, new AuthorizationCodeGrant(
        code,
        clientId,
        redirectUri,
        state,
        nonce,
        subject,
        username,
        List.copyOf(roles),
        Instant.now().plus(authorizationCodeTtl)));
    return code;
  }

  public AuthorizationCodeGrant consumeCode(String code, String clientId, String redirectUri) {
    AuthorizationCodeGrant grant = grants.remove(code);
    if (grant == null) {
      throw new IllegalArgumentException("authorization code not found");
    }
    if (grant.expiresAt().isBefore(Instant.now())) {
      throw new IllegalArgumentException("authorization code expired");
    }
    if (!grant.clientId().equals(clientId)) {
      throw new IllegalArgumentException("client id does not match authorization code");
    }
    if (!grant.redirectUri().equals(redirectUri)) {
      throw new IllegalArgumentException("redirect uri does not match authorization code");
    }
    return grant;
  }

  /**
   * 授权码对应的受控登录上下文。
   */
  public record AuthorizationCodeGrant(
      String code,
      String clientId,
      String redirectUri,
      String state,
      String nonce,
      String subject,
      String username,
      List<String> roles,
      Instant expiresAt) {
  }
}
