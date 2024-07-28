package com.github.airatgaliev.itblogback.dto;

import com.github.airatgaliev.itblogback.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(name = "Get User")
public class GetUser {

  @Schema(description = "Username of the user", example = "JohnDoe")
  private String username;

  @Schema(description = "Email of the user", example = "test@test.com")
  private String email;

  @Schema(description = "First name of the user", example = "John")
  private String firstName;

  @Schema(description = "Last name of the user", example = "Doe")
  private String lastName;

  @Schema(description = "Role of the user", example = "USER")
  private Role role;

  @Schema(description = "Profile avatar image")
  private String avatarUrl;

  @Schema(description = "List of articles written by the user with Author role")
  private List<Long> articlesIds;
}
