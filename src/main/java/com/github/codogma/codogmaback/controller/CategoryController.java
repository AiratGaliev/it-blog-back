package com.github.codogma.codogmaback.controller;

import com.github.codogma.codogmaback.dto.CreateCategory;
import com.github.codogma.codogmaback.dto.GetCategory;
import com.github.codogma.codogmaback.dto.UpdateCategory;
import com.github.codogma.codogmaback.model.UserModel;
import com.github.codogma.codogmaback.service.CategoryService;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
@Tag(name = "Categories", description = "API for blog categories")
public class CategoryController {

  private final CategoryService categoryService;

  @GetMapping
  @Operation(summary = "Get all categories")
  @Parameters({@Parameter(name = "tag", description = "Tag to filter categories"),
      @Parameter(name = "info", description = "Information to filter categories"),
      @Parameter(name = "page", description = "Page number to retrieve"),
      @Parameter(name = "size", description = "Number of categories per page"),
      @Parameter(name = "sort", description = "Field to sort by"),
      @Parameter(name = "order", description = "Order direction, either 'asc' or 'desc'")})
  public ResponseEntity<List<GetCategory>> getAllCategories(
      @RequestParam(required = false) String tag, @RequestParam(required = false) String info,
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "name") String sort,
      @RequestParam(defaultValue = "asc") String order) {
    List<GetCategory> categories = categoryService.getAllCategories(order, sort, page, size, tag,
        info);
    return ResponseEntity.ok(categories);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get article by id")
  public ResponseEntity<GetCategory> getCategoryById(@PathVariable Long id) {
    return categoryService.getCategoryById(id)
        .map(article -> new ResponseEntity<>(article, HttpStatus.OK))
        .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Create a new category")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<GetCategory> createCategory(
      @Valid @ModelAttribute CreateCategory createCategoryDTO,
      @RequestParam(value = "image", required = false) MultipartFile image) {
    createCategoryDTO.setImage(image);
    GetCategory createdCategory = categoryService.createCategory(createCategoryDTO);
    return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
  }

  @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Update a new category")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<String> updateCategory(@PathVariable Long id,
      @Valid @ModelAttribute UpdateCategory updateCategoryDTO,
      @RequestParam(value = "image", required = false) MultipartFile image) {
    updateCategoryDTO.setImage(image);
    categoryService.updateCategory(id, updateCategoryDTO);
    return new ResponseEntity<>("Category updated successfully", HttpStatus.OK);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a category")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<String> deleteCategory(@PathVariable Long id) {
    categoryService.deleteCategory(id);
    return new ResponseEntity<>("Category deleted successfully", HttpStatus.NO_CONTENT);
  }

  @PostMapping("/{id}/add-to-favorites")
  @Operation(summary = "Add category to favorites")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_AUTHOR')")
  public ResponseEntity<Void> addToFavorites(@PathVariable Long id,
      @AuthenticationPrincipal UserModel userModel) {
    categoryService.addToFavorite(id, userModel);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}/is-favorite")
  @Operation(summary = "Check if the user has favorite the category to bookmarks")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_AUTHOR')")
  public ResponseEntity<Boolean> isFavorite(@PathVariable Long id,
      @AuthenticationPrincipal UserModel userModel) {
    boolean isFavorite = categoryService.isFavorite(id, userModel);
    return ResponseEntity.ok(isFavorite);
  }

  @DeleteMapping("/{id}/unfavorite")
  @Operation(summary = "Unfavorite an category")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_AUTHOR')")
  public ResponseEntity<Void> unfavorite(@PathVariable Long id,
      @AuthenticationPrincipal UserModel userModel) {
    categoryService.unfavorite(id, userModel);
    return ResponseEntity.noContent().build();
  }
}