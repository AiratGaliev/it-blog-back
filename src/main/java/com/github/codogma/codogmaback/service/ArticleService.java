package com.github.codogma.codogmaback.service;

import static com.github.codogma.codogmaback.util.ContentUtil.createHtmlPreview;

import com.github.codogma.codogmaback.dto.CreateDraftArticle;
import com.github.codogma.codogmaback.dto.GetArticle;
import com.github.codogma.codogmaback.dto.GetCategory;
import com.github.codogma.codogmaback.dto.GetTag;
import com.github.codogma.codogmaback.dto.UpdateArticle;
import com.github.codogma.codogmaback.dto.UpdateDraftArticle;
import com.github.codogma.codogmaback.exception.ArticleNotFoundException;
import com.github.codogma.codogmaback.exception.BookmarkAlreadyExistsException;
import com.github.codogma.codogmaback.interceptor.localization.LocalizationContext;
import com.github.codogma.codogmaback.model.ArticleModel;
import com.github.codogma.codogmaback.model.BookmarkModel;
import com.github.codogma.codogmaback.model.CategoryModel;
import com.github.codogma.codogmaback.model.Language;
import com.github.codogma.codogmaback.model.Role;
import com.github.codogma.codogmaback.model.Status;
import com.github.codogma.codogmaback.model.TagModel;
import com.github.codogma.codogmaback.model.UserModel;
import com.github.codogma.codogmaback.repository.ArticleRepository;
import com.github.codogma.codogmaback.repository.BookmarkRepository;
import com.github.codogma.codogmaback.repository.CategoryRepository;
import com.github.codogma.codogmaback.repository.TagRepository;
import com.github.codogma.codogmaback.repository.UserRepository;
import com.github.codogma.codogmaback.repository.specifications.ArticleSpecifications;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleService {

  private final EntityManager entityManager;
  private final UserRepository userRepository;
  private final ArticleRepository articleRepository;
  private final CategoryRepository categoryRepository;
  private final TagRepository tagRepository;
  private final BookmarkRepository bookmarkRepository;
  private final LocalizationContext localizationContext;

  @Value("${search.results.limit}")
  private int searchResultsLimit;

  @Transactional
  public Page<GetArticle> getArticles(String order, String sort, int page, int size,
      Long categoryId, String tag, String username, Boolean isFeed, UserModel userModel,
      String content) {
    UserModel foundUser = userModel != null ? userRepository.findById(userModel.getId())
        .orElseThrow(() -> new UsernameNotFoundException("User not found")) : null;
    Sort.Direction sortDirection = Sort.Direction.fromString(order);
    Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
    List<String> supportedLanguages = localizationContext.getSupportedLanguages();
    List<Long> articleIds = null;
    if (content != null && !content.isEmpty()) {
      SearchSession searchSession = Search.session(entityManager);
      articleIds = searchSession.search(ArticleModel.class)
          .where(f -> f.match().fields("content", "title").matching(content).fuzzy(1))
          .fetchHits(searchResultsLimit).stream().map(ArticleModel::getId).toList();
    }
    Specification<ArticleModel> spec = ArticleSpecifications.buildSpecification(categoryId, tag,
        username, supportedLanguages, isFeed, foundUser, articleIds);
    return articleRepository.findAll(spec, pageable).map(this::convertArticleModelToDTO)
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
  public List<GetArticle> getDraftArticles(UserModel userModel) {
    return articleRepository.findAllByUserAndStatus(userModel, Status.DRAFT).stream()
        .map(this::convertArticleModelToDTO).toList();
  }

  @Transactional
  public GetArticle getArticleById(Long id, UserModel userModel) {
    ArticleModel articleModel = articleRepository.findById(id)
        .orElseThrow(() -> new ArticleNotFoundException("Article not found"));
    Status articleStatus = articleModel.getStatus();
    if (articleStatus != Status.PUBLISHED && userModel == null) {
      throw new ArticleNotFoundException("Article not found");
    }
    if (!articleStatus.equals(Status.PUBLISHED) && !articleModel.getUser().getUsername()
        .equals(userModel.getUsername()) && !userModel.getRole().equals(Role.ROLE_ADMIN)) {
      throw new ArticleNotFoundException("Article not found");
    }
    return convertArticleModelToDTO(articleModel);
  }

  @Transactional
  public GetArticle getDraftedArticleById(Long id, UserModel userModel) {
    ArticleModel articleModel = articleRepository.findById(id)
        .orElseThrow(() -> new ArticleNotFoundException("Article not found"));
    if (!userModel.getId().equals(articleModel.getUser().getId())) {
      throw new AccessDeniedException("You are not allowed to edit this article");
    }
    Status articleStatus = articleModel.getStatus();
    if (articleStatus == Status.BLOCKED) {
      throw new AccessDeniedException("Editing not allowed for this article");
    }
    articleModel.setStatus(Status.DRAFT);
    ArticleModel savedArticle = articleRepository.save(articleModel);
    return convertArticleModelToDTO(savedArticle);
  }

  @Transactional
  public GetArticle createDraftArticle(CreateDraftArticle draftArticle, UserModel userModel) {
    ArticleModel articleModel = ArticleModel.builder().user(userModel)
        .title(draftArticle.getTitle()).content(draftArticle.getContent()).build();
    ArticleModel savedArticle = articleRepository.save(articleModel);
    return convertArticleModelToDTO(savedArticle);
  }

  @Transactional
  public void updateDraftArticle(Long id, UpdateDraftArticle draftArticle, UserModel userModel) {
    ArticleModel articleModel = articleRepository.findById(id)
        .orElseThrow(() -> new ArticleNotFoundException("Article not found"));
    if (!userModel.getId().equals(articleModel.getUser().getId())) {
      throw new AccessDeniedException("You are not allowed to edit this article");
    }
    Status articleStatus = articleModel.getStatus();
    if (articleStatus != Status.DRAFT) {
      throw new AccessDeniedException("Editing not allowed for this article");
    }
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
  public void publishArticle(Long id, UserModel userModel) {
    ArticleModel articleModel = articleRepository.findById(id)
        .orElseThrow(() -> new ArticleNotFoundException("Article not found"));
    Status articleStatus = articleModel.getStatus();
    if (articleStatus == Status.DRAFT || articleStatus == Status.BLOCKED) {
      throw new AccessDeniedException("Publishing not allowed for this article");
    }
    if ((articleStatus == Status.MODERATION) && userModel.getRole() != Role.ROLE_ADMIN) {
      throw new AccessDeniedException("Only admins can publish this article");
    }
    if (articleStatus == Status.HIDDEN && !userModel.getId()
        .equals(articleModel.getUser().getId())) {
      throw new AccessDeniedException("Only the author can publish their hidden article");
    }
    articleModel.setStatus(Status.PUBLISHED);
    articleRepository.save(articleModel);
  }

  @Transactional
  public void hideArticle(Long id, UserModel userModel) {
    ArticleModel articleModel = articleRepository.findById(id)
        .orElseThrow(() -> new ArticleNotFoundException("Article not found"));
    if (!userModel.getId().equals(articleModel.getUser().getId())) {
      throw new AccessDeniedException("Only the author can hide their published article");
    }
    Status articleStatus = articleModel.getStatus();
    if (articleStatus != Status.PUBLISHED) {
      throw new AccessDeniedException("Hiding not allowed for status: " + articleStatus);
    }
    articleModel.setStatus(Status.HIDDEN);
    articleRepository.save(articleModel);
  }

  @Transactional
  public void blockArticle(Long id) {
    ArticleModel articleModel = articleRepository.findById(id)
        .orElseThrow(() -> new ArticleNotFoundException("Article not found"));
    Status articleStatus = articleModel.getStatus();
    if (articleStatus == Status.DRAFT) {
      throw new AccessDeniedException("Blocking not allowed for draft article");
    }
    articleModel.setStatus(Status.BLOCKED);
    articleRepository.save(articleModel);
  }

  @Transactional
  public void unblockArticle(Long id) {
    ArticleModel articleModel = articleRepository.findById(id)
        .orElseThrow(() -> new ArticleNotFoundException("Article not found"));
    Status articleStatus = articleModel.getStatus();
    if (articleStatus != Status.BLOCKED) {
      throw new AccessDeniedException("Unblocking not allowed for this article");
    }
    articleModel.setStatus(Status.DRAFT);
    articleRepository.save(articleModel);
  }

  @Transactional
  public void updateArticle(Long id, UpdateArticle updateArticle, UserModel userModel) {
    ArticleModel articleModel = articleRepository.findById(id)
        .orElseThrow(() -> new ArticleNotFoundException("Article not found"));
    if (!userModel.getId().equals(articleModel.getUser().getId())) {
      throw new AccessDeniedException("You are not allowed to update this article");
    }
    Status articleStatus = articleModel.getStatus();
    if (articleStatus != Status.DRAFT) {
      throw new AccessDeniedException("Editing not allowed for this article");
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
  public void deleteArticle(Long id, UserModel userModel) {
    ArticleModel articleModel = articleRepository.findById(id)
        .orElseThrow(() -> new ArticleNotFoundException("Article not found"));
    if (Objects.equals(userModel.getId(), articleModel.getUser().getId())) {
      articleRepository.deleteById(id);
    } else {
      throw new AccessDeniedException("You are not accessible to delete this article");
    }
  }

  @Transactional
  public void bookmark(Long articleId, UserModel userModel) {
    ArticleModel article = articleRepository.findById(articleId).orElseThrow(
        () -> new ArticleNotFoundException("Article with id " + articleId + " not found"));
    boolean bookmarkExists = bookmarkRepository.existsByUserAndArticle(userModel, article);
    if (bookmarkExists) {
      throw new BookmarkAlreadyExistsException("Article already bookmarked");
    }
    BookmarkModel bookmark = BookmarkModel.builder().user(userModel).article(article).build();
    bookmarkRepository.save(bookmark);
  }

  @Transactional
  public boolean isBookmarked(Long articleId, UserModel userModel) {
    ArticleModel article = articleRepository.findById(articleId).orElseThrow(
        () -> new ArticleNotFoundException("Article with id " + articleId + " not found"));
    return bookmarkRepository.existsByUserAndArticle(userModel, article);
  }

  @Transactional
  public void unbookmark(Long articleId, UserModel userModel) {
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
