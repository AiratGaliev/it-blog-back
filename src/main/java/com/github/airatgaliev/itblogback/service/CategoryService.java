package com.github.airatgaliev.itblogback.service;

import com.github.airatgaliev.itblogback.dto.CreateCategory;
import com.github.airatgaliev.itblogback.dto.GetCategory;
import com.github.airatgaliev.itblogback.dto.GetTag;
import com.github.airatgaliev.itblogback.dto.UpdateCategory;
import com.github.airatgaliev.itblogback.interceptor.localization.LocalizationContext;
import com.github.airatgaliev.itblogback.model.CategoryModel;
import com.github.airatgaliev.itblogback.model.Language;
import com.github.airatgaliev.itblogback.model.TagModel;
import com.github.airatgaliev.itblogback.repository.CategoryRepository;
import com.github.airatgaliev.itblogback.repository.TagRepository;
import com.github.airatgaliev.itblogback.util.FileUploadUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

  private final CategoryRepository categoryRepository;
  private final TagRepository tagRepository;
  private final FileUploadUtil fileUploadUtil;
  private final LocalizationContext localizationContext;

  @Transactional
  public List<GetCategory> getAllCategories() {
    return categoryRepository.findAll().stream().map(this::convertCategoryToDTO).toList();
  }

  @Transactional
  public Optional<GetCategory> getCategoryById(Long id) {
    return categoryRepository.findById(id).map(this::convertCategoryToDTO);
  }

  @Transactional
  public GetCategory createCategory(CreateCategory createCategory) {
    CategoryModel category = new CategoryModel();
    setLocalizedCategoryFields(category, createCategory.getName(), createCategory.getDescription());
    category = categoryRepository.save(category);
    MultipartFile image = createCategory.getImage();
    uploadCategoryImage(image, category);
    category = categoryRepository.save(category);
    return convertCategoryToDTO(category);
  }

  @Transactional
  public void updateCategory(Long id, UpdateCategory updateCategory) {
    CategoryModel category = categoryRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Category not found"));
    setLocalizedCategoryFields(category, updateCategory.getName(), updateCategory.getDescription());
    MultipartFile image = updateCategory.getImage();
    uploadCategoryImage(image, category);
    categoryRepository.save(category);
  }

  @Transactional
  public void deleteCategory(Long id) {
    CategoryModel category = categoryRepository.findById(id)
        .orElseThrow(EntityNotFoundException::new);
    category.getArticles().forEach(article -> article.getCategories().remove(category));
    categoryRepository.delete(category);
  }

  private void setLocalizedCategoryFields(CategoryModel category, String name, String description) {
    Language language = Language.valueOf(localizationContext.getLocale().toUpperCase());
    Optional.ofNullable(name).ifPresent(n -> category.getName().put(language, n));
    Optional.ofNullable(description).ifPresent(d -> category.getDescription().put(language, d));
  }

  private void uploadCategoryImage(MultipartFile image, CategoryModel category) {
    if (image != null && !image.isEmpty()) {
      String imageUrl = fileUploadUtil.uploadCategoryAvatar(image, category.getId());
      category.setImageUrl(imageUrl);
    }
  }

  private GetCategory convertCategoryToDTO(CategoryModel category) {
    List<TagModel> topTags = tagRepository.findTop10TagsByCategoryId(category.getId());
    Language interfaceLanguage = Language.valueOf(localizationContext.getLocale().toUpperCase());
    String localizedCategoryName = getLocalizedValue(category.getName(), interfaceLanguage);
    String localizedCategoryDescription = getLocalizedValue(category.getDescription(),
        interfaceLanguage);
    return GetCategory.builder().id(category.getId()).name(localizedCategoryName)
        .description(localizedCategoryDescription).imageUrl(category.getImageUrl()).tags(
            topTags.stream().map(
                    tagModel -> GetTag.builder().id(tagModel.getId()).name(tagModel.getName()).build())
                .toList()).build();
  }

  private String getLocalizedValue(Map<Language, String> values, Language preferredLanguage) {
    if (values.containsKey(preferredLanguage) && StringUtils.hasText(
        values.get(preferredLanguage))) {
      return values.get(preferredLanguage);
    }
    if (values.containsKey(Language.EN) && StringUtils.hasText(values.get(preferredLanguage))) {
      return values.get(Language.EN);
    }
    return values.values().stream().filter(StringUtils::hasText).findFirst().orElse(null);
  }
}
