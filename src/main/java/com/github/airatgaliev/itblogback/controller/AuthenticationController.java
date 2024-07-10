package com.github.airatgaliev.itblogback.controller;

import com.github.airatgaliev.itblogback.dto.AuthenticationResponse;
import com.github.airatgaliev.itblogback.dto.SignInRequest;
import com.github.airatgaliev.itblogback.dto.SignUpRequest;
import com.github.airatgaliev.itblogback.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "API for authentication")
public class AuthenticationController {

  private final AuthenticationService authenticationService;

  @PostMapping("/signup")
  @Operation(summary = "Register a new user")
  public ResponseEntity<String> register(@Valid @RequestBody SignUpRequest signUpRequest) {
    String username = authenticationService.signup(signUpRequest).getUsername();
    return ResponseEntity.status(HttpStatus.CREATED)
        .body("User registered successfully with username: " + username);
  }

  @PostMapping("/login")
  @Operation(summary = "Authenticate a user")
  public ResponseEntity<AuthenticationResponse> authenticate(
      @Valid @RequestBody SignInRequest signInRequest) {
    AuthenticationResponse authenticatedUser = authenticationService.authenticate(
        signInRequest);
    return ResponseEntity.ok(authenticatedUser);
  }
}
