package com.company.opsagent.controlplane.modules.skillregistry;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * 基于文件系统目录的 Skill Manifest 加载器。
 *
 * <p>当前实现用于 P1 阶段的本地只读 Skill 注册：
 * 每个 Skill 目录下放置 `manifest.json` 和 `manifest.signature.json`，
 * 注册中心启动时统一扫描、校验后再进入内存注册表。
 */
public class FileSystemSkillManifestLoader implements SkillManifestLoader {

  private static final TypeReference<SkillDescriptor> SKILL_DESCRIPTOR_TYPE = new TypeReference<>() {
  };
  private static final TypeReference<SkillPublicationMetadata> PUBLICATION_METADATA_TYPE = new TypeReference<>() {
  };

  private final Path rootPath;
  private final ObjectMapper objectMapper;
  private final boolean signatureRequired;
  private final String signingSecret;

  public FileSystemSkillManifestLoader(
      Path rootPath,
      ObjectMapper objectMapper,
      boolean signatureRequired,
      String signingSecret) {
    this.rootPath = rootPath;
    this.objectMapper = objectMapper;
    this.signatureRequired = signatureRequired;
    this.signingSecret = signingSecret;
  }

  /**
   * 扫描根目录下全部 `manifest.json` 并加载为 Skill 注册记录。
   */
  @Override
  public List<RegisteredSkill> loadAll() {
    Path resolvedRoot = resolveRootPath(rootPath);
    if (!Files.exists(resolvedRoot)) {
      throw new IllegalStateException("skill registry root path does not exist: " + resolvedRoot);
    }
    if (!Files.isDirectory(resolvedRoot)) {
      throw new IllegalStateException("skill registry root path is not a directory: " + resolvedRoot);
    }

    List<RegisteredSkill> registeredSkills = new ArrayList<>();
    Set<String> uniqueKeys = new LinkedHashSet<>();
    try (Stream<Path> stream = Files.walk(resolvedRoot)) {
      stream
          .filter(Files::isRegularFile)
          .filter(path -> path.getFileName().toString().equalsIgnoreCase("manifest.json"))
          .sorted(Comparator.comparing(Path::toString))
          .forEach(path -> {
            RegisteredSkill registeredSkill = loadRegisteredSkill(path, resolvedRoot);
            String uniqueKey = registeredSkill.descriptor().skillId() + ":" + registeredSkill.descriptor().version();
            if (!uniqueKeys.add(uniqueKey)) {
              throw invalid(path, "duplicate skill manifest detected: " + uniqueKey);
            }
            registeredSkills.add(registeredSkill);
          });
    } catch (IOException exception) {
      throw new UncheckedIOException("failed to scan skill manifest directory: " + resolvedRoot, exception);
    }
    return List.copyOf(registeredSkills);
  }

  /**
   * 返回解析后的 Skill 根目录绝对路径。
   */
  public Path resolvedRootPath() {
    return resolveRootPath(rootPath);
  }

  /**
   * 加载单个 Manifest，对外供显式发布校验动作复用。
   */
  public RegisteredSkill loadSingle(Path manifestPath) {
    Path resolvedRoot = resolvedRootPath();
    return loadRegisteredSkill(manifestPath.normalize(), resolvedRoot);
  }

  /**
   * 兼容从仓库根目录、backend 目录或子模块目录启动时的相对路径解析。
   */
  private Path resolveRootPath(Path configuredPath) {
    if (configuredPath.isAbsolute()) {
      return configuredPath.normalize();
    }

    Path current = Path.of("").toAbsolutePath().normalize();
    for (Path candidate = current; candidate != null; candidate = candidate.getParent()) {
      Path resolved = candidate.resolve(configuredPath).normalize();
      if (Files.exists(resolved)) {
        return resolved;
      }
    }
    return current.resolve(configuredPath).normalize();
  }

  /**
   * 读取单个 Manifest 文件。
   */
  private SkillDescriptor readDescriptor(Path path) {
    try {
      return objectMapper.readValue(path.toFile(), SKILL_DESCRIPTOR_TYPE);
    } catch (IOException exception) {
      throw new UncheckedIOException("failed to read skill manifest: " + path, exception);
    }
  }

  private RegisteredSkill loadRegisteredSkill(Path manifestPath, Path resolvedRoot) {
    byte[] manifestBytes = readManifestBytes(manifestPath);
    SkillPublicationMetadata publication = readPublicationMetadata(manifestPath);
    verifyPublicationMetadata(manifestPath, manifestBytes, publication);

    SkillDescriptor descriptor = readDescriptor(manifestPath);
    validateDescriptor(descriptor, manifestPath);

    return new RegisteredSkill(
        descriptor,
        publication,
        SkillPublicationStatus.VALIDATED,
        resolvedRoot.relativize(manifestPath).toString().replace('\\', '/'));
  }

