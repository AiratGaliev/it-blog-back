package com.github.airatgaliev.itblogback.repository;

import com.github.airatgaliev.itblogback.model.CategoryModel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryModel, Long> {

  @Query(value = "SELECT c.* FROM categories c "
      + "JOIN article_categories ac ON c.id = ac.category_id "
      + "JOIN articles a ON ac.article_id = a.id " + "WHERE a.user_id = :userId "
      + "GROUP BY c.id", nativeQuery = true)
  List<CategoryModel> findCategoriesByUserId(@Param("userId") Long userId);
}
