package com.github.airatgaliev.itblogback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

@Data
@Schema(name = "Get User")
public class UserDTO {

  @Schema(description = "ID of the user", example = "1")
  private Long id;

  @Schema(description = "Name of the user", example = "John Doe")
  private String name;

  @Schema(description = "List of posts written by the user with Author role")
  private List<GetPostDTO> posts;
}
