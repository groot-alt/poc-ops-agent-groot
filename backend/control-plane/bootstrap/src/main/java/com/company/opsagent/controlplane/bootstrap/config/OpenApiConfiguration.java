package com.company.opsagent.controlplane.bootstrap.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 文档配置。
 *
 * <p>该配置类把控制面基础属性映射到 OpenAPI 文档中，保证接口文档与服务元数据一致。
 */
@Configuration
public class OpenApiConfiguration {

  /**
   * 构建控制面 OpenAPI 文档对象。
   *
   * @param properties 控制面基础属性
   * @return 已填充标题、描述和版本的 OpenAPI 对象
   */
  @Bean
  OpenAPI controlPlaneOpenApi(ControlPlaneProperties properties) {
    return new OpenAPI().info(
        new Info()
            .title("Ops Agent Control Plane API")
            .description(properties.apiDescription())
            .version(properties.apiVersion()));
  }
}
