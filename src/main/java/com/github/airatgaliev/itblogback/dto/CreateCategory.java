package com.github.airatgaliev.itblogback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Create Category")
public class CreateCategory {

  @Schema(description = "Name of the category", example = "Programming", requiredMode = RequiredMode.REQUIRED)
  private String name;
  @Schema(description = "Tag's image", requiredMode = RequiredMode.REQUIRED)
  private MultipartFile image;
  @Schema(description = "Description of the category", example = "Articles about technology")
  private String description;
}