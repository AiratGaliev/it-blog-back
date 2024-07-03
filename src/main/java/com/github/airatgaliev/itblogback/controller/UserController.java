package com.github.airatgaliev.itblogback.controller;

import com.github.airatgaliev.itblogback.dto.GetUserDTO;
import com.github.airatgaliev.itblogback.dto.UpdateUserDTO;
import com.github.airatgaliev.itblogback.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "Users", description = "API for users")
public class UserController {

  private final UserService userService;

  @GetMapping
  @Operation(summary = "Get all users")
  public ResponseEntity<List<GetUserDTO>> getAllUsers() {
    List<GetUserDTO> users = userService.getAllUsers();
    return ResponseEntity.ok(users);
  }

  @GetMapping("/{username}")
  @Operation(summary = "Get an user by username")
  public ResponseEntity<GetUserDTO> getUserByUsername(@PathVariable String username) {
    return userService.getUserByUsername(username)
        .map(createGetUserDTO -> new ResponseEntity<>(createGetUserDTO, HttpStatus.OK))
        .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  @PutMapping("/{username}")
  @Operation(summary = "Update an user by username")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<Void> updateUser(@PathVariable String username,
      @Valid @RequestBody UpdateUserDTO updateUserDTO) {
    userService.updateUser(username, updateUserDTO);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @DeleteMapping("/{username}")
  @Operation(summary = "Delete an user by username")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<Void> deleteUser(@PathVariable String username) {
    userService.deleteUser(username);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
