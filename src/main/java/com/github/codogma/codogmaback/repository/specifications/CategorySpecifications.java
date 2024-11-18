package com.github.codogma.codogmaback.repository.specifications;

import com.github.codogma.codogmaback.model.ArticleModel;
import com.github.codogma.codogmaback.model.CategoryModel;
import com.github.codogma.codogmaback.model.TagModel;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class CategorySpecifications {

  public static Specification<CategoryModel> hasTagName(String tagName) {
    return (root, query, builder) -> {
      if (tagName == null || tagName.isEmpty()) {
        return builder.conjunction();
      }
      Join<CategoryModel, ArticleModel> articleJoin = root.join("articles", JoinType.INNER);
      Join<ArticleModel, TagModel> tagJoin = articleJoin.join("tags", JoinType.INNER);
      return builder.equal(builder.lower(tagJoin.get("name")), tagName.toLowerCase());
    };
  }

  public static Specification<CategoryModel> hasInfoMatch(List<Long> categoryIds) {
    return (root, query, builder) -> categoryIds != null ? root.get("id").in(categoryIds) : null;
  }

  public static Specification<CategoryModel> buildSpecification(String tagName,
      List<Long> categoryIds) {
    return Specification.where(hasTagName(tagName)).and(hasInfoMatch(categoryIds));
  }
}
