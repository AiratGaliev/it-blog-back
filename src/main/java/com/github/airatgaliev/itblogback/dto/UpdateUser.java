package com.github.airatgaliev.itblogback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Schema(name = "Update User")
public class UpdateUser {

  @Schema(description = "Username of the user", example = "John Doe")
  private String username;

  @Schema(description = "Email", example = "test@test.com")
  @Email
  private String email;

  @Schema(description = "User's avatar image file")
  private MultipartFile avatar;
}
