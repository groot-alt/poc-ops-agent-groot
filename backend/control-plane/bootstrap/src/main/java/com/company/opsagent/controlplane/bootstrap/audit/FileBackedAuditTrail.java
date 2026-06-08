package com.company.opsagent.controlplane.bootstrap.audit;

import com.company.opsagent.controlplane.modules.audit.AuditEvent;
import com.company.opsagent.controlplane.modules.audit.AuditTrail;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.util.StringUtils;

/**
 * 基于本地文件的审计链实现。
 *
 * <p>当前采用追加写入 JSONL 文件的方式持久化审计事件，同时在内存中保留快照，便于
 * 接口快速读取最新记录和测试场景断言。
 */
public class FileBackedAuditTrail implements AuditTrail {

  private final Path storagePath;
  private final ObjectMapper objectMapper;
  private final CopyOnWriteArrayList<AuditEvent> events = new CopyOnWriteArrayList<>();
  private final Object lock = new Object();

  public FileBackedAuditTrail(Path storagePath, ObjectMapper objectMapper) {
    this.storagePath = storagePath;
    this.objectMapper = objectMapper;
    loadExisting();
  }

  /**
   * 追加记录一条审计事件，并同步写入本地 JSONL 文件。
   */
  @Override
  public void record(AuditEvent event) {
    synchronized (lock) {
      try {
        ensureParentDirectory();
        Files.writeString(
            storagePath,
            objectMapper.writeValueAsString(event) + System.lineSeparator(),
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND);
        events.add(event);
      } catch (IOException exception) {
        throw new IllegalStateException("failed to persist audit event", exception);
      }
    }
  }

  /**
   * 返回当前内存中的审计快照。
   */
  @Override
  public List<AuditEvent> snapshot() {
    return List.copyOf(events);
  }

  /**
   * 返回最近一条审计事件。
   */
  @Override
  public Optional<AuditEvent> latest() {
    if (events.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(events.get(events.size() - 1));
  }

  /**
   * 清空内存和文件中的审计数据。
   */
  @Override
  public void clear() {
    synchronized (lock) {
      events.clear();
      try {
        Files.deleteIfExists(storagePath);
      } catch (IOException exception) {
        throw new IllegalStateException("failed to clear audit storage", exception);
      }
    }
  }

  /**
   * 在启动时加载已有审计文件，恢复历史快照。
   */
  private void loadExisting() {
    if (!Files.exists(storagePath)) {
      return;
    }
    try {
      for (String line : Files.readAllLines(storagePath, StandardCharsets.UTF_8)) {
        if (!StringUtils.hasText(line)) {
          continue;
        }
        events.add(objectMapper.readValue(line, AuditEvent.class));
      }
    } catch (IOException exception) {
      throw new IllegalStateException("failed to load audit storage", exception);
    }
  }

  /**
   * 确保持久化目录存在。
   */
  private void ensureParentDirectory() throws IOException {
    Path parent = storagePath.getParent();
    if (parent != null) {
      Files.createDirectories(parent);
    }
  }
}
