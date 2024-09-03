package com.github.airatgaliev.itblogback.controller;

import com.github.airatgaliev.itblogback.dto.CreateComment;
import com.github.airatgaliev.itblogback.dto.GetComment;
import com.github.airatgaliev.itblogback.dto.UpdateComment;
import com.github.airatgaliev.itblogback.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
  public List<GetComment> getCommentsByArticleId(@PathVariable Long articleId) {
    return commentService.getCommentsByArticleId(articleId);
  }

  @PostMapping
  @Operation(summary = "Add a new comment")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_AUTHOR')")
  public GetComment createComment(@RequestBody CreateComment createComment,
      @AuthenticationPrincipal UserDetails userDetails) {
    return commentService.createComment(createComment, userDetails);
  }

  @GetMapping("/user/{username}")
  @Operation(summary = "Get all comments created by a user")
  @SecurityRequirement(name = "bearerAuth")
  public List<GetComment> getCommentsByUsername(@PathVariable String username) {
    return commentService.getCommentsByUsername(username);
  }

  @PutMapping("/{commentId}")
  @Operation(summary = "Update a comment")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_AUTHOR')")
  public GetComment updateComment(@PathVariable Long commentId,
      @RequestBody UpdateComment updateComment, @AuthenticationPrincipal UserDetails userDetails) {
    return commentService.updateComment(commentId, updateComment, userDetails);
  }

  @DeleteMapping("/{commentId}")
  @Operation(summary = "Delete a comment")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_AUTHOR')")
  public void deleteComment(@PathVariable Long commentId,
      @AuthenticationPrincipal UserDetails userDetails) {
    commentService.deleteComment(commentId, userDetails);
  }
}
