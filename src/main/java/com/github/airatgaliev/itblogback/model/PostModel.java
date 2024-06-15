package com.github.airatgaliev.itblogback.model;

import jakarta.persistence.Column;
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
@Table(name = "posts")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostModel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false)
  private String title;
  @Column(nullable = false)
  private String content;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private UserModel user;
}
