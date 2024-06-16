package com.github.airatgaliev.itblogback.repository;

import com.github.airatgaliev.itblogback.model.ERole;
import com.github.airatgaliev.itblogback.model.RoleModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<RoleModel, Long> {

  RoleModel findByRole(ERole ERole);
}