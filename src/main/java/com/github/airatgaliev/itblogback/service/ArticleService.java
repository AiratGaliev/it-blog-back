package com.github.airatgaliev.itblogback.service;

import com.github.airatgaliev.itblogback.dto.CreateArticle;
import com.github.airatgaliev.itblogback.dto.GetArticle;
import com.github.airatgaliev.itblogback.dto.GetCategory;
import com.github.airatgaliev.itblogback.dto.UpdateArticle;
import com.github.airatgaliev.itblogback.exception.ArticleNotFoundException;
import com.github.airatgaliev.itblogback.model.ArticleModel;
import com.github.airatgaliev.itblogback.model.CategoryModel;
import com.github.airatgaliev.itblogback.model.UserModel;
import com.github.airatgaliev.itblogback.repository.ArticleRepository;
import com.github.airatgaliev.itblogback.repository.CategoryRepository;
import com.github.airatgaliev.itblogback.repository.UserRepository;
import com.github.airatgaliev.itblogback.util.FileUploadUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleService {

  private final ArticleRepository articleRepository;
  private final UserRepository userRepository;
  private final CategoryRepository categoryRepository;
  private final FileUploadUtil fileUploadUtil;

  @Value("${server.servlet.context-path}")
  private String contextPath;

  @Transactional
  public List<GetArticle> getAllArticles() {
    return this.articleRepository.findAll().stream().map(this::convertArticleModelToDTO)
        .collect(Collectors.toList());
  }

  @Transactional
  public List<GetArticle> getArticlesByCategoryId(Long categoryId) {
    return articleRepository.findByCategoriesId(categoryId).stream()
        .map(this::convertArticleModelToDTO).collect(Collectors.toList());
  }

  @Transactional
  public Optional<GetArticle> getArticleById(Long id) {
    return this.articleRepository.findById(id).map(this::convertArticleModelToDTO);
  }

  @Transactional
  public GetArticle createArticle(CreateArticle createArticle, UserDetails userDetails) {
    UserModel userModel = userRepository.findByUsername(userDetails.getUsername())
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    List<CategoryModel> categories = new ArrayList<>(
        categoryRepository.findAllById(createArticle.getCategoryIds()));
    ArticleModel articleModel = new ArticleModel();
    articleModel.setTitle(createArticle.getTitle());
    articleModel.setContent(createArticle.getContent());
    articleModel.setCategories(categories);
    articleModel.setUser(userModel);
    ArticleModel savedArticle = articleRepository.save(articleModel);
    if (createArticle.getImages() != null && !createArticle.getImages().isEmpty()) {
      savedArticle.setImageUrls(createArticle.getImages().stream().map(image -> {
        String filename = fileUploadUtil.uploadArticleImage(image, savedArticle.getId());
        return String.format("%s/articles/images/%s", contextPath, filename);
      }).collect(Collectors.toList()));
      articleRepository.save(savedArticle);
    }
    return convertArticleModelToDTO(savedArticle);
  }

  @Transactional
  public void updateArticle(Long id, UpdateArticle updateArticle, UserDetails userDetails) {
    UserModel userModel = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(
        () -> new UsernameNotFoundException("User not found " + userDetails.getUsername()));
    ArticleModel articleModel = articleRepository.findById(id)
        .orElseThrow(() -> new ArticleNotFoundException("Article not found"));
    List<CategoryModel> categories = new ArrayList<>(
        categoryRepository.findAllById(updateArticle.getCategoryIds()));
    if (Objects.equals(userModel.getId(), articleModel.getUser().getId())) {
      articleModel.setTitle(updateArticle.getTitle());
      articleModel.setContent(updateArticle.getContent());
      articleModel.setContent(updateArticle.getContent());
      articleModel.setCategories(categories);
      articleModel.setUser(userModel);
      articleRepository.save(articleModel);
    } else {
      throw new AccessDeniedException("You are not accessible to update this article");
    }
  }

  @Transactional
  public void deleteArticle(Long id, UserDetails userDetails) {
    UserModel userModel = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(
        () -> new UsernameNotFoundException("User not found " + userDetails.getUsername()));
    ArticleModel articleModel = articleRepository.findById(id)
        .orElseThrow(() -> new ArticleNotFoundException("Article not found"));
    if (Objects.equals(userModel.getId(), articleModel.getUser().getId())) {
      articleRepository.deleteById(id);
    } else {
      throw new AccessDeniedException("You are not accessible to delete this article");
    }
  }

  private GetArticle convertArticleModelToDTO(ArticleModel articleModel) {
    return GetArticle.builder().id(articleModel.getId()).title(articleModel.getTitle())
        .content(articleModel.getContent()).username(articleModel.getUser().getUsername())
        .authorAvatarUrl(articleModel.getUser().getAvatarUrl())
        .imageUrls(articleModel.getImageUrls().stream().toList()).categories(
            articleModel.getCategories().stream().map(
                categoryModel -> GetCategory.builder().id(categoryModel.getId())
                    .name(categoryModel.getName()).build()).collect(Collectors.toList()))
        .createdAt(articleModel.getCreatedAt()).updatedAt(articleModel.getUpdatedAt()).build();
  }
}
