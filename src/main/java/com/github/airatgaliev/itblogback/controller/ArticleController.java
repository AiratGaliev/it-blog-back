package com.github.airatgaliev.itblogback.controller;

import com.github.airatgaliev.itblogback.dto.CreateArticle;
import com.github.airatgaliev.itblogback.dto.GetArticle;
import com.github.airatgaliev.itblogback.dto.UpdateArticle;
import com.github.airatgaliev.itblogback.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/articles")
@Tag(name = "Articles", description = "API for blog articles")
public class ArticleController {

  private final ArticleService articleService;

  @GetMapping
  @Operation(summary = "Get all articles or articles by category", description = "Retrieve all articles or filter articles by category")
  @Parameters({@Parameter(name = "category", description = "Category to filter articles")})
  public ResponseEntity<List<GetArticle>> getAllArticles(
      @RequestParam(required = false) Long category) {
    List<GetArticle> articles;
    if (category == null) {
      articles = articleService.getAllArticles();
    } else {
      articles = articleService.getArticlesByCategoryId(category);
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

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Create a new article")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAuthority('ROLE_AUTHOR')")
  public ResponseEntity<GetArticle> createArticle(
      @ModelAttribute CreateArticle createArticle,
      @RequestPart List<MultipartFile> images,
      @AuthenticationPrincipal UserDetails userDetails) {
    createArticle.setImages(images);
    GetArticle createdArticle = articleService.createArticle(createArticle, userDetails);
    return new ResponseEntity<>(createdArticle, HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update an article by id")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAuthority('ROLE_AUTHOR')")
  public ResponseEntity<String> updateArticle(@PathVariable Long id,
      @Valid @RequestBody UpdateArticle updateArticle,
      @AuthenticationPrincipal UserDetails userDetails) {
    articleService.updateArticle(id, updateArticle, userDetails);
    return new ResponseEntity<>("Article updated successfully", HttpStatus.OK);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a article")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAuthority('ROLE_AUTHOR')")
  public ResponseEntity<String> deleteArticle(@PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {
    articleService.deleteArticle(id, userDetails);
    return new ResponseEntity<>("Article deleted successfully", HttpStatus.NO_CONTENT);
  }
}
