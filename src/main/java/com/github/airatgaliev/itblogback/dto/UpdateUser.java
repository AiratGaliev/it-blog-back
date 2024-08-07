package com.github.airatgaliev.itblogback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Schema(name = "Update User")
public class UpdateUser {

  @Schema(description = "Username of the user", example = "JohnDoe")
  private String username;

  @Schema(description = "First name of the user", example = "John")
  private String firstName;

  @Schema(description = "Last name of the user", example = "Doe")
  private String lastName;

  @Schema(description = "Bio of the user", example = "I am John Doe")
  private String bio;

  @Schema(description = "Email", example = "test@test.com")
  @Email(message = "Invalid email address")
  private String newEmail;

  @Schema(description = "User's current password", example = "P@ssword")
  private String currentPassword;

  @Schema(description = "User's new password", example = "P@ssw0rd")
  private String newPassword;

  @Schema(description = "User's avatar image")
  private MultipartFile avatar;
}
