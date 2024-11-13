package com.github.codogma.codogmaback.controller;

import com.github.codogma.codogmaback.dto.CreateComment;
import com.github.codogma.codogmaback.dto.GetComment;
import com.github.codogma.codogmaback.dto.UpdateComment;
import com.github.codogma.codogmaback.model.UserModel;
import com.github.codogma.codogmaback.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
@Tag(name = "Comments", description = "API for comments management")
public class CommentController {

  private final CommentService commentService;

  @GetMapping("/article/{articleId}")
  @Operation(summary = "Get all comments for an article")
  public List<GetComment> getCommentsByArticleId(@PathVariable Long articleId,
      @AuthenticationPrincipal UserModel userModel) {
    return commentService.getCommentsByArticleId(articleId, userModel);
  }

  @PostMapping
  @Operation(summary = "Add a new comment")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_AUTHOR', 'ROLE_ADMIN')")
  public ResponseEntity<GetComment> createComment(@Valid @RequestBody CreateComment createComment,
      @AuthenticationPrincipal UserModel userModel) {
    GetComment comment = commentService.createComment(createComment, userModel);
    return new ResponseEntity<>(comment, HttpStatus.CREATED);
  }

  @GetMapping("/user/{username}")
  @Operation(summary = "Get all comments created by a user")
  @SecurityRequirement(name = "bearerAuth")
  public List<GetComment> getCommentsByUsername(@PathVariable String username,
      @AuthenticationPrincipal UserModel userModel) {
    return commentService.getCommentsByUsername(username, userModel);
  }

  @PatchMapping("/{commentId}/publish")
  @Operation(summary = "Publish a comment")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public GetComment publishComment(@PathVariable Long commentId) {
    return commentService.publishComment(commentId);
  }

  @PatchMapping("/{commentId}/block")
  @Operation(summary = "Block a comment")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public GetComment blockComment(@PathVariable Long commentId) {
    return commentService.blockComment(commentId);
  }

  @PutMapping("/{commentId}")
  @Operation(summary = "Update a comment")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_AUTHOR', 'ROLE_ADMIN')")
  public GetComment updateComment(@PathVariable Long commentId,
      @Valid @RequestBody UpdateComment updateComment,
      @AuthenticationPrincipal UserModel userModel) {
    return commentService.updateComment(commentId, updateComment, userModel);
  }

  @DeleteMapping("/{commentId}")
  @Operation(summary = "Delete a comment")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_AUTHOR', 'ROLE_ADMIN')")
  public void deleteComment(@PathVariable Long commentId,
      @AuthenticationPrincipal UserDetails userDetails) {
    commentService.deleteComment(commentId, userDetails);
  }
}
