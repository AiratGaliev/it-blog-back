package com.github.airatgaliev.itblogback.repository;

import com.github.airatgaliev.itblogback.model.Role;
import com.github.airatgaliev.itblogback.model.UserModel;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserModel, Long> {

  List<UserModel> findAllByRoleIsNot(Role role);

  List<UserModel> findAllByRoleAndRoleIsNot(Role role, Role role2);

  @Query("SELECT DISTINCT u FROM UserModel u " + "JOIN u.articles a " + "JOIN a.categories c "
      + "WHERE c.id = :categoryId")
  List<UserModel> findAuthorsByCategoryId(@Param("categoryId") Long categoryId);

  Optional<UserModel> findByUsername(String name);

  Optional<UserModel> findByEmail(String username);

  Optional<UserModel> findByUsernameOrEmail(String username, String email);

  void deleteByUsername(String username);

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);
}
