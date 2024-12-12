package com.github.codogma.codogmaback.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "article_views", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "article_id"})})
public class ArticleView {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @JoinColumn(name = "user_id", nullable = false)
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private UserModel user;
  @JoinColumn(name = "article_id", nullable = false)
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private ArticleModel article;
  @CreationTimestamp
  @Column(nullable = false, updatable = false, name = "created_at")
  private Date createdAt;
  @UpdateTimestamp
  @Column(name = "updated_at")
  private Date updatedAt;
}
