package com.github.airatgaliev.itblogback.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Get Category")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetCategory {

  @Schema(description = "ID of the category", example = "1")
  private Long id;
  @Schema(description = "Name of the category", example = "Technology")
  private String name;
  @Schema(description = "Description of the category", example = "Articles about technology")
  private String description;
  @Schema(description = "Image of the category")
  private String imageUrl;
  @Schema(description = "Tags associated with the category articles")
  private List<GetTag> tags;
}
