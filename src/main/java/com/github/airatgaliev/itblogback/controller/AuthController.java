package com.github.airatgaliev.itblogback.controller;

import com.github.airatgaliev.itblogback.dto.AuthRequest;
import com.github.airatgaliev.itblogback.dto.AuthResponse;
import com.github.airatgaliev.itblogback.dto.CreateUserDTO;
import com.github.airatgaliev.itblogback.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "API for authentication")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  public ResponseEntity<Void> register(@Valid @RequestBody CreateUserDTO createUserDTO) {
    authService.register(createUserDTO);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
    return ResponseEntity.ok(authService.login(authRequest));
  }
}
