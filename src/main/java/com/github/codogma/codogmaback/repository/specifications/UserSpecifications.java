package com.github.codogma.codogmaback.repository.specifications;

import com.github.codogma.codogmaback.dto.UserRole;
import com.github.codogma.codogmaback.model.ArticleModel;
import com.github.codogma.codogmaback.model.CategoryModel;
import com.github.codogma.codogmaback.model.Role;
import com.github.codogma.codogmaback.model.Status;
import com.github.codogma.codogmaback.model.TagModel;
import com.github.codogma.codogmaback.model.UserModel;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecifications {

  public static Specification<UserModel> hasCategoryId(Long categoryId) {
    return (root, query, builder) -> {
      if (categoryId == null) {
        return null;
      }
      Join<UserModel, ArticleModel> articleJoin = root.join("articles", JoinType.INNER);
      Join<ArticleModel, CategoryModel> categoryJoin = articleJoin.join("categories",
          JoinType.INNER);
      return builder.and(builder.equal(categoryJoin.get("id"), categoryId),
          builder.equal(articleJoin.get("status"), Status.PUBLISHED));
    };
  }

  public static Specification<UserModel> hasRole(UserRole role) {
    return (root, query, builder) -> {
      if (role == null) {
        return null;
      }
      return builder.and(builder.equal(root.get("role"), role),
          builder.notEqual(root.get("role"), Role.ROLE_ADMIN));
    };
  }

  public static Specification<UserModel> hasTagName(String tagName) {
    return (root, query, builder) -> {
      if (tagName == null || tagName.isEmpty()) {
        return builder.conjunction();
      }
      Join<UserModel, ArticleModel> articleJoin = root.join("articles", JoinType.INNER);
      Join<ArticleModel, TagModel> tagJoin = articleJoin.join("tags", JoinType.INNER);
      return builder.equal(builder.lower(tagJoin.get("name")), tagName.toLowerCase());
    };
  }

  public static Specification<UserModel> hasInfoMatch(List<Long> usersIds) {
    return (root, query, builder) -> usersIds != null ? root.get("id").in(usersIds) : null;
  }

  public static Specification<UserModel> buildSpecification(Long categoryId, UserRole role,
      String tagName, List<Long> usersIds) {
    return Specification.where(hasCategoryId(categoryId)).and(hasRole(role))
        .and(hasTagName(tagName)).and(hasInfoMatch(usersIds));
  }
}
