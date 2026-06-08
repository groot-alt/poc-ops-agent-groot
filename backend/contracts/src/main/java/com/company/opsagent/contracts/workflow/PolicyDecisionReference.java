package com.company.opsagent.contracts.workflow;

import static com.company.opsagent.contracts.ContractValues.requiredText;

/**
 * 控制面服务端授权决策的不可变引用。
 */
public record PolicyDecisionReference(String decisionId, String policyVersion, String decision) {

  public PolicyDecisionReference {
    decisionId = requiredText(decisionId, "decisionId");
    policyVersion = requiredText(policyVersion, "policyVersion");
    if (!"ALLOW".equals(decision)) {
      throw new IllegalArgumentException("worker requests require an ALLOW decision");
    }
  }
}
