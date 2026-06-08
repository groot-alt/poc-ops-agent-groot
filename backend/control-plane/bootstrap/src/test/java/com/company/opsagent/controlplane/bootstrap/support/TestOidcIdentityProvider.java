package com.company.opsagent.controlplane.bootstrap.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * 本地模拟 OIDC 身份提供方。
 *
 * <p>该测试支撑类会在本地启动一个极简 HTTP 服务，暴露 OIDC 发现端点和 JWK 集端点，
 * 并用临时 RSA 私钥签发测试令牌。
 */
public final class TestOidcIdentityProvider implements AutoCloseable {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final HttpServer server;
  private final RSAKey signingKey;
  private final String issuer;

  private TestOidcIdentityProvider(
      HttpServer server,
      RSAKey signingKey,
      String issuer) {
    this.server = server;
    this.signingKey = signingKey;
      this.issuer = issuer;
  }

  /**
   * 启动测试 OIDC 提供方。
   */
  public static TestOidcIdentityProvider start() {
    try {
      HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
      server.setExecutor(Executors.newCachedThreadPool());
      RSAKey signingKey = new RSAKeyGenerator(2048).keyID("test-oidc-key").generate();
      String issuer = "http://127.0.0.1:" + server.getAddress().getPort() + "/realms/ops";
      TestOidcIdentityProvider provider = new TestOidcIdentityProvider(server, signingKey, issuer);
      provider.registerEndpoints();
      server.start();
      return provider;
    } catch (IOException | JOSEException exception) {
      throw new IllegalStateException("failed to start test OIDC provider", exception);
    }
  }

  /**
   * 返回当前测试服务的 issuer 地址。
   */
  public String issuer() {
    return issuer;
  }

  /**
   * 使用默认 issuer 签发测试令牌。
   */
  public String issueToken(
      String subject,
      String usernameClaim,
      String username,
      String roleClaim,
      Object roleValue,
      String audience) {
    return issueToken(subject, usernameClaim, username, roleClaim, roleValue, audience, issuer);
  }

  /**
   * 使用指定 issuer 签发测试令牌。
   *
   * <p>该重载主要用于构造 issuer 不匹配等负向场景。
   */
  public String issueToken(
      String subject,
      String usernameClaim,
      String username,
      String roleClaim,
      Object roleValue,
      String audience,
      String tokenIssuer) {
    try {
      JWTClaimsSet claims = new JWTClaimsSet.Builder()
          .subject(subject)
          .issuer(tokenIssuer)
          .audience(audience)
          .issueTime(Date.from(Instant.now()))
          .expirationTime(Date.from(Instant.now().plusSeconds(600)))
          .claim(usernameClaim, username)
          .claim(roleClaim, roleValue)
          .build();
      SignedJWT jwt = new SignedJWT(
          new JWSHeader.Builder(JWSAlgorithm.RS256)
              .keyID(signingKey.getKeyID())
              .type(JOSEObjectType.JWT)
              .build(),
          claims);
      jwt.sign(new RSASSASigner(signingKey.toPrivateKey()));
      return jwt.serialize();
    } catch (JOSEException exception) {
      throw new IllegalStateException("failed to issue test token", exception);
    }
  }

  /**
   * 关闭本地测试服务。
   */
  @Override
  public void close() {
    server.stop(0);
  }

  /**
   * 注册 OIDC 发现和 JWK 端点。
   */
  private void registerEndpoints() {
    Map<String, Object> metadata = Map.of(
        "issuer", issuer,
        "jwks_uri", issuer + "/jwks",
        "authorization_endpoint", issuer + "/authorize",
        "token_endpoint", issuer + "/token",
        "id_token_signing_alg_values_supported", List.of("RS256"),
        "subject_types_supported", List.of("public"),
        "response_types_supported", List.of("token"));
    registerJson("/.well-known/openid-configuration/realms/ops", metadata);
    registerJson("/realms/ops/.well-known/openid-configuration", metadata);
    registerJson("/.well-known/oauth-authorization-server/realms/ops", metadata);
    registerJson(
        "/realms/ops/jwks",
        new JWKSet(signingKey.toPublicJWK()).toJSONObject());
  }

  /**
   * 注册返回 JSON 的测试端点。
   */
  private void registerJson(String path, Object payload) {
    server.createContext(path, exchange -> writeJson(exchange, payload));
  }

  /**
   * 把对象序列化并写回测试 HTTP 响应。
   */
  private void writeJson(HttpExchange exchange, Object payload) throws IOException {
    byte[] response = OBJECT_MAPPER.writeValueAsString(payload).getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().add("Content-Type", "application/json");
    exchange.sendResponseHeaders(200, response.length);
    try (var outputStream = exchange.getResponseBody()) {
      outputStream.write(response);
    }
  }
}
