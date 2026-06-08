package com.company.opsagent.controlplane.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 控制面服务启动入口。
 *
 * <p>该类负责启动 Spring Boot WebFlux 应用，并扫描控制面相关组件、配置和模块定义。
 */
@SpringBootApplication(scanBasePackages = "com.company.opsagent.controlplane")
public class ControlPlaneApplication {

  /**
   * 启动控制面进程。
   *
   * @param args 启动参数，通常由运行脚本或 IDE 传入
   */
  public static void main(String[] args) {
    SpringApplication.run(ControlPlaneApplication.class, args);
  }
}
