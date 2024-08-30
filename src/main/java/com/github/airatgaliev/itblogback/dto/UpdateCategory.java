package com.github.airatgaliev.itblogback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Update Category")
public class UpdateCategory {

  @Schema(description = "Name of the category", example = "Software Engineering")
  private String name;
  @Schema(description = "Tag's image")
  private MultipartFile image;
  @Schema(description = "Description of the category", example = "Articles about technology")
  private String description;
}