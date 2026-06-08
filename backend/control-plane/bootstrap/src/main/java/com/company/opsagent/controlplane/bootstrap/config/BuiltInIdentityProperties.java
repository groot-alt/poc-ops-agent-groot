package com.company.opsagent.controlplane.bootstrap.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 正式内建身份模式的运行参数。
 */
@ConfigurationProperties(prefix = "ops-agent.built-in-identity")
public class BuiltInIdentityProperties {

  private boolean schemaInitializerEnabled = true;
  private int lockoutThreshold = 5;
  private Duration lockoutDuration = Duration.ofMinutes(15);
  private Duration sessionIdleTimeout = Duration.ofMinutes(15);
  private Duration sessionAbsoluteTimeout = Duration.ofHours(8);
  private String sessionCookieName = "OPS_AGENT_SESSION";
  private boolean sessionCookieSecure = false;
  private String sessionCookieSameSite = "Lax";
  private Duration sessionCookieMaxAge = Duration.ofHours(8);

  public boolean isSchemaInitializerEnabled() {
    return schemaInitializerEnabled;
  }

  public void setSchemaInitializerEnabled(boolean schemaInitializerEnabled) {
    this.schemaInitializerEnabled = schemaInitializerEnabled;
  }

  public int getLockoutThreshold() {
    return lockoutThreshold;
  }

  public void setLockoutThreshold(int lockoutThreshold) {
    this.lockoutThreshold = lockoutThreshold;
  }

  public Duration getLockoutDuration() {
    return lockoutDuration;
  }

  public void setLockoutDuration(Duration lockoutDuration) {
    this.lockoutDuration = lockoutDuration;
  }

  public Duration getSessionIdleTimeout() {
    return sessionIdleTimeout;
  }

  public void setSessionIdleTimeout(Duration sessionIdleTimeout) {
    this.sessionIdleTimeout = sessionIdleTimeout;
  }

  public Duration getSessionAbsoluteTimeout() {
    return sessionAbsoluteTimeout;
  }

  public void setSessionAbsoluteTimeout(Duration sessionAbsoluteTimeout) {
    this.sessionAbsoluteTimeout = sessionAbsoluteTimeout;
  }

  public String getSessionCookieName() {
    return sessionCookieName;
  }

  public void setSessionCookieName(String sessionCookieName) {
    this.sessionCookieName = sessionCookieName;
  }

  public boolean isSessionCookieSecure() {
    return sessionCookieSecure;
  }

  public void setSessionCookieSecure(boolean sessionCookieSecure) {
    this.sessionCookieSecure = sessionCookieSecure;
  }

  public String getSessionCookieSameSite() {
    return sessionCookieSameSite;
  }

  public void setSessionCookieSameSite(String sessionCookieSameSite) {
    this.sessionCookieSameSite = sessionCookieSameSite;
  }

  public Duration getSessionCookieMaxAge() {
    return sessionCookieMaxAge;
  }

  public void setSessionCookieMaxAge(Duration sessionCookieMaxAge) {
    this.sessionCookieMaxAge = sessionCookieMaxAge;
  }
}
