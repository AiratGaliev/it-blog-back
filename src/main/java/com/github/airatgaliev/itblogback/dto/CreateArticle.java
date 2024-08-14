package com.github.airatgaliev.itblogback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Schema(name = "Create Article")
public class CreateArticle {

  @NotNull(message = "Title cannot be empty")
  @Schema(description = "Title of the article", example = "My First Blog Article", requiredMode = RequiredMode.REQUIRED)
  private String title;
  @NotNull(message = "Content cannot be empty")
  @Schema(description = "Content of the article", example = "This is the content of the article", requiredMode = RequiredMode.REQUIRED)
  private String content;
  @NotNull(message = "Content cannot be empty")
  @Schema(description = "Categories associated with the article")
  private List<Long> categoryIds;
  @Schema(description = "Tags associated with the article", requiredMode = RequiredMode.NOT_REQUIRED)
  private List<Long> tagIds = new ArrayList<>();
  @Schema(type = "array", format = "binary", description = "Article images", requiredMode = RequiredMode.NOT_REQUIRED)
  private List<MultipartFile> images = new ArrayList<>();
}
