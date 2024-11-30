package com.github.codogma.codogmaback.service;

import com.github.codogma.codogmaback.dto.CreateCategory;
import com.github.codogma.codogmaback.dto.GetCategory;
import com.github.codogma.codogmaback.dto.GetTag;
import com.github.codogma.codogmaback.dto.UpdateCategory;
import com.github.codogma.codogmaback.exception.CategoryNotFoundException;
import com.github.codogma.codogmaback.exception.FavoriteAlreadyExistsException;
import com.github.codogma.codogmaback.interceptor.localization.LocalizationContext;
import com.github.codogma.codogmaback.model.CategoryModel;
import com.github.codogma.codogmaback.model.FavoriteModel;
import com.github.codogma.codogmaback.model.Language;
import com.github.codogma.codogmaback.model.TagModel;
import com.github.codogma.codogmaback.model.UserModel;
import com.github.codogma.codogmaback.repository.CategoryRepository;
import com.github.codogma.codogmaback.repository.FavoriteRepository;
import com.github.codogma.codogmaback.repository.TagRepository;
import com.github.codogma.codogmaback.repository.specifications.CategorySpecifications;
import com.github.codogma.codogmaback.util.FileUploadUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

  private final EntityManager entityManager;
  private final CategoryRepository categoryRepository;
  private final TagRepository tagRepository;
  private final FileUploadUtil fileUploadUtil;
  private final LocalizationContext localizationContext;
  private final FavoriteRepository favoriteRepository;

  @Value("${search.results.limit}")
  private int searchResultsLimit;

  @Transactional
  public List<GetCategory> getAllCategories(String order, String sort, int page, int size,
      String tag, String info) {
    Sort.Direction sortDirection = Sort.Direction.fromString(order);
    Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
    List<Long> categoryIds = null;
    if (info != null && !info.isEmpty()) {
      SearchSession searchSession = Search.session(entityManager);
      categoryIds = searchSession.search(CategoryModel.class)
          .where(f -> f.match().fields("name", "description").matching(info).fuzzy(1))
          .fetchHits(searchResultsLimit).stream().map(CategoryModel::getId).toList();
    }
    Specification<CategoryModel> spec = CategorySpecifications.buildSpecification(tag, categoryIds);
    return categoryRepository.findAll(spec, pageable).stream().map(this::convertCategoryToDTO)
        .toList();
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
        .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
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

  @Transactional
  public void addToFavorite(Long id, UserModel userModel) {
    CategoryModel category = categoryRepository.findById(id)
        .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
    boolean favoriteExists = favoriteRepository.existsByUserAndCategory(userModel, category);
    if (favoriteExists) {
      throw new FavoriteAlreadyExistsException("Category is already in favorites");
    }
    FavoriteModel favorite = FavoriteModel.builder().user(userModel).category(category).build();
    favoriteRepository.save(favorite);
  }

  @Transactional
  public boolean isFavorite(Long id, UserModel userModel) {
    CategoryModel category = categoryRepository.findById(id)
        .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
    return favoriteRepository.existsByUserAndCategory(userModel, category);
  }

  @Transactional
  public void unfavorite(Long id, UserModel userModel) {
    CategoryModel category = categoryRepository.findById(id)
        .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
    favoriteRepository.deleteByUserAndCategory(userModel, category);
  }

  private void setLocalizedCategoryFields(CategoryModel category, String name, String description) {
    Language language = localizationContext.getLocale();
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
    Language interfaceLanguage = localizationContext.getLocale();
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
