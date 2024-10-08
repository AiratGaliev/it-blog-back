package com.github.airatgaliev.itblogback.controller;

import com.github.airatgaliev.itblogback.dto.CreateArticle;
import com.github.airatgaliev.itblogback.dto.GetArticle;
import com.github.airatgaliev.itblogback.dto.UpdateArticle;
import com.github.airatgaliev.itblogback.interceptor.localization.LocalizationContext;
import com.github.airatgaliev.itblogback.model.ArticleModel;
import com.github.airatgaliev.itblogback.repository.specifications.ArticleSpecifications;
import com.github.airatgaliev.itblogback.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
@RequestMapping("/articles")
@Tag(name = "Articles", description = "API for blog articles")
public class ArticleController {

  private final ArticleService articleService;
  private final LocalizationContext localizationContext;

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
      @RequestParam(defaultValue = "desc") String order) {

    Sort.Direction sortDirection = Sort.Direction.fromString(order);
    Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
    List<String> supportedLanguages = localizationContext.getSupportedLanguages();
    Specification<ArticleModel> spec = Specification.where(
            ArticleSpecifications.hasCategoryId(categoryId)).and(ArticleSpecifications.hasTagName(tag))
        .and(ArticleSpecifications.hasUsername(username))
        .and(ArticleSpecifications.hasSupportedLanguage(supportedLanguages));

    Page<GetArticle> articles;

    if (content != null && !content.isEmpty()) {
      articles = articleService.searchAndFilterArticles(content, spec, pageable);
    } else {
      articles = articleService.getArticles(spec, pageable);
    }

    return ResponseEntity.ok(articles);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get an article by id")
  public ResponseEntity<GetArticle> getArticleById(@PathVariable Long id) {
    return articleService.getArticleById(id)
        .map(article -> new ResponseEntity<>(article, HttpStatus.OK))
        .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  @PostMapping
  @Operation(summary = "Create a new article")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAuthority('ROLE_AUTHOR')")
  public ResponseEntity<GetArticle> createArticle(@Valid @RequestBody CreateArticle createArticle,
      @AuthenticationPrincipal UserDetails userDetails) {
    GetArticle createdArticle = articleService.createArticle(createArticle, userDetails);
    return new ResponseEntity<>(createdArticle, HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update an article by id")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAuthority('ROLE_AUTHOR')")
  public ResponseEntity<GetArticle> updateArticle(@PathVariable Long id,
      @Valid @RequestBody UpdateArticle updateArticle,
      @AuthenticationPrincipal UserDetails userDetails) {
    GetArticle updatedArticle = articleService.updateArticle(id, updateArticle, userDetails);
    return new ResponseEntity<>(updatedArticle, HttpStatus.OK);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a article")
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
    articleService.bookmark(userDetails.getUsername(), id);
    return new ResponseEntity<>(HttpStatus.OK);
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
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
