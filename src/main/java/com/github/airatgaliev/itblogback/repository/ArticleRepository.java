package com.github.airatgaliev.itblogback.repository;

import com.github.airatgaliev.itblogback.model.ArticleModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<ArticleModel, Long> {

  Page<ArticleModel> findByCategoriesId(Long categoryId, Pageable pageable);

  Page<ArticleModel> findByTagsId(Long tagId, Pageable pageable);

  Page<ArticleModel> findByCategoriesIdAndTagsId(Long categoryId, Long tagId, Pageable pageable);

  Page<ArticleModel> findByCategoriesIdAndTagsIdAndContentContaining(Long categoryId, Long tagId,
      String content, Pageable pageable);

  Page<ArticleModel> findByTagsIdAndContentContaining(Long tagId, String content,
      Pageable pageable);

  Page<ArticleModel> findByCategoriesIdAndContentContaining(Long categoryId, String content,
      Pageable pageable);

  Page<ArticleModel> findByContentContaining(String content, Pageable pageable);
}
