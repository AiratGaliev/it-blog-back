package com.github.airatgaliev.itblogback.repository;

import com.github.airatgaliev.itblogback.model.SubscriptionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionRepository extends JpaRepository<SubscriptionModel, Long> {

}
