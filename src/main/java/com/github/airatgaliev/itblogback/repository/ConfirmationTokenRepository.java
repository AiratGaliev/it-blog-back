package com.github.airatgaliev.itblogback.repository;

import com.github.airatgaliev.itblogback.model.ConfirmationToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationToken, Long> {

  Optional<ConfirmationToken> findByToken(String token);
}
