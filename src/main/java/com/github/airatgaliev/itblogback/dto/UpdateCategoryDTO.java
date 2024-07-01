package com.github.airatgaliev.itblogback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "Update Category")
public class UpdateCategoryDTO {

  @Schema(description = "Name of the category", example = "Software Engineering")
  private String name;
}