  /**
   * 读取发布侧文件。
   */
  private SkillPublicationMetadata readPublicationMetadata(Path manifestPath) {
    Path publicationPath = manifestPath.resolveSibling("manifest.signature.json");
    if (!Files.exists(publicationPath)) {
      if (signatureRequired) {
        throw invalid(manifestPath, "manifest.signature.json is required");
      }
      return new SkillPublicationMetadata(
          "unknown",
          OffsetDateTime.MIN,
          "",
          "NONE",
          "");
    }
    try {
      return objectMapper.readValue(publicationPath.toFile(), PUBLICATION_METADATA_TYPE);
    } catch (IOException exception) {
      throw new UncheckedIOException("failed to read skill publication metadata: " + publicationPath, exception);
    }
  }

  private byte[] readManifestBytes(Path manifestPath) {
    try {
      return Files.readAllBytes(manifestPath);
    } catch (IOException exception) {
      throw new UncheckedIOException("failed to read manifest bytes: " + manifestPath, exception);
    }
  }

  /**
   * 对契约关键字段做最小但严格的启动期校验。
   */
  private void validateDescriptor(SkillDescriptor descriptor, Path path) {
    requireNotBlank(descriptor.skillId(), path, "skillId");
    requireNotBlank(descriptor.version(), path, "version");
    requireNotBlank(descriptor.displayName(), path, "displayName");
    requireNotBlank(descriptor.description(), path, "description");
    requireNotBlank(descriptor.owner(), path, "owner");

    if (descriptor.category() == null) {
      throw invalid(path, "category must be provided");
    }
    if (descriptor.riskLevel() == null) {
      throw invalid(path, "riskLevel must be provided");
    }
    if (descriptor.executor() == null) {
      throw invalid(path, "executor must be provided");
    }
    if (descriptor.outputType() == null) {
      throw invalid(path, "outputType must be provided");
    }
    if (descriptor.timeoutSeconds() <= 0) {
      throw invalid(path, "timeoutSeconds must be greater than 0");
    }
    if (!descriptor.readOnly()) {
      throw invalid(path, "P1 stage only allows readOnly skills");
    }
    if (descriptor.riskLevel() != SkillRiskLevel.READ_ONLY) {
      throw invalid(path, "P1 stage skill riskLevel must be READ_ONLY");
    }

    Set<String> parameterNames = new LinkedHashSet<>();
    for (SkillParameterDescriptor parameter : descriptor.parameters()) {
      requireNotBlank(parameter.name(), path, "parameter.name");
      requireNotBlank(parameter.displayName(), path, "parameter.displayName");
      requireNotBlank(parameter.description(), path, "parameter.description");
      if (parameter.type() == null) {
        throw invalid(path, "parameter.type must be provided");
      }
      String normalizedName = parameter.name().toLowerCase(Locale.ROOT);
      if (!parameterNames.add(normalizedName)) {
        throw invalid(path, "duplicate parameter name: " + parameter.name());
      }
    }
  }

  /**
   * 校验发布侧文件中的发布时间、摘要和值签名。
   */
  private void verifyPublicationMetadata(
      Path manifestPath,
      byte[] manifestBytes,
      SkillPublicationMetadata publication) {
    requireNotBlank(publication.publishedBy(), manifestPath, "publication.publishedBy");
    if (publication.publishedAt() == null || OffsetDateTime.MIN.equals(publication.publishedAt())) {
      throw invalid(manifestPath, "publication.publishedAt must be provided");
    }
    requireNotBlank(publication.checksumSha256(), manifestPath, "publication.checksumSha256");
    requireNotBlank(publication.signatureAlgorithm(), manifestPath, "publication.signatureAlgorithm");
    requireNotBlank(publication.signature(), manifestPath, "publication.signature");

    String actualChecksum = sha256Hex(manifestBytes);
    if (!actualChecksum.equalsIgnoreCase(publication.checksumSha256())) {
      throw invalid(manifestPath, "manifest checksum mismatch");
    }

    if (signatureRequired) {
      if (signingSecret == null || signingSecret.isBlank()) {
        throw invalid(manifestPath, "signing secret must be configured when signature verification is required");
      }
      if (!"HmacSHA256".equals(publication.signatureAlgorithm())) {
        throw invalid(manifestPath, "unsupported signature algorithm: " + publication.signatureAlgorithm());
      }
      String expectedSignature = hmacSha256Hex(signingSecret, publication.checksumSha256());
      if (!expectedSignature.equalsIgnoreCase(publication.signature())) {
        throw invalid(manifestPath, "manifest signature mismatch");
      }
    }
  }

  private void requireNotBlank(String value, Path path, String field) {
    if (value == null || value.isBlank()) {
      throw invalid(path, field + " must not be blank");
    }
  }

  private String sha256Hex(byte[] bytes) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return toHex(digest.digest(bytes));
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is not available", exception);
    }
  }

  private String hmacSha256Hex(String secret, String payload) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      return toHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception exception) {
      throw new IllegalStateException("failed to calculate HmacSHA256 signature", exception);
    }
  }

  private String toHex(byte[] bytes) {
    StringBuilder builder = new StringBuilder(bytes.length * 2);
    for (byte value : bytes) {
      builder.append(String.format("%02x", value));
    }
    return builder.toString();
  }

  private IllegalStateException invalid(Path path, String message) {
    return new IllegalStateException("invalid skill manifest at " + path + ": " + message);
  }
}
