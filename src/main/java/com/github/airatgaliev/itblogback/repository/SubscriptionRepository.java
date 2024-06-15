package com.github.airatgaliev.itblogback.repository;

import com.github.airatgaliev.itblogback.model.SubscriptionModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<SubscriptionModel, Long> {

}
