package com.github.airatgaliev.itblogback.repository;

import com.github.airatgaliev.itblogback.model.TagModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<TagModel, Long> {

}
