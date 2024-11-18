package com.github.codogma.codogmaback.controller;

import com.github.codogma.codogmaback.dto.GetUser;
import com.github.codogma.codogmaback.dto.UpdateUser;
import com.github.codogma.codogmaback.dto.UserRole;
import com.github.codogma.codogmaback.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
  @Parameters({@Parameter(name = "categoryId", description = "Category id to filter authors"),
      @Parameter(name = "role", description = "Role to filter users", schema = @Schema(implementation = UserRole.class)),
      @Parameter(name = "tag", description = "Tag to filter users"),
      @Parameter(name = "info", description = "Information to filter users"),
      @Parameter(name = "page", description = "Page number to retrieve"),
      @Parameter(name = "size", description = "Number of categories per page"),
      @Parameter(name = "sort", description = "Field to sort by"),
      @Parameter(name = "order", description = "Order direction, either 'asc' or 'desc'")})
  public ResponseEntity<List<GetUser>> getAllUsers(@RequestParam(required = false) Long categoryId,
      @RequestParam(required = false) UserRole role, @RequestParam(required = false) String tag,
      @RequestParam(required = false) String info, @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "username") String sort,
      @RequestParam(defaultValue = "asc") String order) {
    List<GetUser> users = userService.getAllUsers(categoryId, role, tag, info, page, size,
        sort, order);
    // TODO delete this code after testing
//    List<GetUser> users;
//    if (categoryId != null && role == UserRole.ROLE_AUTHOR) {
//      users = userService.getAllAuthorsByCategoryId(categoryId);
//    } else if (role != null) {
//      users = userService.getAllByRole(role.toRole());
//    } else {
//      users = userService.getAllUsersOld();
//    }
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
  public ResponseEntity<Object> updateUser(@Valid @ModelAttribute UpdateUser updateUser,
      @RequestParam(value = "avatar", required = false) MultipartFile avatar,
      @AuthenticationPrincipal UserDetails userDetails, BindingResult bindingResult) {
    updateUser.setAvatar(avatar);
    GetUser updatedUser = userService.updateUser(updateUser, userDetails, bindingResult);
    if (bindingResult.hasErrors()) {
      Map<String, String> errors = bindingResult.getFieldErrors().stream().collect(
          Collectors.toMap(FieldError::getField,
              fieldError -> Optional.ofNullable(fieldError.getDefaultMessage())
                  .orElse("Unknown error")));
      return ResponseEntity.badRequest().body(errors);
    }
    return new ResponseEntity<>(updatedUser, HttpStatus.OK);
  }

  @DeleteMapping("/{username}")
  @Operation(summary = "Delete an user by username")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
  public ResponseEntity<Void> deleteUser(@PathVariable String username) {
    userService.deleteUser(username);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @PostMapping("/{username}/subscribe")
  @Operation(summary = "Subscribe to a user")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_AUTHOR')")
  public ResponseEntity<Void> subscribe(@PathVariable String username,
      @AuthenticationPrincipal UserDetails userDetails) {
    userService.subscribe(userDetails.getUsername(), username);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @GetMapping("/{username}/is-subscribed")
  @Operation(summary = "Check if the authenticated user is subscribed to another user")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_AUTHOR')")
  public ResponseEntity<Boolean> isSubscribed(@PathVariable String username,
      @AuthenticationPrincipal UserDetails userDetails) {
    String authenticatedUsername = userDetails.getUsername();
    boolean isSubscribed = userService.isSubscribed(authenticatedUsername, username);
    return ResponseEntity.ok(isSubscribed);
  }

  @DeleteMapping("/{username}/unsubscribe")
  @Operation(summary = "Unsubscribe from a user")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_AUTHOR')")
  public ResponseEntity<Void> unsubscribe(@PathVariable String username,
      @AuthenticationPrincipal UserDetails userDetails) {
    userService.unsubscribe(userDetails.getUsername(), username);
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
