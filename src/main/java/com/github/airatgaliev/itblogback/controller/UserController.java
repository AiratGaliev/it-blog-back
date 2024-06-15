package com.github.airatgaliev.itblogback.controller;

import com.github.airatgaliev.itblogback.dto.CreateUserDTO;
import com.github.airatgaliev.itblogback.dto.UpdateUserDTO;
import com.github.airatgaliev.itblogback.dto.UserDTO;
import com.github.airatgaliev.itblogback.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/authors")
@Tag(name = "Authors", description = "API for authors")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  @Operation(summary = "Get all authors")
  public ResponseEntity<List<UserDTO>> getAllAuthors() {
    List<UserDTO> authors = userService.getAllAuthors();
    return ResponseEntity.ok(authors);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get an author by id")
  public ResponseEntity<UserDTO> getAuthorById(@PathVariable Long id) {
    return userService.getAuthorById(id)
        .map(createUserDTO -> new ResponseEntity<>(createUserDTO, HttpStatus.OK))
        .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  @PostMapping
  @Operation(summary = "Create a new author")
  public ResponseEntity<Void> createAuthor(@Valid @RequestBody CreateUserDTO createUserDTO) {
    userService.createAuthor(createUserDTO);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update an author")
  public ResponseEntity<Void> updateAuthor(@PathVariable Long id,
      @Valid @RequestBody UpdateUserDTO updateUserDTO) {
    userService.updateAuthor(id, updateUserDTO);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete an author")
  public ResponseEntity<Void> deleteAuthor(@PathVariable Long id) {
    userService.deleteAuthor(id);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
