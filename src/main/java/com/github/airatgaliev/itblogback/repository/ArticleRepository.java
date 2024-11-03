package com.github.airatgaliev.itblogback.repository;

import com.github.airatgaliev.itblogback.model.ArticleModel;
import com.github.airatgaliev.itblogback.model.Status;
import com.github.airatgaliev.itblogback.model.UserModel;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<ArticleModel, Long>,
    JpaSpecificationExecutor<ArticleModel> {

  Page<ArticleModel> findAllAndByStatus(@Nullable Specification<ArticleModel> spec,
      Pageable pageable, Status status);

  List<ArticleModel> findAllByUserAndStatus(UserModel user, Status status);
}
