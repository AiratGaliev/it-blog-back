package com.github.airatgaliev.itblogback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
@Schema(name = "Update Article")
public class UpdateArticle {

  @NotNull(message = "Title cannot be empty")
  @Schema(description = "Title of the article", example = "My First Blog Article")
  private String title;
  @NotNull(message = "Content cannot be empty")
  @Schema(description = "Content of the article", example = "This is the content of the article")
  private String content;
  @NotNull(message = "Categories cannot be empty")
  @Schema(description = "Categories associated with the article")
  private List<Long> categoryIds;
}
