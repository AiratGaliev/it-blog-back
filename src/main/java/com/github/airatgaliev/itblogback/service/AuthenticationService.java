package com.github.airatgaliev.itblogback.service;

import com.github.airatgaliev.itblogback.dto.AuthenticationResponse;
import com.github.airatgaliev.itblogback.dto.GetUser;
import com.github.airatgaliev.itblogback.dto.SignInRequest;
import com.github.airatgaliev.itblogback.dto.SignUpRequest;
import com.github.airatgaliev.itblogback.exception.UserAlreadyExistsException;
import com.github.airatgaliev.itblogback.model.Role;
import com.github.airatgaliev.itblogback.model.UserModel;
import com.github.airatgaliev.itblogback.repository.UserRepository;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;

  public GetUser signup(SignUpRequest input) {
    new UserModel();
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

    UserModel user = UserModel.builder().username(input.getUsername()).email(input.getEmail())
        .password(passwordEncoder.encode(input.getPassword())).role(Role.ROLE_USER).build();

    UserModel savedUser = userRepository.save(user);
    return GetUser.builder().username(savedUser.getUsername()).email(savedUser.getEmail())
        .firstName(savedUser.getFirstName()).lastName(savedUser.getLastName())
        .role(savedUser.getRole()).build();
  }

  public AuthenticationResponse authenticate(SignInRequest input) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(input.getUsernameOrEmail(), input.getPassword()));
    // TODO: check if user exists with existsByUsernameOrEmail method
    UserModel user = userRepository.findByUsernameOrEmail(input.getUsernameOrEmail(),
        input.getUsernameOrEmail()).orElseThrow(
        () -> new UsernameNotFoundException("User not found " + input.getUsernameOrEmail()));
    String jwtToken = jwtService.generateToken(user);
    return AuthenticationResponse.builder().token(jwtToken)
        .expiresIn(jwtService.getExpirationTime()).build();
  }

  private String getFieldValue(SignUpRequest signUpRequest, String fieldName) {
    try {
      Field field = SignUpRequest.class.getDeclaredField(fieldName);
      field.setAccessible(true);
      return (String) field.get(signUpRequest);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException("Error accessing field: " + fieldName, e);
    }
  }
}
