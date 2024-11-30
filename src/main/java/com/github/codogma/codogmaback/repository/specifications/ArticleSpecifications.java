package com.github.codogma.codogmaback.repository.specifications;

import com.github.codogma.codogmaback.model.ArticleModel;
import com.github.codogma.codogmaback.model.FavoriteModel;
import com.github.codogma.codogmaback.model.Language;
import com.github.codogma.codogmaback.model.Role;
import com.github.codogma.codogmaback.model.Status;
import com.github.codogma.codogmaback.model.SubscriptionModel;
import com.github.codogma.codogmaback.model.UserModel;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class ArticleSpecifications {

  public static Specification<ArticleModel> hasAccess(UserModel userModel) {
    return (root, query, builder) -> {
      if (userModel != null) {
        Role role = userModel.getRole();
        if (role.equals(Role.ROLE_ADMIN)) {
          return builder.conjunction();
        } else if (role.equals(Role.ROLE_AUTHOR)) {
          return builder.and(builder.or(builder.equal(root.get("status"), Status.PUBLISHED),
                  builder.equal(root.get("user").get("username"), userModel.getUsername())),
              builder.equal(root.get("user").get("role"), Role.ROLE_AUTHOR));
        }
      }
      return builder.and(builder.equal(root.get("status"), Status.PUBLISHED),
          builder.equal(root.get("user").get("role"), Role.ROLE_AUTHOR));
    };
  }

  public static Specification<ArticleModel> hasFavoriteCategories(UserModel userModel) {
    return (root, query, builder) -> {
      if (userModel != null) {
        List<FavoriteModel> favorites = userModel.getFavorites();
        if (favorites != null && !favorites.isEmpty()) {
          List<Long> favoriteCategoriesIds = favorites.stream()
              .map(favorite -> favorite.getCategory().getId()).toList();
          return root.join("categories").get("id").in(favoriteCategoriesIds);
        }
      }
      return builder.disjunction();
    };
  }

  public static Specification<ArticleModel> hasSubscribedArticles(UserModel userModel) {
    return (root, query, builder) -> {
      if (userModel != null) {
        List<SubscriptionModel> subscriptions = userModel.getSubscriptions();
        if (subscriptions != null && !subscriptions.isEmpty()) {
          List<Long> subscribedUserIds = subscriptions.stream().map(sub -> sub.getUser().getId())
              .toList();
          return root.get("user").get("id").in(subscribedUserIds);
        }
      }
      return builder.disjunction();
    };
  }

  public static Specification<ArticleModel> hasCategoryId(Long categoryId) {
    return (root, query, builder) -> categoryId != null ? builder.equal(
        root.join("categories").get("id"), categoryId) : null;
  }

  public static Specification<ArticleModel> hasContentMatch(List<Long> articleIds) {
    return (root, query, builder) -> articleIds != null ? root.get("id").in(articleIds) : null;
  }

  public static Specification<ArticleModel> hasSupportedLanguage(
      List<Language> supportedLanguages) {
    return (root, query, builder) -> root.get("language").in(supportedLanguages);
  }

  public static Specification<ArticleModel> hasTagName(String tagName) {
    return (root, query, builder) -> {
      if (tagName == null || tagName.isEmpty()) {
        return builder.conjunction();
      }
      return builder.equal(builder.lower(root.join("tags").get("name")), tagName.toLowerCase());
    };
  }

  public static Specification<ArticleModel> hasUsername(String username) {
    return (root, query, builder) -> username != null ? builder.equal(
        root.get("user").get("username"), username) : null;
  }

  public static Specification<ArticleModel> buildSpecification(Long categoryId, String tagName,
      String username, List<Language> supportedLanguages, Boolean isFeed, UserModel userModel,
      List<Long> articleIds) {
    Specification<ArticleModel> spec = Specification.where(hasCategoryId(categoryId))
        .and(hasTagName(tagName)).and(hasUsername(username))
        .and(hasSupportedLanguage(supportedLanguages)).and(hasAccess(userModel))
        .and(hasContentMatch(articleIds));
    if (Boolean.TRUE.equals(isFeed)) {
      spec = spec.and(hasFavoriteCategories(userModel)).or(hasSubscribedArticles(userModel));
    }
    return spec;
  }
}
