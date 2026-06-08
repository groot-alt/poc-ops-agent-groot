package com.company.opsagent.controlplane.modules.identity;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

/**
 * 身份 Claim 标准化映射器。
 *
 * <p>负责把 JWT / OIDC 中的原始主体、用户名和角色信息整理成系统内部统一的
 * `OperatorIdentity`，并完成角色前缀补齐、去重和空值处理。
 */
public class IdentityClaimsMapper {

  /**
   * 把原始 claim 信息映射为系统内部身份对象。
   *
   * @param subject 令牌主体标识，不能为空
   * @param preferredUsername 用户名 claim，可为空，为空时回退到 subject
   * @param roles 原始角色集合，可包含重复项或未加前缀的角色
   * @return 标准化后的操作人身份
   */
  public OperatorIdentity fromClaims(
      String subject,
      String preferredUsername,
      Collection<String> roles) {
    String normalizedSubject = requireValue(subject, "subject");
    String normalizedUsername = isBlank(preferredUsername) ? normalizedSubject : preferredUsername.trim();
    LinkedHashSet<String> normalizedRoles = new LinkedHashSet<>();
    for (String role : roles) {
      if (isBlank(role)) {
        continue;
      }
      String trimmed = role.trim();
      normalizedRoles.add(trimmed.startsWith("ROLE_") ? trimmed : "ROLE_" + trimmed);
    }
    return new OperatorIdentity(normalizedSubject, normalizedUsername, List.copyOf(normalizedRoles));
  }

  /**
   * 校验并返回必填字段值。
   */
  private String requireValue(String value, String fieldName) {
    if (isBlank(value)) {
      throw new IllegalArgumentException(fieldName + " is required");
    }
    return value.trim();
  }

  /**
   * 判断字符串是否为空白。
   */
  private boolean isBlank(String value) {
    return Objects.isNull(value) || value.isBlank();
  }
}
