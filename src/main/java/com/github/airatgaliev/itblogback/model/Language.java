package com.github.airatgaliev.itblogback.model;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Language {
  EN("en"), RU("ru");
  private final String code;

  public static boolean isSupported(String code) {
    return Arrays.stream(values()).anyMatch(lang -> lang.getCode().equals(code));
  }
}
