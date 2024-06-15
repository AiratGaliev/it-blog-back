package com.github.airatgaliev.itblogback.repository;

import com.github.airatgaliev.itblogback.model.PostModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<PostModel, Long> {

}
