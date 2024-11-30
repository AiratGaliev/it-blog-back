package com.github.codogma.codogmaback.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.MapKeyEnumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;

@Data
@Entity
@Builder
@Indexed
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "categories")
public class CategoryModel {

  @Id
  @Column(nullable = false)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @FullTextField(name = "name", analyzer = "standard")
  @ElementCollection
  @MapKeyColumn(name = "language")
  @MapKeyEnumerated(EnumType.STRING)
  @CollectionTable(name = "category_localized_names", joinColumns = @JoinColumn(name = "category_id"))
  @Column(unique = true, nullable = false)
  private Map<Language, String> name = new HashMap<>();
  @FullTextField
  @ElementCollection
  @MapKeyColumn(name = "language")
  @MapKeyEnumerated(EnumType.STRING)
  @CollectionTable(name = "category_localized_descriptions", joinColumns = @JoinColumn(name = "category_id"))
  private Map<Language, String> description = new HashMap<>();
  @Column(name = "image_url")
  private String imageUrl;
  @ManyToMany(fetch = FetchType.LAZY, mappedBy = "categories")
  private List<ArticleModel> articles = new ArrayList<>();
  @OneToMany(mappedBy = "category", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  private List<FavoriteModel> favorites = new ArrayList<>();
  @CreationTimestamp
  @Column(nullable = false, updatable = false, name = "created_at")
  private Date createdAt;
  @UpdateTimestamp
  @Column(name = "updated_at")
  private Date updatedAt;
}