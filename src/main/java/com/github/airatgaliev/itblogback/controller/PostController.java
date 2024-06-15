package com.github.airatgaliev.itblogback.controller;

import com.github.airatgaliev.itblogback.dto.CreatePostDTO;
import com.github.airatgaliev.itblogback.dto.GetPostDTO;
import com.github.airatgaliev.itblogback.dto.UpdatePostDTO;
import com.github.airatgaliev.itblogback.service.PostService;
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
@RequestMapping("/posts")
@Tag(name = "Posts", description = "API for blog posts")
public class PostController {

  private final PostService postService;

  public PostController(PostService postService) {
    this.postService = postService;
  }

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
  public ResponseEntity<Void> createPost(@Valid @RequestBody CreatePostDTO createPostDTO) {
    postService.createPost(createPostDTO);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update a post")
  public ResponseEntity<Void> updatePost(@Valid @PathVariable Long id,
      @RequestBody UpdatePostDTO updatePostDTO) {
    postService.updatePost(id, updatePostDTO);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a post")
  public ResponseEntity<Void> deletePost(@PathVariable Long id) {
    postService.deletePost(id);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
