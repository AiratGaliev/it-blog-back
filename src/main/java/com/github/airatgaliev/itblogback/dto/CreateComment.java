package com.github.airatgaliev.itblogback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Create Comment")
public class CreateComment {

  @Schema(description = "ID of the article", example = "1", requiredMode = RequiredMode.REQUIRED)
  private Long articleId;
  @Schema(description = "ID of the parent comment", example = "1")
  private Long parentCommentId;
  @Schema(description = "Content of the comment", example = "This is the content of the comment", requiredMode = RequiredMode.REQUIRED, minLength = 2, maxLength = 1000)
  private String content;
}
