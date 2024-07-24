package com.github.airatgaliev.itblogback.service;

import static com.github.airatgaliev.itblogback.util.TokenUtils.extractToken;
import static com.github.airatgaliev.itblogback.util.TokenUtils.invalidateToken;

import com.github.airatgaliev.itblogback.dto.AuthenticationResponse;
import com.github.airatgaliev.itblogback.dto.GetUser;
import com.github.airatgaliev.itblogback.dto.SignInRequest;
import com.github.airatgaliev.itblogback.dto.SignUpRequest;
import com.github.airatgaliev.itblogback.exception.UserAlreadyExistsException;
import com.github.airatgaliev.itblogback.model.Role;
import com.github.airatgaliev.itblogback.model.UserModel;
import com.github.airatgaliev.itblogback.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final UserDetailsService userDetailsService;
  private final JwtService jwtService;

  public GetUser signup(SignUpRequest input) {
    validateUniqueness(input);

    UserModel user = UserModel.builder().username(input.getUsername()).email(input.getEmail())
        .password(passwordEncoder.encode(input.getPassword())).role(Role.ROLE_USER).build();

    UserModel savedUser = userRepository.save(user);
    return GetUser.builder().username(savedUser.getUsername()).email(savedUser.getEmail())
        .firstName(savedUser.getFirstName()).lastName(savedUser.getLastName())
        .role(savedUser.getRole()).build();
  }

  private void validateUniqueness(SignUpRequest input) {
    Map<String, Function<String, Optional<UserModel>>> uniquenessChecks = new HashMap<>();
    uniquenessChecks.put("username", userRepository::findByUsername);
    uniquenessChecks.put("email", userRepository::findByEmail);

    Map<String, String> existingFields = new HashMap<>();

    for (Map.Entry<String, Function<String, Optional<UserModel>>> entry : uniquenessChecks.entrySet()) {
      String field = entry.getKey();
      String value = getFieldValue(input, field);

      if (value != null && entry.getValue().apply(value).isPresent()) {
        existingFields.put(field, value);
      }
    }

    if (!existingFields.isEmpty()) {
      throw new UserAlreadyExistsException("The following fields already exist: " + existingFields);
    }
  }

  private String getFieldValue(SignUpRequest signUpRequest, String fieldName) {
    try {
      var field = SignUpRequest.class.getDeclaredField(fieldName);
      field.setAccessible(true);
      return (String) field.get(signUpRequest);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException("Error accessing field: " + fieldName, e);
    }
  }

  public AuthenticationResponse authenticate(SignInRequest input, HttpServletResponse response) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(input.getUsernameOrEmail(), input.getPassword()));
    UserModel user = userRepository.findByUsernameOrEmail(input.getUsernameOrEmail(),
        input.getUsernameOrEmail()).orElseThrow(
        () -> new UsernameNotFoundException("User not found " + input.getUsernameOrEmail()));
    String jwtToken = jwtService.generateToken(user);
    setAuthTokenCookie(response, jwtToken);
    return AuthenticationResponse.builder().token(jwtToken)
        .expiresIn(jwtService.getExpirationTime()).build();
  }

  private void setAuthTokenCookie(HttpServletResponse response, String token) {
    ResponseCookie cookie = ResponseCookie.from("auth-token", token).httpOnly(true).secure(false)
        .path("/").build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }

  public AuthenticationResponse handleTokenRefresh(HttpServletRequest request,
      HttpServletResponse response) {
    String token = extractToken(request);
    if (token == null || jwtService.isTokenExpired(token)) {
      throw new RuntimeException("Token has expired or is invalid");
    }
    AuthenticationResponse refreshedToken = refreshToken(token);
    setAuthTokenCookie(response, refreshedToken.getToken());
    return refreshedToken;
  }

  public AuthenticationResponse refreshToken(String token) {
    String username = jwtService.extractUsername(token);
    UserModel user = userRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found " + username));

    String newToken = jwtService.generateToken(user);
    return AuthenticationResponse.builder().token(newToken)
        .expiresIn(jwtService.getExpirationTime()).build();
  }

  public Optional<GetUser> getCurrentUser(HttpServletRequest request) {
    String token = extractToken(request);
    if (token != null) {
      String username = jwtService.extractUsername(token);
      return userRepository.findByUsername(username).map(
          user -> GetUser.builder().username(user.getUsername()).email(user.getEmail())
              .firstName(user.getFirstName()).lastName(user.getLastName()).role(user.getRole())
              .build());
    }
    return Optional.empty();
  }

  public void logout(HttpServletRequest request, HttpServletResponse response) {
    String token = extractToken(request);
    String username = jwtService.extractUsername(token);
    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
    if (token != null && jwtService.isTokenValid(token, userDetails)) {
      invalidateToken(response);
    }
  }
}