package com.company.opsagent.controlplane.bootstrap.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 控制面基础配置装配入口。
 *
 * <p>当前主要负责把控制面自定义配置项注册为 Spring 可注入的配置对象。
 */
@Configuration
@EnableConfigurationProperties(ControlPlaneProperties.class)
public class ControlPlaneBootstrapConfiguration {
}
