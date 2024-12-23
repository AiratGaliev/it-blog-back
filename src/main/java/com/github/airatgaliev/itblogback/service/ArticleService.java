package com.github.airatgaliev.itblogback.service;

import static com.github.airatgaliev.itblogback.util.ContentUtil.createHtmlPreview;

import com.github.airatgaliev.itblogback.dto.CreateDraftArticle;
import com.github.airatgaliev.itblogback.dto.GetArticle;
import com.github.airatgaliev.itblogback.dto.GetCategory;
import com.github.airatgaliev.itblogback.dto.GetTag;
import com.github.airatgaliev.itblogback.dto.UpdateArticle;
import com.github.airatgaliev.itblogback.dto.UpdateDraftArticle;
import com.github.airatgaliev.itblogback.exception.ArticleNotFoundException;
import com.github.airatgaliev.itblogback.exception.BookmarkAlreadyExistsException;
import com.github.airatgaliev.itblogback.interceptor.localization.LocalizationContext;
import com.github.airatgaliev.itblogback.model.ArticleModel;
import com.github.airatgaliev.itblogback.model.BookmarkModel;
import com.github.airatgaliev.itblogback.model.CategoryModel;
import com.github.airatgaliev.itblogback.model.Language;
import com.github.airatgaliev.itblogback.model.Role;
import com.github.airatgaliev.itblogback.model.Status;
import com.github.airatgaliev.itblogback.model.TagModel;
import com.github.airatgaliev.itblogback.model.UserModel;
import com.github.airatgaliev.itblogback.repository.ArticleRepository;
import com.github.airatgaliev.itblogback.repository.BookmarkRepository;
import com.github.airatgaliev.itblogback.repository.CategoryRepository;
import com.github.airatgaliev.itblogback.repository.TagRepository;
import com.github.airatgaliev.itblogback.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Date;
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
  private final BookmarkRepository bookmarkRepository;
  private final LocalizationContext localizationContext;

  @Value("${search.results.limit}")
  private int searchResultsLimit;
  @Value("${search.massindexer.threads}")
  private int searchMassIndexerThreads;

  @Transactional
  public Page<GetArticle> getArticles(Specification<ArticleModel> spec, Pageable pageable) {
    return getArticles(pageable, spec);
  }

  @EventListener(ContextRefreshedEvent.class)
  @Transactional
  public void onApplicationEvent(ContextRefreshedEvent event) {
    initializeSearchIndexing();
  }

  @Transactional
  public void initializeSearchIndexing() {
    SearchSession searchSession = Search.session(entityManager);
    searchSession.massIndexer(ArticleModel.class).threadsToLoadObjects(searchMassIndexerThreads)
        .start().thenRun(() -> log.info("Indexing completed successfully.")).exceptionally(e -> {
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
    return getArticles(pageable, combinedSpec);
  }

  private Page<GetArticle> getArticles(Pageable pageable,
      Specification<ArticleModel> combinedSpec) {
    return articleRepository.findAll(combinedSpec, pageable)
        .map(this::convertArticleModelToDTO)
        .map(article -> {
          if (article.getPreviewContent().isEmpty()) {
            String previewContent = createHtmlPreview(article.getContent(), 1100);
            article.setPreviewContent(previewContent);
          } else {
            article.setPreviewContent(article.getPreviewContent());
          }
          article.setContent(null);
          return article;
        });
  }

  @Transactional
  public List<GetArticle> getDraftArticles(UserDetails userDetails) {
    UserModel userModel = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(
        () -> new UsernameNotFoundException("User not found " + userDetails.getUsername()));
    return articleRepository.findAllByUserAndStatus(userModel, Status.DRAFT).stream()
        .map(this::convertArticleModelToDTO).toList();
  }

  private List<Long> searchArticleIdsByContent(String content) {
    SearchSession searchSession = Search.session(entityManager);
    return searchSession.search(ArticleModel.class)
        .where(f -> f.match().fields("content", "title").matching(content).fuzzy(1))
        .fetchHits(searchResultsLimit).stream().map(ArticleModel::getId).toList();
  }

  @Transactional
  public Optional<GetArticle> getArticleById(Long id) {
    return articleRepository.findById(id).map(this::convertArticleModelToDTO);
  }

  @Transactional
  public GetArticle getDraftedArticleById(Long id, UserDetails userDetails) {
    UserModel userModel = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(
        () -> new UsernameNotFoundException("User not found " + userDetails.getUsername()));
    ArticleModel articleModel = articleRepository.findById(id)
        .orElseThrow(() -> new ArticleNotFoundException("Article not found"));
    if (!userModel.getId().equals(articleModel.getUser().getId())) {
      throw new AccessDeniedException("You are not allowed to edit this article");
    }
    articleModel.setStatus(Status.DRAFT);
    ArticleModel savedArticle = articleRepository.save(articleModel);
    return convertArticleModelToDTO(savedArticle);
  }

  @Transactional
  public GetArticle createDraftArticle(CreateDraftArticle draftArticle, UserDetails userDetails) {
    UserModel userModel = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(
        () -> new UsernameNotFoundException("User not found " + userDetails.getUsername()));
    ArticleModel articleModel = ArticleModel.builder().user(userModel)
        .title(draftArticle.getTitle()).content(draftArticle.getContent()).build();
    ArticleModel savedArticle = articleRepository.save(articleModel);
    return convertArticleModelToDTO(savedArticle);
  }

  @Transactional
  public void updateDraftArticle(Long id, UpdateDraftArticle draftArticle,
      UserDetails userDetails) {
    UserModel userModel = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(
        () -> new UsernameNotFoundException("User not found " + userDetails.getUsername()));
    ArticleModel articleModel = articleRepository.findById(id)
        .orElseThrow(() -> new ArticleNotFoundException("Article not found"));
    if (!userModel.getId().equals(articleModel.getUser().getId())) {
      throw new AccessDeniedException("You are not allowed to edit this article");
    }
    articleModel.setStatus(Status.DRAFT);
    Language language = draftArticle.getLanguage();
    if (language != null) {
      articleModel.setLanguage(draftArticle.getLanguage());
    }
    Long originalArticleId = draftArticle.getOriginalArticleId();
    if (originalArticleId != null) {
      articleRepository.findById(originalArticleId)
          .orElseThrow(() -> new ArticleNotFoundException("Original article not found"));
      articleModel.setOriginalArticleId(originalArticleId);
    }
    if (draftArticle.getTitle() != null) {
      articleModel.setTitle(draftArticle.getTitle());
    }
    if (draftArticle.getPreviewContent() != null) {
      articleModel.setPreviewContent(draftArticle.getPreviewContent());
    }
    if (draftArticle.getContent() != null) {
      articleModel.setContent(draftArticle.getContent());
    }
    List<Long> categoryIds = draftArticle.getCategoryIds();
    if (categoryIds != null && !categoryIds.isEmpty()) {
      List<CategoryModel> categories = new ArrayList<>(
          categoryRepository.findAllById(draftArticle.getCategoryIds()));
      articleModel.setCategories(categories);
    }
    List<String> tags = draftArticle.getTags();
    if (tags != null && !tags.isEmpty()) {
      List<TagModel> tagModels = new ArrayList<>();
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
      articleModel.setTags(tagModels);
    }
    articleRepository.save(articleModel);
  }

  @Transactional
  public void publishArticle(Long id, UserDetails userDetails) {
    UserModel userModel = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(
        () -> new UsernameNotFoundException("User not found " + userDetails.getUsername()));
    ArticleModel articleModel = articleRepository.findById(id)
        .orElseThrow(() -> new ArticleNotFoundException("Article not found"));
    Status articleStatus = articleModel.getStatus();
    if (articleStatus != Status.MODERATION && articleStatus != Status.HIDDEN) {
      throw new AccessDeniedException("Publishing not allowed for status: " + articleStatus);
    }
    if (articleStatus == Status.MODERATION && userModel.getRole() != Role.ROLE_ADMIN) {
      throw new AccessDeniedException("Only admins can publish articles under moderation");
    }
    if (articleStatus == Status.HIDDEN && !userModel.getId()
        .equals(articleModel.getUser().getId())) {
      throw new AccessDeniedException("Only the author can publish their hidden article");
    }
    articleModel.setStatus(Status.PUBLISHED);
    articleRepository.save(articleModel);
  }

  @Transactional
  public void hideArticle(Long id, UserDetails userDetails) {
    UserModel userModel = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(
        () -> new UsernameNotFoundException("User not found " + userDetails.getUsername()));
    ArticleModel articleModel = articleRepository.findById(id)
        .orElseThrow(() -> new ArticleNotFoundException("Article not found"));
    Status articleStatus = articleModel.getStatus();
    if (articleStatus != Status.PUBLISHED) {
      throw new AccessDeniedException("Hiding not allowed for status: " + articleStatus);
    }
    if (!userModel.getId().equals(articleModel.getUser().getId())) {
      throw new AccessDeniedException("Only the author can hide their published article");
    }
    articleModel.setStatus(Status.HIDDEN);
    articleRepository.save(articleModel);
  }

  @Transactional
  public void updateArticle(Long id, UpdateArticle updateArticle, UserDetails userDetails) {
    UserModel userModel = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(
        () -> new UsernameNotFoundException("User not found " + userDetails.getUsername()));
    ArticleModel articleModel = articleRepository.findById(id)
        .orElseThrow(() -> new ArticleNotFoundException("Article not found"));
    if (!userModel.getId().equals(articleModel.getUser().getId())) {
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
    Long originalArticleId = updateArticle.getOriginalArticleId();
    if (originalArticleId != null) {
      articleRepository.findById(originalArticleId)
          .orElseThrow(() -> new ArticleNotFoundException("Original article not found"));
      articleModel.setOriginalArticleId(originalArticleId);
    }
    articleModel.setLanguage(updateArticle.getLanguage());
    articleModel.setStatus(Status.MODERATION);
    articleModel.setTitle(updateArticle.getTitle());
    articleModel.setPreviewContent(updateArticle.getPreviewContent());
    articleModel.setContent(updateArticle.getContent());
    articleModel.setCategories(categories);
    articleModel.setTags(tagModels);
    articleModel.setUser(userModel);
    articleRepository.save(articleModel);
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

  @Transactional
  public void bookmark(Long articleId, UserDetails userDetails) {
    UserModel userModel = userRepository.findByUsername(userDetails.getUsername())
        .orElseThrow(() -> new UsernameNotFoundException("Bookmarking user not found"));
    ArticleModel article = articleRepository.findById(articleId).orElseThrow(
        () -> new ArticleNotFoundException("Article with id " + articleId + " not found"));
    boolean bookmarkExists = bookmarkRepository.existsByUserAndArticle(userModel, article);
    if (bookmarkExists) {
      throw new BookmarkAlreadyExistsException("Article already bookmarked");
    }
    BookmarkModel bookmark = BookmarkModel.builder().user(userModel).article(article)
        .createdAt(new Date()).build();
    bookmarkRepository.save(bookmark);
  }

  @Transactional
  public boolean isBookmarked(String bookmarkingUsername, Long articleId) {
    UserModel userModel = userRepository.findByUsername(bookmarkingUsername)
        .orElseThrow(() -> new UsernameNotFoundException("Bookmarking user not found"));
    ArticleModel article = articleRepository.findById(articleId).orElseThrow(
        () -> new ArticleNotFoundException("Article with id " + articleId + " not found"));
    return bookmarkRepository.existsByUserAndArticle(userModel, article);
  }

  @Transactional
  public void unbookmark(String bookmarkingUsername, Long articleId) {
    UserModel userModel = userRepository.findByUsername(bookmarkingUsername)
        .orElseThrow(() -> new UsernameNotFoundException("Bookmarking user not found"));
    ArticleModel article = articleRepository.findById(articleId).orElseThrow(
        () -> new ArticleNotFoundException("Article with id " + articleId + " not found"));
    bookmarkRepository.deleteByUserAndArticle(userModel, article);
  }

  private GetArticle convertArticleModelToDTO(ArticleModel articleModel) {
    ArticleModel originalArticle =
        articleModel.getOriginalArticleId() != null ? articleRepository.findById(
            articleModel.getOriginalArticleId()).orElse(null) : null;
    Language interfaceLanguage = Language.valueOf(localizationContext.getLocale().toUpperCase());
    return GetArticle.builder().id(articleModel.getId()).status(articleModel.getStatus())
        .language(articleModel.getLanguage()).originalArticle(
            originalArticle != null ? GetArticle.builder().id(originalArticle.getId())
                .title(originalArticle.getTitle()).build() : null).title(articleModel.getTitle())
        .previewContent(articleModel.getPreviewContent()).content(articleModel.getContent())
        .username(articleModel.getUser().getUsername())
        .authorAvatarUrl(articleModel.getUser().getAvatarUrl())
        .categories(articleModel.getCategories().stream().map(category -> {
          String localizedCategoryName = category.getName()
              .getOrDefault(interfaceLanguage, category.getName().get(Language.EN));
          return GetCategory.builder().id(category.getId()).name(localizedCategoryName).build();
        }).toList()).tags(articleModel.getTags().stream()
            .map(tagModel -> GetTag.builder().id(tagModel.getId()).name(tagModel.getName()).build())
            .toList()).bookmarksCount(articleModel.getBookmarks().size())
        .createdAt(articleModel.getCreatedAt()).updatedAt(articleModel.getUpdatedAt()).build();
  }
}
