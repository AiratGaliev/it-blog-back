package com.github.codogma.codogmaback.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Language {
  EN("en"), RU("ru");

  private final String code;

  @JsonCreator
  public static Language fromCode(String code) {
    return Stream.of(values()).filter(lang -> lang != null && lang.getCode().equalsIgnoreCase(code))
        .findFirst().orElse(null);
  }

  @JsonValue
  public String getCode() {
    return code;
  }
}
