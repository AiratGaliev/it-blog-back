package com.github.codogma.codogmaback.repository;

import com.github.codogma.codogmaback.model.CompilationModel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CompilationRepository extends JpaRepository<CompilationModel, Long>,
    JpaSpecificationExecutor<CompilationModel> {

  boolean existsByArticles_Id(Long id);

  List<CompilationModel> findTop10ByTitleStartingWithIgnoreCase(String name);
}
