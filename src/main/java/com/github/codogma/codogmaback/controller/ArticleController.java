package com.github.codogma.codogmaback.controller;

import com.github.codogma.codogmaback.dto.CreateDraftArticle;
import com.github.codogma.codogmaback.dto.GetArticle;
import com.github.codogma.codogmaback.dto.UpdateArticle;
import com.github.codogma.codogmaback.dto.UpdateDraftArticle;
import com.github.codogma.codogmaback.model.UserModel;
import com.github.codogma.codogmaback.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/articles")
@Tag(name = "Articles", description = "API for blog articles")
public class ArticleController {

  private final ArticleService articleService;

  @GetMapping
  @Operation(summary = "Get all articles or filter articles by various criteria", description = "Retrieve all articles or filter articles by category, tag, and/or content. Supports pagination and multiple filter combinations to narrow down search results.")
  @Parameters({@Parameter(name = "categoryId", description = "Category id to filter articles"),
      @Parameter(name = "tag", description = "Tag to filter articles"),
      @Parameter(name = "username", description = "Author username to filter articles"),
      @Parameter(name = "content", description = "Content to filter articles"),
      @Parameter(name = "page", description = "Page number to retrieve"),
      @Parameter(name = "size", description = "Number of articles per page"),
      @Parameter(name = "sort", description = "Field to sort by"),
      @Parameter(name = "order", description = "Order direction, either 'asc' or 'desc'")})
  public ResponseEntity<Page<GetArticle>> getAllArticles(
      @RequestParam(required = false) Long categoryId, @RequestParam(required = false) String tag,
      @RequestParam(required = false) String username,
      @RequestParam(required = false) String content, @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "createdAt") String sort,
      @RequestParam(defaultValue = "desc") String order,
      @AuthenticationPrincipal UserModel userModel) {
    Page<GetArticle> articles = articleService.getArticles(order, sort, page, size, categoryId, tag,
        username, userModel, content);
    return ResponseEntity.ok(articles);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get an article")
  public ResponseEntity<GetArticle> getArticleById(@PathVariable Long id,
      @AuthenticationPrincipal UserModel userModel) {
    GetArticle article = articleService.getArticleById(id, userModel);
    return ResponseEntity.ok(article);
  }

  @GetMapping("/drafts")
  @Operation(summary = "Get the author's draft articles")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAuthority('ROLE_AUTHOR')")
  public ResponseEntity<List<GetArticle>> getDraftArticles(
      @AuthenticationPrincipal UserDetails userDetails) {
    List<GetArticle> articles = articleService.getDraftArticles(userDetails);
    return ResponseEntity.ok(articles);
  }

  @GetMapping("/drafts/{id}")
  @Operation(summary = "Get the author's drafted article")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAuthority('ROLE_AUTHOR')")
  public ResponseEntity<GetArticle> getDraftedArticleById(@PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {
    GetArticle article = articleService.getDraftedArticleById(id, userDetails);
    return ResponseEntity.ok(article);
  }

  @PostMapping("/drafts")
  @Operation(summary = "Create draft article")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAuthority('ROLE_AUTHOR')")
  public ResponseEntity<GetArticle> createDraftArticle(
      @Valid @RequestBody CreateDraftArticle draftArticle,
      @AuthenticationPrincipal UserDetails userDetails) {
    GetArticle article = articleService.createDraftArticle(draftArticle, userDetails);
    return new ResponseEntity<>(article, HttpStatus.CREATED);
  }

  @PatchMapping("/drafts/{id}")
  @Operation(summary = "Update draft article")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAuthority('ROLE_AUTHOR')")
  public ResponseEntity<Void> updateDraftArticle(@PathVariable Long id,
      @Valid @RequestBody UpdateDraftArticle draftArticle,
      @AuthenticationPrincipal UserDetails userDetails) {
    articleService.updateDraftArticle(id, draftArticle, userDetails);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/publish")
  @Operation(summary = "Publish an article")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_AUTHOR')")
  public ResponseEntity<Void> publishArticle(@PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {
    articleService.publishArticle(id, userDetails);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/hide")
  @Operation(summary = "Hide an article")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAnyAuthority('ROLE_AUTHOR')")
  public ResponseEntity<Void> hideArticle(@PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {
    articleService.hideArticle(id, userDetails);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/block")
  @Operation(summary = "Block an article")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
  public ResponseEntity<Void> blockArticle(@PathVariable Long id) {
    articleService.blockArticle(id);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/unblock")
  @Operation(summary = "Unblock an article")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
  public ResponseEntity<Void> unblockArticle(@PathVariable Long id) {
    articleService.unblockArticle(id);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update an article")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAuthority('ROLE_AUTHOR')")
  public ResponseEntity<Void> updateArticle(@PathVariable Long id,
      @Valid @RequestBody UpdateArticle article, @AuthenticationPrincipal UserDetails userDetails) {
    articleService.updateArticle(id, article, userDetails);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete an article")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAuthority('ROLE_AUTHOR')")
  public ResponseEntity<Void> deleteArticle(@PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {
    articleService.deleteArticle(id, userDetails);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/bookmark")
  @Operation(summary = "Add article to bookmarks")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_AUTHOR')")
  public ResponseEntity<Void> bookmark(@PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {
    articleService.bookmark(id, userDetails);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}/is-bookmarked")
  @Operation(summary = "Check if the authenticated user has added the article to bookmarks")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_AUTHOR')")
  public ResponseEntity<Boolean> isBookmarked(@PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {
    String authenticatedUsername = userDetails.getUsername();
    boolean isSubscribed = articleService.isBookmarked(authenticatedUsername, id);
    return ResponseEntity.ok(isSubscribed);
  }

  @DeleteMapping("/{id}/unbookmark")
  @Operation(summary = "Unbookmark an article")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_AUTHOR')")
  public ResponseEntity<Void> unbookmark(@PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {
    articleService.unbookmark(userDetails.getUsername(), id);
    return ResponseEntity.noContent().build();
  }
}
