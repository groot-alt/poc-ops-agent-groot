package com.company.opsagent.controlplane.bootstrap.api;

import com.company.opsagent.contracts.identity.AdminResetPasswordRequest;
import com.company.opsagent.controlplane.bootstrap.config.SecurityProperties;
import com.company.opsagent.controlplane.modules.identity.api.IdentityAdministrationService;
import com.company.opsagent.controlplane.modules.identity.application.AdminResetPasswordCommand;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * 正式内建身份模式下的管理入口。
 */
@RestController
@RequestMapping("/internal/identity")
public class BuiltInIdentityAdministrationController {

  private final SecurityProperties securityProperties;
  private final IdentityAdministrationService identityAdministrationService;

  public BuiltInIdentityAdministrationController(
      SecurityProperties securityProperties,
      ObjectProvider<IdentityAdministrationService> identityAdministrationServiceProvider) {
    this.securityProperties = securityProperties;
    this.identityAdministrationService = identityAdministrationServiceProvider.getIfAvailable();
  }

  @PostMapping("/password-reset")
  public Mono<ResponseEntity<Void>> resetPassword(@RequestBody AdminResetPasswordRequest request) {
    if (!isBuiltInMode() || identityAdministrationService == null) {
      return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
    return Mono.fromRunnable(() -> identityAdministrationService.resetPassword(new AdminResetPasswordCommand(
            request.accountId(),
            request.reason(),
            request.temporaryPassword(),
            request.forcePasswordChange())))
        .subscribeOn(Schedulers.boundedElastic())
        .thenReturn(ResponseEntity.noContent().build());
  }

  private boolean isBuiltInMode() {
    return "built-in".equalsIgnoreCase(securityProperties.authMode());
  }
}
