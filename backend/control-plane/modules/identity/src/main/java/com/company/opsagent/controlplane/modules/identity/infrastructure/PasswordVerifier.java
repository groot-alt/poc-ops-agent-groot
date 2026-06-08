package com.company.opsagent.controlplane.modules.identity.infrastructure;

import com.company.opsagent.controlplane.modules.identity.domain.PasswordCredential;

/**
 * 密码校验策略接口。
 */
public interface PasswordVerifier {

  boolean matches(String rawPassword, PasswordCredential credential);
}
