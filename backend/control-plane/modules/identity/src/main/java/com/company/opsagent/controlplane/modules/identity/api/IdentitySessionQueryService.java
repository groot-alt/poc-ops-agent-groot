package com.company.opsagent.controlplane.modules.identity.api;

import com.company.opsagent.controlplane.modules.identity.OperatorIdentity;
import com.company.opsagent.controlplane.modules.identity.application.IdentitySessionStatus;
import java.util.Optional;

/**
 * 正式身份模块对外暴露的会话查询应用接口。
 */
public interface IdentitySessionQueryService {

  Optional<OperatorIdentity> findOperatorIdentityBySessionId(String sessionId);

  Optional<IdentitySessionStatus> findSessionStatusBySessionId(String sessionId);
}
