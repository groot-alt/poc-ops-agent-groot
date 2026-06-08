package com.company.opsagent.executionworker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 独立部署的受限执行 Worker 启动入口。
 */
@SpringBootApplication
public class ExecutionWorkerApplication {

  /**
   * 启动默认只绑定本机回环地址的开发 Worker。
   */
  public static void main(String[] args) {
    SpringApplication.run(ExecutionWorkerApplication.class, args);
  }
}
