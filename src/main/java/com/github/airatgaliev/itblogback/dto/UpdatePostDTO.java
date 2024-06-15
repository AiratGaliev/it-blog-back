package com.github.airatgaliev.itblogback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "Update Post")
public class UpdatePostDTO {

  @Schema(description = "Title of the post", example = "My First Blog Post")
  private String title;
  @Schema(description = "Content of the post", example = "This is the content of the post")
  private String content;
}
