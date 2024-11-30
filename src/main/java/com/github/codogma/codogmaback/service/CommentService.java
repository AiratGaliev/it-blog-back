package com.github.codogma.codogmaback.service;

import com.github.codogma.codogmaback.dto.CreateComment;
import com.github.codogma.codogmaback.dto.GetArticle;
import com.github.codogma.codogmaback.dto.GetComment;
import com.github.codogma.codogmaback.dto.GetUser;
import com.github.codogma.codogmaback.dto.UpdateComment;
import com.github.codogma.codogmaback.exception.ArticleNotFoundException;
import com.github.codogma.codogmaback.exception.CommentNotFoundException;
import com.github.codogma.codogmaback.model.ArticleModel;
import com.github.codogma.codogmaback.model.CommentModel;
import com.github.codogma.codogmaback.model.Role;
import com.github.codogma.codogmaback.model.Status;
import com.github.codogma.codogmaback.model.UserModel;
import com.github.codogma.codogmaback.repository.ArticleRepository;
import com.github.codogma.codogmaback.repository.CommentRepository;
import com.github.codogma.codogmaback.repository.UserRepository;
import com.github.codogma.codogmaback.repository.specifications.CommentSpecifications;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentService {

  private final CommentRepository commentRepository;
  private final ArticleRepository articleRepository;
  private final UserRepository userRepository;

  @Transactional
  public List<GetComment> getComments(Long articleId, String username, UserModel userModel) {
    List<GetComment> comments = new ArrayList<>();
    Specification<CommentModel> spec = Specification.where(null);
    if (articleId != null) {
      ArticleModel article = articleRepository.findById(articleId)
          .orElseThrow(() -> new ArticleNotFoundException("Article not found"));
      String currentUsername = (userModel != null) ? userModel.getUsername() : null;
      boolean isAdmin = userModel != null && userModel.getRole().equals(Role.ROLE_ADMIN);
      String articleAuthorUsername = article.getUser().getUsername();
      spec = Specification.where(CommentSpecifications.hasArticleId(articleId))
          .and(CommentSpecifications.isRootComment())
          .and(CommentSpecifications.hasAccess(currentUsername, isAdmin, articleAuthorUsername));
      comments = commentRepository.findAll(spec, Sort.by("createdAt").ascending()).stream()
          .map(this::convertCommentModelToDTO).toList();
    }
    if (username != null) {
      UserModel foundUser = userRepository.findByUsername(username).orElseThrow(
          () -> new UsernameNotFoundException("User not found by username " + username));
      boolean canViewAllComments =
          userModel != null && (userModel.getRole().equals(Role.ROLE_ADMIN) || Objects.equals(
              userModel.getId(), foundUser.getId()));
      spec = spec.and(CommentSpecifications.belongsToUser(foundUser.getId()))
          .and(CommentSpecifications.isAccessible(canViewAllComments));
      comments = commentRepository.findAll(spec, Sort.by("createdAt").ascending()).stream()
          .map(this::convertCommentModelToDTOWithArticleData).toList();
    }
    return comments;
  }

  @Transactional
  public GetComment createComment(CreateComment createComment, UserModel userModel) {
    ArticleModel article = articleRepository.findById(createComment.getArticleId())
        .orElseThrow(() -> new ArticleNotFoundException("Article not found"));
    CommentModel comment = new CommentModel();
    Status articleStatus = article.getStatus();
    if ((userModel.getRole().equals(Role.ROLE_ADMIN) || article.getUser().getId()
        .equals(userModel.getId()))) {
      if (articleStatus.equals(Status.PUBLISHED)) {
        comment.setStatus(Status.PUBLISHED);
      } else {
        comment.setStatus(Status.HIDDEN);
      }
    } else if (articleStatus.equals(Status.PUBLISHED)) {
      comment.setStatus(Status.MODERATION);
    } else {
      throw new AccessDeniedException("You don't have access to comment on this article");
    }
    Long parentCommentId = createComment.getParentCommentId();
    if (parentCommentId != null) {
      CommentModel parentComment = commentRepository.findById(parentCommentId).orElseThrow(
          () -> new CommentNotFoundException("Parent comment not found by id " + parentCommentId));
      comment.setParentComment(parentComment);
      Status parentCommentStatus = parentComment.getStatus();
      if ((userModel.getRole().equals(Role.ROLE_ADMIN) || article.getUser().getId()
          .equals(userModel.getId()))) {
        if (parentCommentStatus.equals(Status.PUBLISHED)) {
          comment.setStatus(Status.PUBLISHED);
        } else {
          comment.setStatus(Status.HIDDEN);
        }
      } else if (parentCommentStatus.equals(Status.PUBLISHED)) {
        comment.setStatus(Status.MODERATION);
      } else {
        throw new AccessDeniedException("You don't have access to comment on this comment");
      }
    }
    comment.setContent(createComment.getContent());
    comment.setUser(userModel);
    comment.setArticle(article);
    CommentModel savedComment = commentRepository.save(comment);
    return convertCommentModelToDTO(savedComment);
  }

  @Transactional
  public GetComment publishComment(Long commentId) {
    CommentModel comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new CommentNotFoundException("Comment not found by id " + commentId));
    comment.setStatus(Status.PUBLISHED);
    CommentModel savedComment = commentRepository.save(comment);
    return convertCommentModelToDTO(savedComment);
  }

  @Transactional
  public GetComment blockComment(Long commentId) {
    CommentModel comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new CommentNotFoundException("Comment not found by id " + commentId));
    comment.setStatus(Status.BLOCKED);
    CommentModel savedComment = commentRepository.save(comment);
    return convertCommentModelToDTO(savedComment);
  }

  @Transactional
  public GetComment updateComment(Long commentId, UpdateComment updateComment,
      UserModel userModel) {
    CommentModel comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new CommentNotFoundException("Comment not found by id " + commentId));
    String content = updateComment.getContent();
    if (!comment.getUser().getUsername().equals(userModel.getUsername())) {
      throw new SecurityException("You are not access to update this comment");
    }
    comment.setContent(content);
    CommentModel savedComment = commentRepository.save(comment);
    return convertCommentModelToDTO(savedComment);
  }

  @Transactional
  public void deleteComment(Long commentId, UserDetails userDetails) {
    CommentModel comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new CommentNotFoundException("Comment not found by id " + commentId));
    if (!comment.getUser().getUsername().equals(userDetails.getUsername())) {
      throw new SecurityException("You are not access to delete this comment");
    }
    commentRepository.delete(comment);
  }

  private GetComment convertCommentModelToDTO(CommentModel commentModel) {
    List<GetComment> replies = commentModel.getReplies().stream()
        .map(this::convertCommentModelToDTO).toList();
    return GetComment.builder().id(commentModel.getId()).status(commentModel.getStatus())
        .content(commentModel.getContent()).user(
            GetUser.builder().username(commentModel.getUser().getUsername())
                .avatarUrl(commentModel.getUser().getAvatarUrl()).build())
        .createdAt(commentModel.getCreatedAt()).replies(replies).build();
  }

  private GetComment convertCommentModelToDTOWithArticleData(CommentModel commentModel) {
    GetComment comment = convertCommentModelToDTO(commentModel);
    comment.setArticle(GetArticle.builder().id(commentModel.getArticle().getId())
        .title(commentModel.getArticle().getTitle()).build());
    comment.setReplies(null);
    return comment;
  }
}
