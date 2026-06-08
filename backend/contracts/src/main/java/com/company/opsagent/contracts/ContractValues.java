package com.company.opsagent.contracts;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 跨模块契约值对象的统一边界校验工具。
 */
public final class ContractValues {

  private ContractValues() {
  }

  /**
   * 校验必填文本，避免无效标识进入跨模块信封。
   */
  public static String requiredText(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " must not be blank");
    }
    return value;
  }

  /**
   * 校验必填值。
   */
  public static <T> T required(T value, String fieldName) {
    return Objects.requireNonNull(value, fieldName + " must not be null");
  }

  /**
   * 防御性复制非空列表。
   */
  public static <T> List<T> requiredList(List<T> values, String fieldName) {
    List<T> copy = List.copyOf(required(values, fieldName));
    if (copy.isEmpty()) {
      throw new IllegalArgumentException(fieldName + " must not be empty");
    }
    return copy;
  }

  /**
   * 校验时间值。
   */
  public static OffsetDateTime requiredTime(OffsetDateTime value, String fieldName) {
    return required(value, fieldName);
  }
}
