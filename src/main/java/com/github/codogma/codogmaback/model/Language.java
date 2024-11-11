package com.github.codogma.codogmaback.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Language {
  EN("en"),
  RU("ru");

  private final String code;

  // Используем @JsonCreator для десериализации значения
  @JsonCreator
  public static Language fromCode(String code) {
    return Arrays.stream(values())
        .filter(lang -> lang.getCode().equalsIgnoreCase(code))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unsupported language code: " + code));
  }

  // Метод для проверки поддерживаемых значений
  public static boolean isSupported(String code) {
    return Arrays.stream(values()).anyMatch(lang -> lang.getCode().equals(code));
  }

  // Используем @JsonValue для сериализации значения
  @JsonValue
  public String getCode() {
    return code;
  }
}
