package com.github.airatgaliev.itblogback.repository;

import com.github.airatgaliev.itblogback.model.PostModel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<PostModel, Long> {

  List<PostModel> findByCategoriesId(Long categoryId);
}
