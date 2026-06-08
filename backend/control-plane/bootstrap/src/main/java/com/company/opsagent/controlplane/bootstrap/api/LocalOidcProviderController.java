package com.company.opsagent.controlplane.bootstrap.api;

import com.company.opsagent.controlplane.bootstrap.config.LocalOidcProviderProperties;
import com.company.opsagent.controlplane.bootstrap.security.localoidc.LocalOidcAuthorizationService;
import com.company.opsagent.controlplane.bootstrap.security.localoidc.LocalOidcAuthorizationService.AuthorizationCodeGrant;
import com.company.opsagent.controlplane.bootstrap.security.localoidc.LocalOidcKeyMaterial;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

/**
 * 仅用于本地联调的模拟 OIDC Provider 端点。
 */
@RestController
@ConditionalOnProperty(prefix = "ops-agent.local-oidc-provider", name = "enabled", havingValue = "true")
public class LocalOidcProviderController {

  private final LocalOidcProviderProperties properties;
  private final LocalOidcAuthorizationService authorizationService;
  private final LocalOidcKeyMaterial keyMaterial;

  public LocalOidcProviderController(
      LocalOidcProviderProperties properties,
      LocalOidcAuthorizationService authorizationService,
      LocalOidcKeyMaterial keyMaterial) {
    this.properties = properties;
    this.authorizationService = authorizationService;
    this.keyMaterial = keyMaterial;
  }

  @GetMapping({
      "/mock-oidc/.well-known/openid-configuration",
      "/.well-known/openid-configuration/mock-oidc",
      "/.well-known/oauth-authorization-server/mock-oidc"
  })
  public Map<String, Object> configuration() {
    return Map.of(
        "issuer", properties.issuer(),
        "authorization_endpoint", properties.issuer() + "/oauth2/authorize",
        "token_endpoint", properties.issuer() + "/oauth2/token",
        "jwks_uri", properties.issuer() + "/oauth2/jwks",
        "response_types_supported", List.of("code"),
        "subject_types_supported", List.of("public"),
        "grant_types_supported", List.of("authorization_code"),
        "id_token_signing_alg_values_supported", List.of("RS256"),
        "token_endpoint_auth_methods_supported", List.of("client_secret_basic", "client_secret_post"),
        "scopes_supported", List.of("openid", "profile", "email"));
  }

  @GetMapping("/mock-oidc/oauth2/jwks")
  public Map<String, Object> jwks() {
    return keyMaterial.jwkSet();
  }

  @GetMapping("/mock-oidc/oauth2/authorize")
  public Mono<ResponseEntity<Void>> authorize(
      @RequestParam("response_type") String responseType,
      @RequestParam("client_id") String clientId,
      @RequestParam("redirect_uri") String redirectUri,
      @RequestParam("state") String state,
      @RequestParam(name = "nonce", required = false) String nonce,
      @RequestParam(name = "username", required = false) String username,
      @RequestParam(name = "subject", required = false) String subject,
      @RequestParam(name = "role", required = false) List<String> roles) {
    if (!"code".equals(responseType)) {
      return Mono.error(new IllegalArgumentException("unsupported response_type"));
    }
    validateClientId(clientId);
    String code = authorizationService.issueCode(
        clientId,
        redirectUri,
        state,
        nonce,
        hasText(subject) ? subject : properties.defaultSubject(),
        hasText(username) ? username : properties.defaultUsername(),
        roles == null || roles.isEmpty() ? properties.defaultRoles() : roles);
    URI redirectLocation = UriComponentsBuilder.fromUriString(redirectUri)
        .queryParam("code", code)
        .queryParam("state", state)
        .build()
        .encode()
        .toUri();
    return Mono.just(ResponseEntity.status(HttpStatus.FOUND)
        .location(redirectLocation)
        .build());
  }

  @PostMapping(path = "/mock-oidc/oauth2/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public Mono<Map<String, Object>> token(ServerWebExchange exchange) {
    return exchange.getFormData().map(formData -> issueTokenResponse(exchange.getRequest().getHeaders(), formData));
  }

  private Map<String, Object> issueTokenResponse(HttpHeaders headers, MultiValueMap<String, String> formData) {
    String grantType = formData.getFirst("grant_type");
    if (!"authorization_code".equals(grantType)) {
      throw new IllegalArgumentException("unsupported grant_type");
    }

    ClientCredentials clientCredentials = extractClientCredentials(headers, formData);
    validateClientId(clientCredentials.clientId());
    if (!properties.clientSecret().equals(clientCredentials.clientSecret())) {
      throw new IllegalArgumentException("invalid client secret");
    }

    String code = required(formData, "code");
    String redirectUri = required(formData, "redirect_uri");
    AuthorizationCodeGrant grant = authorizationService.consumeCode(code, clientCredentials.clientId(), redirectUri);

    Instant issuedAt = Instant.now();
    Instant expiresAt = issuedAt.plus(properties.tokenTtl());
    String accessToken = keyMaterial.signToken(
        properties.issuer(),
        properties.audience(),
        grant.subject(),
        grant.username(),
        grant.roles(),
        issuedAt,
        expiresAt,
        null);
    String idToken = keyMaterial.signToken(
        properties.issuer(),
        clientCredentials.clientId(),
        grant.subject(),
        grant.username(),
        grant.roles(),
        issuedAt,
        expiresAt,
        grant.nonce());

    return Map.of(
        "access_token", accessToken,
        "id_token", idToken,
        "token_type", "Bearer",
        "expires_in", properties.tokenTtl().toSeconds(),
        "scope", "openid profile");
  }

  private void validateClientId(String clientId) {
    if (!properties.clientId().equals(clientId)) {
      throw new IllegalArgumentException("invalid client id");
    }
  }

  private ClientCredentials extractClientCredentials(HttpHeaders headers, MultiValueMap<String, String> formData) {
    String authorization = headers.getFirst(HttpHeaders.AUTHORIZATION);
    if (authorization != null && authorization.startsWith("Basic ")) {
      String encoded = authorization.substring("Basic ".length()).trim();
      String decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
      int separator = decoded.indexOf(':');
      if (separator < 0) {
        throw new IllegalArgumentException("invalid basic authorization header");
      }
      return new ClientCredentials(decoded.substring(0, separator), decoded.substring(separator + 1));
    }
    return new ClientCredentials(required(formData, "client_id"), required(formData, "client_secret"));
  }

  private String required(MultiValueMap<String, String> formData, String key) {
    String value = formData.getFirst(key);
    if (!hasText(value)) {
      throw new IllegalArgumentException("missing required parameter: " + key);
    }
    return value;
  }

  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }

  private record ClientCredentials(String clientId, String clientSecret) {
  }
}
