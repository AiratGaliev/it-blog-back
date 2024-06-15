package com.github.airatgaliev.itblogback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
@Schema(name = "Update User")
public class UpdateUserDTO {

  @Schema(description = "Username of the user", example = "John Doe")
  private String username;

  @Schema(description = "Email", example = "test@test.com")
  @Email
  private String email;
}
