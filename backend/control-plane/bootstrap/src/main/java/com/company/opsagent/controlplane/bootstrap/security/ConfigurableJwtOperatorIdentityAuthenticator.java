package com.company.opsagent.controlplane.bootstrap.security;

import com.company.opsagent.controlplane.bootstrap.config.SecurityProperties;
import com.company.opsagent.controlplane.modules.identity.IdentityClaimsMapper;
import com.company.opsagent.controlplane.modules.identity.OperatorIdentity;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import reactor.core.publisher.Mono;

/**
 * 可配置的 JWT 操作人认证器。
 *
 * <p>该类统一处理两种场景：
 * 1. 本地开发使用共享密钥的 HS256 令牌；
 * 2. 接入真实企业身份系统时使用 OIDC/JWK 方式校验令牌。
 */
public class ConfigurableJwtOperatorIdentityAuthenticator implements OperatorIdentityAuthenticator {

  private static final String MODE_DEV_HS256 = "dev-hs256";
  private static final String MODE_OIDC = "oidc";

  private final SecurityProperties securityProperties;
  private final IdentityClaimsMapper identityClaimsMapper;
  private final ReactiveJwtDecoder oidcDecoder;

  public ConfigurableJwtOperatorIdentityAuthenticator(
      SecurityProperties securityProperties,
      IdentityClaimsMapper identityClaimsMapper) {
    this.securityProperties = securityProperties;
    this.identityClaimsMapper = identityClaimsMapper;
    this.oidcDecoder = createOidcDecoder(securityProperties);
  }

  /**
   * 根据当前认证模式选择认证路径。
   */
  @Override
  public Mono<OperatorIdentity> authenticate(String token) {
    return switch (mode()) {
      case MODE_OIDC -> authenticateWithOidc(token);
      case MODE_DEV_HS256 -> authenticateWithSharedSecret(token);
      default -> Mono.error(new IllegalStateException("unsupported auth mode: " + mode()));
    };
  }

  /**
   * 使用共享密钥模式校验令牌。
   *
   * <p>这里显式校验签名、issuer、audience 和过期时间，适合本地开发和自动化测试场景。
   */
  private Mono<OperatorIdentity> authenticateWithSharedSecret(String token) {
    try {
      SignedJWT signedJwt = SignedJWT.parse(token);
      if (!signedJwt.verify(new MACVerifier(securityProperties.sharedSecret()))) {
        return Mono.empty();
      }
      if (!securityProperties.issuer().equals(signedJwt.getJWTClaimsSet().getIssuer())) {
        return Mono.empty();
      }
      if (!signedJwt.getJWTClaimsSet().getAudience().contains(securityProperties.audience())) {
        return Mono.empty();
      }
      if (signedJwt.getJWTClaimsSet().getExpirationTime() == null
          || signedJwt.getJWTClaimsSet().getExpirationTime().toInstant().isBefore(Instant.now())) {
        return Mono.empty();
      }
      return Mono.just(identityClaimsMapper.fromClaims(
          signedJwt.getJWTClaimsSet().getSubject(),
          readUsernameClaim(signedJwt.getJWTClaimsSet().getClaims()),
          readRolesClaim(signedJwt.getJWTClaimsSet().getClaims())));
    } catch (ParseException | JOSEException exception) {
      return Mono.empty();
    }
  }

  /**
   * 使用 OIDC 解码器校验令牌。
   *
   * <p>一旦解码器构建成功，签名、issuer、audience 等校验交给 Spring Security 的 JWT 校验器完成。
   */
  private Mono<OperatorIdentity> authenticateWithOidc(String token) {
    if (oidcDecoder == null) {
      return Mono.error(new IllegalStateException("OIDC mode requires issuer-uri or jwk-set-uri"));
    }
    return oidcDecoder.decode(token)
        .map(jwt -> identityClaimsMapper.fromClaims(
            jwt.getSubject(),
            readUsernameClaim(jwt.getClaims()),
            readRolesClaim(jwt.getClaims())))
        .onErrorResume(exception -> Mono.empty());
  }

  /**
   * 构建 OIDC 解码器。
   *
   * <p>优先使用 `jwk-set-uri` 直接指定公钥地址；如果未配置，则尝试通过 `issuer-uri`
   * 进行 OIDC 发现。随后追加 audience 与 issuer 校验器。
   */
  private ReactiveJwtDecoder createOidcDecoder(SecurityProperties properties) {
    if (!MODE_OIDC.equals(mode())) {
      return null;
    }
    ReactiveJwtDecoder decoder;
    if (hasText(properties.jwkSetUri())) {
      decoder = NimbusReactiveJwtDecoder.withJwkSetUri(properties.jwkSetUri()).build();
    } else if (hasText(properties.issuerUri())) {
      decoder = ReactiveJwtDecoders.fromIssuerLocation(properties.issuerUri());
    } else {
      return null;
    }

    String expectedIssuer = hasText(properties.issuerUri()) ? properties.issuerUri() : properties.issuer();
    OAuth2TokenValidator<Jwt> audienceValidator = jwt -> jwt.getAudience().contains(properties.audience())
        ? OAuth2TokenValidatorResult.success()
        : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "required audience missing", null));
    if (decoder instanceof NimbusReactiveJwtDecoder nimbusDecoder && hasText(expectedIssuer)) {
      OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators.createDefaultWithIssuer(expectedIssuer);
      nimbusDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(issuerValidator, audienceValidator));
    } else if (decoder instanceof NimbusReactiveJwtDecoder nimbusDecoderWithoutIssuer) {
      nimbusDecoderWithoutIssuer.setJwtValidator(audienceValidator);
    }
    return decoder;
  }

  /**
   * 返回当前生效的认证模式；未配置时回退到开发态模式。
   */
  private String mode() {
    return hasText(securityProperties.authMode()) ? securityProperties.authMode() : MODE_DEV_HS256;
  }

  /**
   * 判断字符串是否具备有效文本内容。
   */
  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }

  /**
   * 从 claim 集合中读取用户名。
   *
   * <p>如果未读到有效值，则由上层身份映射器回退到 `subject`。
   */
  private String readUsernameClaim(java.util.Map<String, Object> claims) {
    String usernameClaim = hasText(securityProperties.usernameClaim())
        ? securityProperties.usernameClaim()
        : "preferred_username";
    Object value = claims.get(usernameClaim);
    if (value instanceof String stringValue && !stringValue.isBlank()) {
      return stringValue;
    }
    return null;
  }

  /**
   * 从 claim 集合中读取角色列表。
   *
   * <p>支持两种常见格式：角色数组，或以空格/逗号分隔的角色字符串。
   */
  private List<String> readRolesClaim(java.util.Map<String, Object> claims) {
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
}
