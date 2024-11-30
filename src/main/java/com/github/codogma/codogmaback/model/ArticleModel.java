package com.github.codogma.codogmaback.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;

@Data
@Entity
@Builder
@Indexed
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "articles")
public class ArticleModel {

  @Id
  @Column(nullable = false)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @FullTextField
  @Builder.Default
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Status status = Status.DRAFT;
  @FullTextField
  @Enumerated(EnumType.STRING)
  private Language language;
  private Long originalArticleId;
  @FullTextField
  @Column(nullable = false)
  private String title;
  @FullTextField
  @Column(columnDefinition = "TEXT")
  private String previewContent;
  @FullTextField
  @Column(columnDefinition = "TEXT")
  private String content;
  @IndexedEmbedded
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserModel user;
  @Builder.Default
  @IndexedEmbedded
  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(name = "article_categories", joinColumns = @JoinColumn(name = "article_id"), inverseJoinColumns = @JoinColumn(name = "category_id"))
  private List<CategoryModel> categories = new ArrayList<>();
  @Builder.Default
  @OneToMany(mappedBy = "article", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  private List<BookmarkModel> bookmarks = new ArrayList<>();
  @Builder.Default
  @IndexedEmbedded
  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(name = "article_tags", joinColumns = @JoinColumn(name = "article_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
  private List<TagModel> tags = new ArrayList<>();
  @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("createdAt ASC")
  private List<CommentModel> comments = new ArrayList<>();
  @CreationTimestamp
  @Column(nullable = false, updatable = false, name = "created_at")
  private Date createdAt;
  @UpdateTimestamp
  @Column(name = "updated_at")
  private Date updatedAt;
}