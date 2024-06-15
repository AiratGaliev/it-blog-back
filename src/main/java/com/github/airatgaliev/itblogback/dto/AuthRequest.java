package com.github.airatgaliev.itblogback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
@Schema(name = "Authentication Request")
public class AuthRequest {

  @NotEmpty
  @Schema(description = "Username of the user")
  private String username;

  @NotEmpty
  @Schema(description = "Password of the user")
  private String password;
}
