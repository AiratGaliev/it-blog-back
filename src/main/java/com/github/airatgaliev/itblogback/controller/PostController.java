package com.github.airatgaliev.itblogback.controller;

import com.github.airatgaliev.itblogback.dto.CreatePostDTO;
import com.github.airatgaliev.itblogback.dto.GetPostDTO;
import com.github.airatgaliev.itblogback.dto.UpdatePostDTO;
import com.github.airatgaliev.itblogback.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/posts")
@Tag(name = "Posts", description = "API for blog posts")
@ApiResponse(content = @Content(mediaType = "application/json"))
public class PostController {

  private final PostService postService;

  @GetMapping
  @Operation(summary = "Get all posts")
  public ResponseEntity<List<GetPostDTO>> getAllPosts() {
    List<GetPostDTO> posts = postService.getAllPosts();
    return ResponseEntity.ok(posts);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get post by id")
  public ResponseEntity<GetPostDTO> getPostById(@PathVariable Long id) {
    return postService.getPostById(id).map(post -> new ResponseEntity<>(post, HttpStatus.OK))
        .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }
  
  @PostMapping
  @Operation(summary = "Create a new post")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<String> createPost(@Valid @RequestBody CreatePostDTO createPostDTO,
      @AuthenticationPrincipal UserDetails userDetails) {
    postService.createPost(createPostDTO, userDetails);
    return new ResponseEntity<>("Post created successfully", HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update a post")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<String> updatePost(@PathVariable Long id,
      @Valid @RequestBody UpdatePostDTO updatePostDTO,
      @AuthenticationPrincipal UserDetails userDetails) {
    postService.updatePost(id, updatePostDTO, userDetails);
    return new ResponseEntity<>("Post updated successfully", HttpStatus.NO_CONTENT);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a post")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<String> deletePost(@PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {
    postService.deletePost(id, userDetails);
    return new ResponseEntity<>("Post deleted successfully", HttpStatus.NO_CONTENT);
  }
}
