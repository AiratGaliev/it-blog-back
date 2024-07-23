package com.github.airatgaliev.itblogback.controller;

import com.github.airatgaliev.itblogback.dto.CreatePost;
import com.github.airatgaliev.itblogback.dto.GetPost;
import com.github.airatgaliev.itblogback.dto.UpdatePost;
import com.github.airatgaliev.itblogback.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
@Tag(name = "Posts", description = "API for blog posts")
public class PostController {

  private final PostService postService;

  @GetMapping
  @Operation(summary = "Get all posts or posts by category", description = "Retrieve all posts or filter posts by category")
  @Parameters({@Parameter(name = "category", description = "Category to filter posts")})
  public ResponseEntity<List<GetPost>> getAllPosts(@RequestParam(required = false) Long category) {
    List<GetPost> posts;
    if (category == null) {
      posts = postService.getAllPosts();
    } else {
      posts = postService.getPostsByCategoryId(category);
    }
    return ResponseEntity.ok(posts);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get post by id")
  public ResponseEntity<GetPost> getPostById(@PathVariable Long id) {
    return postService.getPostById(id).map(post -> new ResponseEntity<>(post, HttpStatus.OK))
        .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  @PostMapping
  @Operation(summary = "Create a new post")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAuthority('ROLE_AUTHOR')")
  public ResponseEntity<GetPost> createPost(@Valid @RequestBody CreatePost createPost,
      @AuthenticationPrincipal UserDetails userDetails) {
    GetPost createdPost = postService.createPost(createPost, userDetails);
    return new ResponseEntity<>(createdPost, HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update a post")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAuthority('ROLE_AUTHOR')")
  public ResponseEntity<String> updatePost(@PathVariable Long id,
      @Valid @RequestBody UpdatePost updatePost,
      @AuthenticationPrincipal UserDetails userDetails) {
    postService.updatePost(id, updatePost, userDetails);
    return new ResponseEntity<>("Post updated successfully", HttpStatus.NO_CONTENT);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a post")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAuthority('ROLE_AUTHOR')")
  public ResponseEntity<String> deletePost(@PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {
    postService.deletePost(id, userDetails);
    return new ResponseEntity<>("Post deleted successfully", HttpStatus.NO_CONTENT);
  }
}
