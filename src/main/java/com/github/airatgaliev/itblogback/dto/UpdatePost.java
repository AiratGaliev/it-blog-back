package com.github.airatgaliev.itblogback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
@Schema(name = "Update Post")
public class UpdatePost {

  @NotNull(message = "Title cannot be empty")
  @Schema(description = "Title of the post", example = "My First Blog Post")
  private String title;
  @NotNull(message = "Content cannot be empty")
  @Schema(description = "Content of the post", example = "This is the content of the post")
  private String content;
  @NotNull(message = "Categories cannot be empty")
  @Schema(description = "Categories associated with the post")
  private List<Long> categoryIds;
}
