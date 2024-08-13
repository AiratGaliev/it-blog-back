package com.github.airatgaliev.itblogback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Data;

@Data
@Schema(name = "Create Tag")
public class CreateTag {

  @Schema(description = "Name of the tag", example = "vpn", requiredMode = RequiredMode.REQUIRED)
  private String name;
}
