package com.github.airatgaliev.itblogback.service;

import com.github.airatgaliev.itblogback.dto.CreateComment;
import com.github.airatgaliev.itblogback.dto.GetArticle;
import com.github.airatgaliev.itblogback.dto.GetComment;
import com.github.airatgaliev.itblogback.dto.GetUser;
import com.github.airatgaliev.itblogback.dto.UpdateComment;
import com.github.airatgaliev.itblogback.exception.ArticleNotFoundException;
import com.github.airatgaliev.itblogback.exception.CommentNotFoundException;
import com.github.airatgaliev.itblogback.model.ArticleModel;
import com.github.airatgaliev.itblogback.model.CommentModel;
import com.github.airatgaliev.itblogback.model.UserModel;
import com.github.airatgaliev.itblogback.repository.ArticleRepository;
import com.github.airatgaliev.itblogback.repository.CommentRepository;
import com.github.airatgaliev.itblogback.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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
  public List<GetComment> getCommentsByArticleId(Long articleId) {
    articleRepository.findById(articleId)
        .orElseThrow(() -> new ArticleNotFoundException("Article not found"));
    return commentRepository.findByArticleIdAndParentCommentIsNullOrderByCreatedAtAsc(articleId)
        .stream().map(this::convertCommentModelToDTO).collect(Collectors.toList());
  }

  @Transactional
  public GetComment createComment(CreateComment createComment, UserDetails userDetails) {
    UserModel userModel = userRepository.findByUsername(userDetails.getUsername())
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    ArticleModel article = articleRepository.findById(createComment.getArticleId())
        .orElseThrow(() -> new ArticleNotFoundException("Article not found"));

    CommentModel comment = new CommentModel();
    comment.setContent(createComment.getContent());
    comment.setUser(userModel);
    comment.setArticle(article);

    if (createComment.getParentCommentId() != null) {
      CommentModel parentComment = commentRepository.findById(createComment.getParentCommentId())
          .orElseThrow(() -> new CommentNotFoundException(
              "Comment not found by id " + createComment.getParentCommentId()));
      comment.setParentComment(parentComment);
    }

    return convertCommentModelToDTO(commentRepository.save(comment));
  }

  @Transactional
  public GetComment updateComment(Long commentId, UpdateComment updateComment,
      UserDetails userDetails) {
    CommentModel comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new CommentNotFoundException("Comment not found by id " + commentId));

    if (!comment.getUser().getUsername().equals(userDetails.getUsername())) {
      throw new SecurityException("You are not authorized to update this comment");
    }

    comment.setContent(updateComment.getContent());
    return convertCommentModelToDTO(commentRepository.save(comment));
  }

  @Transactional
  public List<GetComment> getCommentsByUsername(String username) {
    UserModel userModel = userRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found by username " + username));
    return userModel.getComments().stream().map(commentModel -> {
      GetComment getComment = convertCommentModelToDTO(commentModel);
      getComment.setArticle(GetArticle.builder().id(commentModel.getArticle().getId())
          .title(commentModel.getArticle().getTitle()).build());
      getComment.setReplies(null);
      return getComment;
    }).collect(Collectors.toList());
  }

  @Transactional
  public void deleteComment(Long commentId, UserDetails userDetails) {
    CommentModel comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new CommentNotFoundException("Comment not found by id " + commentId));
    if (!comment.getUser().getUsername().equals(userDetails.getUsername())) {
      throw new SecurityException("You are not authorized to delete this comment");
    }
    commentRepository.delete(comment);
  }

  private GetComment convertCommentModelToDTO(CommentModel commentModel) {
    List<GetComment> replies = commentModel.getReplies().stream()
        .map(this::convertCommentModelToDTO).collect(Collectors.toList());

    return GetComment.builder().id(commentModel.getId()).content(commentModel.getContent()).user(
            GetUser.builder().username(commentModel.getUser().getUsername())
                .avatarUrl(commentModel.getUser().getAvatarUrl()).build())
        .createdAt(commentModel.getCreatedAt()).replies(replies).build();
  }
}
