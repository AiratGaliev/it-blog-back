package com.github.airatgaliev.itblogback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(name = "Create Post")
public class CreatePostDTO {

  @NotNull(message = "Title cannot be empty")
  @Schema(description = "Title of the post", example = "My First Blog Post", requiredMode = RequiredMode.REQUIRED)
  private String title;
  @NotNull(message = "Content cannot be empty")
  @Schema(description = "Content of the post", example = "This is the content of the post", requiredMode = RequiredMode.REQUIRED)
  private String content;
  @NotNull(message = "Author ID cannot be empty")
  @Schema(description = "Author ID associated with the post", example = "1", requiredMode = RequiredMode.REQUIRED)
  private Long authorId;
}
