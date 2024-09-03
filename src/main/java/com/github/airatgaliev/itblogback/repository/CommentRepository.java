package com.github.airatgaliev.itblogback.repository;

import com.github.airatgaliev.itblogback.model.CommentModel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<CommentModel, Long> {

  List<CommentModel> findByArticleIdAndParentCommentIsNullOrderByCreatedAtAsc(Long articleId);
}
