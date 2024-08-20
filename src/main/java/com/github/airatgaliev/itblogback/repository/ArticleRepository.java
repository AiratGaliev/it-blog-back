package com.github.airatgaliev.itblogback.repository;

import com.github.airatgaliev.itblogback.model.ArticleModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<ArticleModel, Long> {

  Page<ArticleModel> findByCategoriesId(Long categoryId, Pageable pageable);

  Page<ArticleModel> findByTagsNameIgnoreCase(String tagName, Pageable pageable);

  Page<ArticleModel> findByCategoriesIdAndTagsNameIgnoreCase(Long categoryId, String tagName,
      Pageable pageable);

  Page<ArticleModel> findByCategoriesIdAndTagsNameIgnoreCaseAndContentContaining(Long categoryId,
      String tagName, String content, Pageable pageable);

  Page<ArticleModel> findByTagsNameIgnoreCaseAndContentContaining(String tagName, String content,
      Pageable pageable);

  Page<ArticleModel> findByCategoriesIdAndContentContaining(Long categoryId, String content,
      Pageable pageable);

  Page<ArticleModel> findByContentContaining(String content, Pageable pageable);
}
