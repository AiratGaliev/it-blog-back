package com.github.airatgaliev.itblogback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
@Schema(name = "Create User")
public class CreateUserDTO {

  @NotEmpty(message = "Username cannot be empty")
  @Schema(description = "Username of the user", example = "John Doe", requiredMode = RequiredMode.REQUIRED)
  private String username;

  @NotEmpty(message = "Email cannot be empty")
  @Email
  @Schema(description = "Email", example = "test@test.com", requiredMode = RequiredMode.REQUIRED)
  private String email;

  @NotEmpty(message = "Password cannot be empty")
  @Schema(description = "Password", example = "password", requiredMode = RequiredMode.REQUIRED)
  private String password;
}
