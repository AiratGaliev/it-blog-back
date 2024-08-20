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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
  @Operation(summary = "Get all articles or filter articles by various criteria", description = "Retrieve all articles or filter articles by category, tag, and/or content. Supports pagination and multiple filter combinations to narrow down search results.")
  @Parameters({@Parameter(name = "category", description = "Category to filter articles"),
      @Parameter(name = "tag", description = "Tag to filter articles"),
      @Parameter(name = "content", description = "Content to filter articles"),
      @Parameter(name = "page", description = "Page number to retrieve"),
      @Parameter(name = "size", description = "Number of articles per page"),
      @Parameter(name = "sort", description = "Field to sort by"),
      @Parameter(name = "order", description = "Order direction, either 'asc' or 'desc'")})
  public ResponseEntity<Page<GetArticle>> getAllArticles(
      @RequestParam(required = false) Long category, @RequestParam(required = false) String tag,
      @RequestParam(required = false) String content, @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "createdAt") String sort,
      @RequestParam(defaultValue = "desc") String order) {

    Sort.Direction sortDirection = Sort.Direction.fromString(order);
    Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
    Page<GetArticle> articles;

    if (category != null && tag != null && content != null) {
      articles = articleService.getArticlesByCategoryAndTagsNameAndContentContaining(category, tag,
          content, pageable);
    } else if (category != null && tag != null) {
      articles = articleService.getArticlesByCategoryAndTag(category, tag, pageable);
    } else if (category != null && content != null) {
      articles = articleService.getArticlesByCategoryAndContentContaining(category, content,
          pageable);
    } else if (tag != null && content != null) {
      articles = articleService.getArticlesByTagsNameAndContentContaining(tag, content, pageable);
    } else if (category != null) {
      articles = articleService.getArticlesByCategoryId(category, pageable);
    } else if (tag != null) {
      articles = articleService.getArticlesByTagsName(tag, pageable);
    } else if (content != null) {
      articles = articleService.getArticlesByContentContaining(content, pageable);
    } else {
      articles = articleService.getAllArticles(pageable);
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
      @Valid @ModelAttribute CreateArticle createArticle,
      @RequestPart(value = "images", required = false) List<MultipartFile> images,
      @AuthenticationPrincipal UserDetails userDetails) {
    createArticle.setImages(images);
    GetArticle createdArticle = articleService.createArticle(createArticle, userDetails);
    return new ResponseEntity<>(createdArticle, HttpStatus.CREATED);
  }

  @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Update an article by id")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAuthority('ROLE_AUTHOR')")
  public ResponseEntity<GetArticle> updateArticle(@PathVariable Long id,
      @Valid @ModelAttribute UpdateArticle updateArticle,
      @RequestPart(value = "images", required = false) List<MultipartFile> images,
      @AuthenticationPrincipal UserDetails userDetails) {
    updateArticle.setImages(images);
    GetArticle updatedArticle = articleService.updateArticle(id, updateArticle, userDetails);
    return new ResponseEntity<>(updatedArticle, HttpStatus.OK);
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
