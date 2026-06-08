package com.company.opsagent.controlplane.bootstrap.security.localoidc;

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
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 本地模拟 OIDC Provider 运行时生成的 RSA 密钥材料。
 */
public final class LocalOidcKeyMaterial {

  private final RSAKey signingKey;

  private LocalOidcKeyMaterial(RSAKey signingKey) {
    this.signingKey = signingKey;
  }

  public static LocalOidcKeyMaterial generate() {
    try {
      return new LocalOidcKeyMaterial(new RSAKeyGenerator(2048)
          .keyID("local-oidc-" + UUID.randomUUID())
          .generate());
    } catch (JOSEException exception) {
      throw new IllegalStateException("failed to generate local OIDC signing key", exception);
    }
  }

  public Map<String, Object> jwkSet() {
    return new JWKSet(signingKey.toPublicJWK()).toJSONObject();
  }

  public String signToken(
      String issuer,
      String audience,
      String subject,
      String username,
      List<String> roles,
      Instant issuedAt,
      Instant expiresAt,
      String nonce) {
    try {
      JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder()
          .issuer(issuer)
          .audience(audience)
          .subject(subject)
          .issueTime(Date.from(issuedAt))
          .expirationTime(Date.from(expiresAt))
          .claim("preferred_username", username)
          .claim("roles", roles);
      if (nonce != null && !nonce.isBlank()) {
        claims.claim("nonce", nonce);
      }
      SignedJWT jwt = new SignedJWT(
          new JWSHeader.Builder(JWSAlgorithm.RS256)
              .keyID(signingKey.getKeyID())
              .type(JOSEObjectType.JWT)
              .build(),
          claims.build());
      jwt.sign(new RSASSASigner(signingKey.toPrivateKey()));
      return jwt.serialize();
    } catch (JOSEException exception) {
      throw new IllegalStateException("failed to sign local OIDC token", exception);
    }
  }
}
