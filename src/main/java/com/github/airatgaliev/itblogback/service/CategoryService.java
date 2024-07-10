package com.github.airatgaliev.itblogback.service;

import com.github.airatgaliev.itblogback.dto.CreateCategory;
import com.github.airatgaliev.itblogback.dto.GetCategory;
import com.github.airatgaliev.itblogback.dto.UpdateCategory;
import com.github.airatgaliev.itblogback.model.CategoryModel;
import com.github.airatgaliev.itblogback.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

  private final CategoryRepository categoryRepository;

  @Transactional
  public List<GetCategory> getAllCategories() {
    return categoryRepository.findAll().stream().map(this::convertCategoryToDTO)
        .collect(Collectors.toList());
  }

  @Transactional
  public Optional<GetCategory> getCategoryById(Long id) {
    return categoryRepository.findById(id).map(this::convertCategoryToDTO);
  }

  @Transactional
  public void createCategory(CreateCategory createCategory) {
    CategoryModel category = new CategoryModel();
    category.setName(createCategory.getName());
    categoryRepository.save(category);
  }

  @Transactional
  public void updateCategory(Long id, UpdateCategory updateCategory) {
    CategoryModel category = categoryRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Category not found"));
    category.setName(updateCategory.getName());
    categoryRepository.save(category);
  }

  @Transactional
  public void deleteCategory(Long id) {
    categoryRepository.deleteById(id);
  }

  private GetCategory convertCategoryToDTO(CategoryModel category) {
    return GetCategory.builder().id(category.getId()).name(category.getName()).build();
  }
}
