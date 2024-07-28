package com.github.airatgaliev.itblogback.repository;

import com.github.airatgaliev.itblogback.model.ArticleModel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<ArticleModel, Long> {

  List<ArticleModel> findByCategoriesId(Long categoryId);
}
