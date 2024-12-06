package com.github.codogma.codogmaback.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.codogma.codogmaback.model.Role;
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
@Schema(name = "Get User")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetUser {

  @Schema(description = "Username of the user", example = "JohnDoe")
  private String username;
  @Schema(description = "Email of the user", example = "test@test.com")
  private String email;
  @Schema(description = "First name of the user", example = "John")
  private String firstName;
  @Schema(description = "Last name of the user", example = "Doe")
  private String lastName;
  @Schema(description = "Short info of the user", example = "I am a programmer")
  private String shortInfo;
  @Schema(description = "Bio of the user", example = "My name is John Doe, I am a programmer")
  private String bio;
  @Schema(description = "Role of the user", example = "USER")
  private Role role;
  @Schema(description = "Profile avatar image")
  private String avatarUrl;
  @Schema(description = "List of categories associated with the user")
  private List<GetCategory> categories;
}
