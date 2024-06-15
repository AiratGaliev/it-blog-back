package com.github.airatgaliev.itblogback.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "subscriptions")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionModel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "subscriber_id")
  private UserModel subscriber;

  @ManyToOne
  @JoinColumn(name = "subscribed_to_id")
  private UserModel subscribedTo;
}
