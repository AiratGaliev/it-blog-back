package com.github.airatgaliev.itblogback.repository;

import com.github.airatgaliev.itblogback.model.ArticleModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<ArticleModel, Long> {

  Page<ArticleModel> findByCategoriesId(Long categoryId, Pageable pageable);
}
