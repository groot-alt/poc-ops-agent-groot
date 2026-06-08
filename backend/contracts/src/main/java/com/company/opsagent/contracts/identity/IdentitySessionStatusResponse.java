package com.company.opsagent.contracts.identity;

import static com.company.opsagent.contracts.ContractValues.requiredText;
import static com.company.opsagent.contracts.ContractValues.requiredTime;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 浏览器会话查询响应契约。
 */
public record IdentitySessionStatusResponse(
    boolean authenticated,
    String subject,
    String username,
    List<String> roles,
    String authenticationType,
    OffsetDateTime sessionExpiresAt,
    boolean passwordChangeRequired) {

  public IdentitySessionStatusResponse {
    roles = roles == null ? List.of() : List.copyOf(roles);
    authenticationType = requiredText(authenticationType, "authenticationType");
    if (authenticated) {
      subject = requiredText(subject, "subject");
      username = requiredText(username, "username");
      sessionExpiresAt = requiredTime(sessionExpiresAt, "sessionExpiresAt");
      if (roles.stream().anyMatch(role -> role == null || role.isBlank())) {
        throw new IllegalArgumentException("roles must not contain blank values");
      }
    } else if (subject != null || username != null || !roles.isEmpty() || sessionExpiresAt != null || passwordChangeRequired) {
      throw new IllegalArgumentException("anonymous session must not contain authenticated fields");
    }
  }
}
