package com.github.airatgaliev.itblogback.repository;

import com.github.airatgaliev.itblogback.model.Role;
import com.github.airatgaliev.itblogback.model.UserModel;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserModel, Long> {

  List<UserModel> findAllByRole(Role role);

  Optional<UserModel> findByUsername(String name);

  Optional<UserModel> findByEmail(String username);

  Optional<UserModel> findByUsernameOrEmail(String username, String email);

  void deleteByUsername(String username);

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);
}
