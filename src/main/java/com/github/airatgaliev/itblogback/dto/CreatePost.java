package com.github.airatgaliev.itblogback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
@Schema(name = "Create Post")
public class CreatePost {

  @NotNull(message = "Title cannot be empty")
  @Schema(description = "Title of the post", example = "My First Blog Post", requiredMode = RequiredMode.REQUIRED)
  private String title;
  @NotNull(message = "Content cannot be empty")
  @Schema(description = "Content of the post", example = "This is the content of the post", requiredMode = RequiredMode.REQUIRED)
  private String content;
  @NotNull(message = "Content cannot be empty")
  @Schema(description = "Categories associated with the post")
  private List<Long> categoryIds;
}
