package com.github.airatgaliev.itblogback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Data;

@Data
@Schema(name = "Create Category")
public class CreateCategory {

  @Schema(description = "Name of the category", example = "Programming", requiredMode = RequiredMode.REQUIRED)
  private String name;
}