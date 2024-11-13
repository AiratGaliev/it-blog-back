package com.github.codogma.codogmaback.repository;

import com.github.codogma.codogmaback.model.SubscriptionModel;
import com.github.codogma.codogmaback.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionRepository extends JpaRepository<SubscriptionModel, Long> {

  boolean existsBySubscriberAndUser(UserModel subscriber, UserModel user);

  void deleteBySubscriberAndUser(UserModel subscriber, UserModel user);
}
