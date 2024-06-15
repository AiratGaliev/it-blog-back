package com.github.airatgaliev.itblogback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(name = "Authentication Response")
public class AuthResponse {

  private String token;
}
