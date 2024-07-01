package com.github.airatgaliev.itblogback.service;

import com.github.airatgaliev.itblogback.dto.CreateCategoryDTO;
import com.github.airatgaliev.itblogback.dto.GetCategoryDTO;
import com.github.airatgaliev.itblogback.dto.UpdateCategoryDTO;
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
  public List<GetCategoryDTO> getAllCategories() {
    return categoryRepository.findAll().stream().map(this::convertCategoryToDTO)
        .collect(Collectors.toList());
  }

  @Transactional
  public Optional<GetCategoryDTO> getCategoryById(Long id) {
    return categoryRepository.findById(id).map(this::convertCategoryToDTO);
  }

  @Transactional
  public void createCategory(CreateCategoryDTO createCategoryDTO) {
    CategoryModel category = new CategoryModel();
    category.setName(createCategoryDTO.getName());
    categoryRepository.save(category);
  }

  @Transactional
  public void updateCategory(Long id, UpdateCategoryDTO updateCategoryDTO) {
    CategoryModel category = categoryRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Category not found"));
    category.setName(updateCategoryDTO.getName());
    categoryRepository.save(category);
  }

  @Transactional
  public void deleteCategory(Long id) {
    categoryRepository.deleteById(id);
  }

  private GetCategoryDTO convertCategoryToDTO(CategoryModel category) {
    return GetCategoryDTO.builder().id(category.getId()).name(category.getName()).build();
  }
}
