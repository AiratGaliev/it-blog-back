package com.github.airatgaliev.itblogback.controller;

import com.github.airatgaliev.itblogback.dto.AuthenticationResponse;
import com.github.airatgaliev.itblogback.dto.GetUser;
import com.github.airatgaliev.itblogback.dto.SignInRequest;
import com.github.airatgaliev.itblogback.dto.SignUpRequest;
import com.github.airatgaliev.itblogback.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
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
      @Valid @RequestBody SignInRequest signInRequest, HttpServletResponse response) {
    AuthenticationResponse authenticatedUser = authenticationService.authenticate(signInRequest,
        response);
    return ResponseEntity.ok(authenticatedUser);
  }

  @GetMapping("/current-user")
  @Operation(summary = "Get the current authenticated user")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<GetUser> getCurrentUser(HttpServletRequest request) {
    return authenticationService.getCurrentUser(request)
        .map(ResponseEntity::ok)
        .orElseGet(() -> new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
  }

  @PostMapping("/refresh-token")
  @Operation(summary = "Refresh the JWT token")
  public ResponseEntity<AuthenticationResponse> refreshToken(HttpServletRequest request,
      HttpServletResponse response) {
    AuthenticationResponse refreshedToken = authenticationService.handleTokenRefresh(request,
        response);
    return ResponseEntity.ok(refreshedToken);
  }

  @PostMapping("/logout")
  @Operation(summary = "Logout a user")
  public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
    authenticationService.logout(request, response);
    return ResponseEntity.ok("Logged out successfully");
  }
}
