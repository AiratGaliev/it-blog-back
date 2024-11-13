package com.github.codogma.codogmaback.model;

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
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "categories")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryModel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(nullable = false)
  private Long id;
  @ElementCollection
  @CollectionTable(name = "category_localized_names", joinColumns = @JoinColumn(name = "category_id"))
  @MapKeyEnumerated(EnumType.STRING)
  @MapKeyColumn(name = "language")
  @Column(unique = true, nullable = false)
  private Map<Language, String> name = new HashMap<>();
  @ElementCollection
  @CollectionTable(name = "category_localized_descriptions", joinColumns = @JoinColumn(name = "category_id"))
  @MapKeyEnumerated(EnumType.STRING)
  @MapKeyColumn(name = "language")
  private Map<Language, String> description = new HashMap<>();
  @Column(name = "image_url")
  private String imageUrl;
  @ManyToMany(fetch = FetchType.LAZY, mappedBy = "categories")
  private List<ArticleModel> articles = new ArrayList<>();
}