package com.github.airatgaliev.itblogback.dto;

import com.github.airatgaliev.itblogback.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "User Roles", enumAsRef = true, description = "Role to filter users")
public enum UserRole {
  ROLE_USER,
  ROLE_AUTHOR;

  public static UserRole fromRole(Role role) {
    return UserRole.valueOf(role.name());
  }

  public Role toRole() {
    return Role.valueOf(this.name());
  }
}
