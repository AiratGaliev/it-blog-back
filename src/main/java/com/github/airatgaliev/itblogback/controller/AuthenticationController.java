package com.github.airatgaliev.itblogback.controller;

import com.github.airatgaliev.itblogback.dto.AuthenticationResponseDTO;
import com.github.airatgaliev.itblogback.dto.SignInRequestDTO;
import com.github.airatgaliev.itblogback.dto.SignUpRequestDTO;
import com.github.airatgaliev.itblogback.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
@ApiResponse(content = @Content(mediaType = "application/json"))
public class AuthenticationController {

  private final AuthenticationService authenticationService;

  @PostMapping("/signup")
  @Operation(summary = "Register a new user")
  public ResponseEntity<String> register(@Valid @RequestBody SignUpRequestDTO signUpRequestDTO) {
    Long userId = authenticationService.signup(signUpRequestDTO).getId();
    return ResponseEntity.status(HttpStatus.CREATED)
        .body("User registered successfully with ID: " + userId);
  }

  @PostMapping("/login")
  @Operation(summary = "Authenticate a user")
  public ResponseEntity<AuthenticationResponseDTO> authenticate(
      @Valid @RequestBody SignInRequestDTO signInRequestDTO) {
    AuthenticationResponseDTO authenticatedUser = authenticationService.authenticate(
        signInRequestDTO);
    return ResponseEntity.ok(authenticatedUser);
  }
}
