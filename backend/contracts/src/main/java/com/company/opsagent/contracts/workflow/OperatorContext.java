package com.company.opsagent.contracts.workflow;

import static com.company.opsagent.contracts.ContractValues.requiredList;
import static com.company.opsagent.contracts.ContractValues.requiredText;

import java.util.List;

/**
 * 已认证操作人的不可变身份上下文。
 */
public record OperatorContext(String operatorId, List<String> roles) {

  public OperatorContext {
    operatorId = requiredText(operatorId, "operatorId");
    roles = requiredList(roles, "roles");
  }
}
