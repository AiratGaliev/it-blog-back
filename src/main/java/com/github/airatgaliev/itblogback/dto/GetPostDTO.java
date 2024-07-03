package com.github.airatgaliev.itblogback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(name = "Get Post")
public class GetPostDTO {

  @Schema(description = "ID of the post", example = "1")
  private Long id;
  @Schema(description = "Title of the post", example = "My First Blog Post")
  private String title;
  @Schema(description = "Content of the post", example = "This is the content of the post")
  private String content;
  @Schema(description = "Username associated with the post", example = "JohnDoe")
  private String username;
  @Schema(description = "Categories associated with the post")
  private List<GetCategoryDTO> categories;
  @Schema(description = "Date and time of the post creation", example = "2022-01-01T00:00:00.000Z")
  private Date createdAt;
  @Schema(description = "Date and time of the post update", example = "2022-01-01T01:00:00.000Z")
  private Date updatedAt;
}
