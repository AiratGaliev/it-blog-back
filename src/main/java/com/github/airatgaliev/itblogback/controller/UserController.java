package com.github.airatgaliev.itblogback.controller;

import com.github.airatgaliev.itblogback.dto.GetUser;
import com.github.airatgaliev.itblogback.dto.UpdateUser;
import com.github.airatgaliev.itblogback.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "Users", description = "API for users")
public class UserController {

  private final UserService userService;

  @GetMapping
  @Operation(summary = "Get all users")
  public ResponseEntity<List<GetUser>> getAllUsers() {
    List<GetUser> users = userService.getAllUsers();
    return ResponseEntity.ok(users);
  }

  @GetMapping("/authors")
  @Operation(summary = "Get all authors")
  public ResponseEntity<List<GetUser>> getAllAuthors() {
    List<GetUser> users = userService.getAllAuthors();
    return ResponseEntity.ok(users);
  }

  @GetMapping("/{username}")
  @Operation(summary = "Get an user by username")
  public ResponseEntity<GetUser> getUserByUsername(@PathVariable String username) {
    return userService.getUserByUsername(username)
        .map(createGetUser -> new ResponseEntity<>(createGetUser, HttpStatus.OK))
        .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Update an user by username")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<String> updateUser(@Valid UpdateUser updateUser,
      @ModelAttribute @RequestParam(value = "avatar", required = false) MultipartFile avatar,
      @AuthenticationPrincipal UserDetails userDetails) {
    updateUser.setAvatar(avatar);
    userService.updateUser(updateUser, userDetails);
    return new ResponseEntity<>("User updated successfully", HttpStatus.OK);
  }

  @DeleteMapping("/{username}")
  @Operation(summary = "Delete an user by username")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
  public ResponseEntity<Void> deleteUser(@PathVariable String username) {
    userService.deleteUser(username);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
