package com.github.airatgaliev.itblogback.service;

import com.github.airatgaliev.itblogback.dto.CreateArticle;
import com.github.airatgaliev.itblogback.dto.GetArticle;
import com.github.airatgaliev.itblogback.dto.GetCategory;
import com.github.airatgaliev.itblogback.dto.GetTag;
import com.github.airatgaliev.itblogback.dto.UpdateArticle;
import com.github.airatgaliev.itblogback.exception.ArticleNotFoundException;
import com.github.airatgaliev.itblogback.model.ArticleModel;
import com.github.airatgaliev.itblogback.model.CategoryModel;
import com.github.airatgaliev.itblogback.model.TagModel;
import com.github.airatgaliev.itblogback.model.UserModel;
import com.github.airatgaliev.itblogback.repository.ArticleRepository;
import com.github.airatgaliev.itblogback.repository.CategoryRepository;
import com.github.airatgaliev.itblogback.repository.TagRepository;
import com.github.airatgaliev.itblogback.repository.UserRepository;
import com.github.airatgaliev.itblogback.util.FileUploadUtil;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleService {

  private final EntityManager entityManager;
  private final ArticleRepository articleRepository;
  private final UserRepository userRepository;
  private final CategoryRepository categoryRepository;
  private final TagRepository tagRepository;
  private final FileUploadUtil fileUploadUtil;

  @Value("${server.servlet.context-path}")
  private String contextPath;
  @Value("${search.results.limit}")
  private int searchResultsLimit;

  @Transactional
  public Page<GetArticle> getArticles(Specification<ArticleModel> spec, Pageable pageable) {
    return articleRepository.findAll(spec, pageable).map(this::convertArticleModelToDTO);
  }

  @EventListener(ContextRefreshedEvent.class)
  @Transactional
  public void onApplicationEvent(ContextRefreshedEvent event) {
    initializeSearchIndexing();
  }

  @Transactional
  public void initializeSearchIndexing() {
    SearchSession searchSession = Search.session(entityManager);
    searchSession.massIndexer(ArticleModel.class).threadsToLoadObjects(5).start()
        .thenRun(() -> log.info("Indexing completed successfully.")).exceptionally(e -> {
          log.error("Error occurred during indexing.", e);
          return null;
        });
  }

  @Transactional
  public Page<GetArticle> searchAndFilterArticles(String content, Specification<ArticleModel> spec,
      Pageable pageable) {
    List<Long> articleIds = searchArticleIdsByContent(content);
    Specification<ArticleModel> combinedSpec = spec.and(
        (root, query, builder) -> root.get("id").in(articleIds));
    return articleRepository.findAll(combinedSpec, pageable).map(this::convertArticleModelToDTO);
  }

  private List<Long> searchArticleIdsByContent(String content) {
    SearchSession searchSession = Search.session(entityManager);
    return searchSession.search(ArticleModel.class)
        .where(f -> f.match().fields("content", "title").matching(content).fuzzy())
        .fetchHits(searchResultsLimit)
        .stream().map(ArticleModel::getId).collect(Collectors.toList());
  }

  @Transactional
  public Optional<GetArticle> getArticleById(Long id) {
    return articleRepository.findById(id).map(this::convertArticleModelToDTO);
  }

  @Transactional
  public GetArticle createArticle(CreateArticle createArticle, UserDetails userDetails) {
    UserModel userModel = userRepository.findByUsername(userDetails.getUsername())
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    List<CategoryModel> categories = new ArrayList<>(
        categoryRepository.findAllById(createArticle.getCategoryIds()));
    List<String> tags = createArticle.getTags();
    List<TagModel> tagModels = new ArrayList<>();
    if (tags != null && !tags.isEmpty()) {
      List<TagModel> existingTags = tagRepository.findAllByNameIgnoreCaseIn(tags);
      Map<String, TagModel> existingTagMap = existingTags.stream()
          .collect(Collectors.toMap(tag -> tag.getName().toLowerCase(), tag -> tag));
      tags.forEach(tag -> {
        TagModel tagModel = existingTagMap.get(tag.toLowerCase());
        if (tagModel == null) {
          tagModel = new TagModel();
          tagModel.setName(tag);
          tagModel = tagRepository.save(tagModel);
        }
        tagModels.add(tagModel);
      });
    }
    ArticleModel articleModel = new ArticleModel();
    articleModel.setTitle(createArticle.getTitle());
    articleModel.setContent(createArticle.getContent());
    articleModel.setCategories(categories);
    articleModel.setTags(tagModels);
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
  public GetArticle updateArticle(Long id, UpdateArticle updateArticle, UserDetails userDetails) {
    UserModel userModel = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(
        () -> new UsernameNotFoundException("User not found " + userDetails.getUsername()));
    ArticleModel articleModel = articleRepository.findById(id)
        .orElseThrow(() -> new ArticleNotFoundException("Article not found"));
    if (!Objects.equals(userModel.getId(), articleModel.getUser().getId())) {
      throw new AccessDeniedException("You are not allowed to update this article");
    }
    List<CategoryModel> categories = new ArrayList<>(
        categoryRepository.findAllById(updateArticle.getCategoryIds()));
    List<String> tags = updateArticle.getTags();
    List<TagModel> tagModels = new ArrayList<>();
    if (tags != null && !tags.isEmpty()) {
      List<TagModel> existingTags = tagRepository.findAllByNameIgnoreCaseIn(tags);
      Map<String, TagModel> existingTagMap = existingTags.stream()
          .collect(Collectors.toMap(tag -> tag.getName().toLowerCase(), tag -> tag));
      tags.forEach(tag -> {
        TagModel tagModel = existingTagMap.get(tag.toLowerCase());
        if (tagModel == null) {
          tagModel = new TagModel();
          tagModel.setName(tag);
          tagModel = tagRepository.save(tagModel);
        }
        tagModels.add(tagModel);
      });
    }
    articleModel.setTitle(updateArticle.getTitle());
    articleModel.setContent(updateArticle.getContent());
    articleModel.setContent(updateArticle.getContent());
    articleModel.setCategories(categories);
    articleModel.setTags(tagModels);
    articleModel.setUser(userModel);
    ArticleModel savedArticle = articleRepository.save(articleModel);
    return convertArticleModelToDTO(savedArticle);
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
                    .name(categoryModel.getName()).build()).collect(Collectors.toList())).tags(
            articleModel.getTags().stream().map(
                    tagModel -> GetTag.builder().id(tagModel.getId()).name(tagModel.getName()).build())
                .collect(Collectors.toList())).createdAt(articleModel.getCreatedAt())
        .updatedAt(articleModel.getUpdatedAt()).build();
  }
}
