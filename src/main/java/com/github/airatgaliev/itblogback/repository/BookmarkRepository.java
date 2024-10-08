package com.github.airatgaliev.itblogback.repository;

import com.github.airatgaliev.itblogback.model.ArticleModel;
import com.github.airatgaliev.itblogback.model.BookmarkModel;
import com.github.airatgaliev.itblogback.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookmarkRepository extends JpaRepository<BookmarkModel, Long> {

  void deleteByUserAndArticle(UserModel user, ArticleModel article);

  boolean existsByUserAndArticle(UserModel user, ArticleModel article);
}
