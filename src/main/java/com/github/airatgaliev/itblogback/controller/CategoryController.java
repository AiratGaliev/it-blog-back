package com.github.airatgaliev.itblogback.controller;

import com.github.airatgaliev.itblogback.dto.CreateCategoryDTO;
import com.github.airatgaliev.itblogback.dto.GetCategoryDTO;
import com.github.airatgaliev.itblogback.dto.UpdateCategoryDTO;
import com.github.airatgaliev.itblogback.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/categories")
@Tag(name = "Categories", description = "API for blog categories")
public class CategoryController {

  private final CategoryService categoryService;

  @GetMapping
  @Operation(summary = "Get all categories")
  public ResponseEntity<List<GetCategoryDTO>> getAllCategories() {
    List<GetCategoryDTO> categories = categoryService.getAllCategories();
    return ResponseEntity.ok(categories);
  }

  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/{id}")
  @Operation(summary = "Get post by id")
  public ResponseEntity<GetCategoryDTO> getCategoryById(@PathVariable Long id) {
    return categoryService.getCategoryById(id)
        .map(post -> new ResponseEntity<>(post, HttpStatus.OK))
        .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  @PostMapping
  @Operation(summary = "Create a new category")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<String> createCategory(@Valid @RequestBody CreateCategoryDTO categoryDTO) {
    categoryService.createCategory(categoryDTO);
    return new ResponseEntity<>("Category created successfully", HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update a new category")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<String> updateCategory(@PathVariable Long id,
      @Valid @RequestBody UpdateCategoryDTO categoryDTO) {
    categoryService.updateCategory(id, categoryDTO);
    return new ResponseEntity<>("Category updated successfully", HttpStatus.NO_CONTENT);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a category")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<String> deletePost(@PathVariable Long id) {
    categoryService.deleteCategory(id);
    return new ResponseEntity<>("Category deleted successfully", HttpStatus.NO_CONTENT);
  }
}
