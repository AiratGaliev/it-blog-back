package com.github.codogma.codogmaback.controller;

import com.github.codogma.codogmaback.dto.AuthenticationResponse;
import com.github.codogma.codogmaback.dto.GetUser;
import com.github.codogma.codogmaback.dto.SignInRequest;
import com.github.codogma.codogmaback.dto.SignUpRequest;
import com.github.codogma.codogmaback.model.UserModel;
import com.github.codogma.codogmaback.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "API for authentication")
public class AuthenticationController {

  private final AuthenticationService authenticationService;

  @PostMapping(value = "/sign-up", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Register a new user")
  public ResponseEntity<String> signUp(@Valid @ModelAttribute SignUpRequest signUpRequest,
      @RequestPart(value = "avatar") MultipartFile avatar, @RequestHeader("Origin") String origin) {
    String username = authenticationService.signUp(signUpRequest, avatar, origin).getUsername();
    return ResponseEntity.status(HttpStatus.CREATED)
        .body("User registered successfully with username: " + username);
  }

  @PostMapping("/confirm-email")
  @Operation(summary = "Email confirmation")
  public ResponseEntity<String> confirmEmail(@RequestParam("token") String token) {
    authenticationService.confirmEmail(token);
    return ResponseEntity.ok("Email confirmed successfully");
  }

  @PostMapping("/sign-in")
  @Operation(summary = "Authenticate a user")
  public ResponseEntity<AuthenticationResponse> signIn(
      @Valid @RequestBody SignInRequest signInRequest, HttpServletResponse response) {
    AuthenticationResponse authenticatedUser = authenticationService.signIn(signInRequest,
        response);
    return ResponseEntity.ok(authenticatedUser);
  }

  @GetMapping("/current-user")
  @Operation(summary = "Get the current authenticated user")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<GetUser> currentUser(@AuthenticationPrincipal UserModel userModel) {
    return authenticationService.currentUser(userModel).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.noContent().build());
  }

  @PostMapping("/refresh-token")
  @Operation(summary = "Refresh the HttpOnly cookie 'auth-token'")
  public ResponseEntity<Void> refreshToken(HttpServletResponse response,
      @AuthenticationPrincipal UserModel userModel) {
    authenticationService.refreshToken(response, userModel);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/logout")
  @Operation(summary = "Logout a user")
  public ResponseEntity<String> logout(HttpServletResponse response,
      @AuthenticationPrincipal UserModel userModel) {
    authenticationService.logout(response, userModel);
    return ResponseEntity.ok("Logged out successfully");
  }
}