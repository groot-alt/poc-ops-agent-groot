package com.company.opsagent.controlplane.bootstrap.security;

import com.company.opsagent.controlplane.modules.identity.IdentityClaimsMapper;
import com.company.opsagent.controlplane.modules.identity.OperatorIdentity;
import java.util.List;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

/**
 * JWT 到 Spring Security 认证对象的转换器。
 *
 * <p>当前主链路已经使用自定义过滤器直接处理令牌，这个转换器保留为后续接入标准
 * Resource Server 认证链时的兼容扩展点。
 */
public class OperatorAuthenticationConverter
    implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

  private final IdentityClaimsMapper identityClaimsMapper;

  public OperatorAuthenticationConverter(IdentityClaimsMapper identityClaimsMapper) {
    this.identityClaimsMapper = identityClaimsMapper;
  }

  /**
   * 把 JWT 转换成 Spring Security 可识别的认证对象。
   */
  @Override
  public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
    List<String> roles = jwt.getClaimAsStringList("roles");
    OperatorIdentity identity = identityClaimsMapper.fromClaims(
        jwt.getSubject(),
        jwt.getClaimAsString("preferred_username"),
        roles == null ? List.of() : roles);
    List<SimpleGrantedAuthority> authorities = identity.roles().stream()
        .map(SimpleGrantedAuthority::new)
        .toList();
    return Mono.just(new UsernamePasswordAuthenticationToken(identity, jwt.getTokenValue(), authorities));
  }
}
