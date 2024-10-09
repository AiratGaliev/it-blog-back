package com.github.airatgaliev.itblogback.repository.specifications;

import com.github.airatgaliev.itblogback.model.ArticleModel;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class ArticleSpecifications {

  public static Specification<ArticleModel> hasCategoryId(Long categoryId) {
    return (root, query, builder) -> categoryId != null ? builder.equal(
        root.join("categories").get("id"), categoryId) : null;
  }

  public static Specification<ArticleModel> hasTagName(String tagName) {
    return (root, query, builder) -> tagName != null ? builder.like(
        builder.lower(root.join("tags").get("name")), "%" + tagName.toLowerCase() + "%") : null;
  }

  public static Specification<ArticleModel> hasUsername(String username) {
    return (root, query, builder) -> username != null ? builder.equal(
        root.get("user").get("username"), username) : null;
  }

  public static Specification<ArticleModel> hasSupportedLanguage(List<String> supportedLanguages) {
    return (root, query, builder) -> root.get("language").in(supportedLanguages);
  }
}
