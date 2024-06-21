package com.github.airatgaliev.itblogback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(name = "Get Post")
public class GetPostDTO {

  @Schema(description = "Title of the post", example = "My First Blog Post")
  private String title;
  @Schema(description = "Content of the post", example = "This is the content of the post")
  private String content;
  @Schema(description = "Author ID associated with the post", example = "1")
  private Long authorId;
}
