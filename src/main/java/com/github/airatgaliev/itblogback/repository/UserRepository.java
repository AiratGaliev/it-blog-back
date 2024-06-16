package com.github.airatgaliev.itblogback.repository;

import com.github.airatgaliev.itblogback.model.UserModel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserModel, Long> {

  Optional<UserModel> findByUsername(String name);
}
