package com.github.airatgaliev.itblogback.controller;

import com.github.airatgaliev.itblogback.dto.CreateCategory;
import com.github.airatgaliev.itblogback.dto.GetCategory;
import com.github.airatgaliev.itblogback.dto.UpdateCategory;
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
  public ResponseEntity<List<GetCategory>> getAllCategories() {
    List<GetCategory> categories = categoryService.getAllCategories();
    return ResponseEntity.ok(categories);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get post by id")
  public ResponseEntity<GetCategory> getCategoryById(@PathVariable Long id) {
    return categoryService.getCategoryById(id)
        .map(post -> new ResponseEntity<>(post, HttpStatus.OK))
        .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  @PostMapping
  @Operation(summary = "Create a new category")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<GetCategory> createCategory(
      @Valid @RequestBody CreateCategory categoryDTO) {
    GetCategory createdCategory = categoryService.createCategory(categoryDTO);
    return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update a new category")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<String> updateCategory(@PathVariable Long id,
      @Valid @RequestBody UpdateCategory categoryDTO) {
    categoryService.updateCategory(id, categoryDTO);
    return new ResponseEntity<>("Category updated successfully", HttpStatus.NO_CONTENT);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a category")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<String> deleteCategory(@PathVariable Long id) {
    categoryService.deleteCategory(id);
    return new ResponseEntity<>("Category deleted successfully", HttpStatus.NO_CONTENT);
  }
}
