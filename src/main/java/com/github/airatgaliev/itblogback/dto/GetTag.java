package com.github.airatgaliev.itblogback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(name = "Get Tag")
public class GetTag {

  @Schema(description = "ID of the tag", example = "1")
  private Long id;
  @Schema(description = "Name of the tag", example = "vpn")
  private String name;
}
