package com.company.opsagent.controlplane.modules.identity.application;

import com.company.opsagent.controlplane.modules.identity.domain.PasswordCredential;
import com.company.opsagent.controlplane.modules.identity.infrastructure.PasswordHasher;
import com.company.opsagent.controlplane.modules.identity.infrastructure.PasswordVerifier;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * 基于 PBKDF2 的密码哈希与校验服务。
 */
public class Pbkdf2PasswordService implements PasswordHasher, PasswordVerifier {

  private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
  private static final int ITERATIONS = 310_000;
  private static final int KEY_LENGTH = 32;
  private static final int SALT_LENGTH = 16;

  @SuppressWarnings("unused")
  private final Clock clock;
  private final SecureRandom secureRandom;

  public Pbkdf2PasswordService(Clock clock) {
    this.clock = clock;
    this.secureRandom = new SecureRandom();
  }

  @Override
  public PasswordCredential hash(
      String accountId,
      String rawPassword,
      long passwordVersion,
      boolean mustChangeOnNextLogin) {
    byte[] salt = new byte[SALT_LENGTH];
    secureRandom.nextBytes(salt);
    String parameters = "i=" + ITERATIONS + ",l=" + KEY_LENGTH + ",s=" + Base64.getEncoder().encodeToString(salt);
    return new PasswordCredential(
        UUID.randomUUID().toString(),
        accountId,
        ALGORITHM,
        parameters,
        computeHash(rawPassword, salt, ITERATIONS, KEY_LENGTH),
        passwordVersion,
        mustChangeOnNextLogin);
  }

  @Override
  public boolean matches(String rawPassword, PasswordCredential credential) {
    Parameters parameters = Parameters.parse(credential.hashParameters());
    return computeHash(rawPassword, parameters.salt(), parameters.iterations(), parameters.keyLength())
        .equals(credential.passwordHash());
  }

  private String computeHash(String rawPassword, byte[] salt, int iterations, int keyLength) {
    try {
      PBEKeySpec spec = new PBEKeySpec(rawPassword.toCharArray(), salt, iterations, keyLength * 8);
      byte[] bytes = SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
      return Base64.getEncoder().encodeToString(bytes);
    } catch (GeneralSecurityException exception) {
      throw new IllegalStateException("failed to hash password", exception);
    }
  }

  private record Parameters(int iterations, int keyLength, byte[] salt) {

    private static Parameters parse(String parameters) {
      int iterations = 0;
      int keyLength = 0;
      byte[] salt = null;
      for (String part : parameters.split(",")) {
        String[] pair = part.split("=", 2);
        if (pair.length != 2) {
          continue;
        }
        switch (pair[0]) {
          case "i" -> iterations = Integer.parseInt(pair[1]);
          case "l" -> keyLength = Integer.parseInt(pair[1]);
          case "s" -> salt = Base64.getDecoder().decode(pair[1].getBytes(StandardCharsets.UTF_8));
          default -> {
          }
        }
      }
      if (iterations < 1 || keyLength < 1 || salt == null || salt.length == 0) {
        throw new IllegalArgumentException("invalid password hash parameters");
      }
      return new Parameters(iterations, keyLength, salt);
    }
  }
}
