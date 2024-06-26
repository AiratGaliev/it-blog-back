package com.github.airatgaliev.itblogback.repository;

import com.github.airatgaliev.itblogback.model.CategoryModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryModel, Long> {

}
