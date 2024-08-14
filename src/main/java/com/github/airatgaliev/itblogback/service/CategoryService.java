package com.github.airatgaliev.itblogback.service;

import com.github.airatgaliev.itblogback.dto.CreateCategory;
import com.github.airatgaliev.itblogback.dto.GetCategory;
import com.github.airatgaliev.itblogback.dto.GetTag;
import com.github.airatgaliev.itblogback.dto.UpdateCategory;
import com.github.airatgaliev.itblogback.model.CategoryModel;
import com.github.airatgaliev.itblogback.model.TagModel;
import com.github.airatgaliev.itblogback.repository.CategoryRepository;
import com.github.airatgaliev.itblogback.repository.TagRepository;
import com.github.airatgaliev.itblogback.util.FileUploadUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

  private final CategoryRepository categoryRepository;
  private final TagRepository tagRepository;
  private final FileUploadUtil fileUploadUtil;

  @Value("${server.servlet.context-path}")
  private String contextPath;

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
  public GetCategory createCategory(CreateCategory createCategory) {
    CategoryModel category = new CategoryModel();
    category.setName(createCategory.getName());
    category.setDescription(createCategory.getDescription());
    CategoryModel savedCategory = categoryRepository.save(category);
    if (createCategory.getImage() != null && !createCategory.getImage().isEmpty()) {
      String imageFilename = fileUploadUtil.uploadCategoryAvatar(createCategory.getImage(),
          savedCategory.getId());
      String imagerUrl = String.format("%s/categories/images/%s", contextPath, imageFilename);
      category.setImageUrl(imagerUrl);
    }
    savedCategory = categoryRepository.save(category);
    return convertCategoryToDTO(savedCategory);
  }

  @Transactional
  public void updateCategory(Long id, UpdateCategory updateCategory) {
    CategoryModel category = categoryRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Category not found"));
    category.setName(updateCategory.getName());
    category.setDescription(updateCategory.getDescription());
    if (updateCategory.getImage() != null && !updateCategory.getImage().isEmpty()) {
      String imageFilename = fileUploadUtil.uploadCategoryAvatar(updateCategory.getImage(),
          category.getId());
      String imagerUrl = String.format("%s/categories/images/%s", contextPath, imageFilename);
      category.setImageUrl(imagerUrl);
    }
    categoryRepository.save(category);
  }

  @Transactional
  public void deleteCategory(Long id) {
    CategoryModel category = categoryRepository.findById(id)
        .orElseThrow(EntityNotFoundException::new);
    category.getArticles().forEach(article -> article.getCategories().remove(category));
    categoryRepository.delete(category);
  }

  private GetCategory convertCategoryToDTO(CategoryModel category) {
    List<TagModel> topTags = tagRepository.findTop10TagsByCategoryId(category.getId());
    return GetCategory.builder().id(category.getId()).name(category.getName())
        .description(category.getDescription()).imageUrl(category.getImageUrl()).tags(
            topTags.stream().map(
                    tagModel -> GetTag.builder().id(tagModel.getId()).name(tagModel.getName()).build())
                .collect(Collectors.toList())).build();
  }
}
