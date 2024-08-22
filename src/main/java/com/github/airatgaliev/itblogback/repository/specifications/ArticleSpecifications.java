package com.github.airatgaliev.itblogback.repository.specifications;

import com.github.airatgaliev.itblogback.model.ArticleModel;
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

  public static Specification<ArticleModel> hasAuthorId(Long authorId) {
    return (root, query, builder) -> authorId != null ? builder.equal(root.get("user").get("id"),
        authorId) : null;
  }

  public static Specification<ArticleModel> hasContentContaining(String content) {
    return (root, query, builder) -> content != null ? builder.like(
        builder.lower(root.get("content")), "%" + content.toLowerCase() + "%") : null;
  }
}
