package com.github.airatgaliev.itblogback.repository;

import com.github.airatgaliev.itblogback.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserModel, Long> {

  UserModel findByUsername(String name);
}
