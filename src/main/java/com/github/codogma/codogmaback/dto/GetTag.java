package com.github.codogma.codogmaback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Get Tag")
public class GetTag {

  @Schema(description = "ID of the tag", example = "1")
  private Long id;
  @Schema(description = "Name of the tag", example = "vpn")
  private String name;
}
