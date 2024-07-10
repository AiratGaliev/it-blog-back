package com.github.airatgaliev.itblogback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(name = "Get Category")
public class GetCategory {

  @Schema(description = "ID of the category", example = "1")
  private Long id;
  @Schema(description = "Name of the category", example = "Technology")
  private String name;
}
