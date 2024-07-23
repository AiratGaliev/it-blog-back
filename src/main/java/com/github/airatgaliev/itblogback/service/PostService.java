package com.github.airatgaliev.itblogback.service;

import com.github.airatgaliev.itblogback.dto.CreatePost;
import com.github.airatgaliev.itblogback.dto.GetCategory;
import com.github.airatgaliev.itblogback.dto.GetPost;
import com.github.airatgaliev.itblogback.dto.UpdatePost;
import com.github.airatgaliev.itblogback.exception.PostNotFoundException;
import com.github.airatgaliev.itblogback.model.CategoryModel;
import com.github.airatgaliev.itblogback.model.PostModel;
import com.github.airatgaliev.itblogback.model.UserModel;
import com.github.airatgaliev.itblogback.repository.CategoryRepository;
import com.github.airatgaliev.itblogback.repository.PostRepository;
import com.github.airatgaliev.itblogback.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

  private final PostRepository postRepository;
  private final UserRepository userRepository;
  private final CategoryRepository categoryRepository;

  @Transactional
  public List<GetPost> getAllPosts() {
    return this.postRepository.findAll().stream().map(this::convertPostModelToDTO)
        .collect(Collectors.toList());
  }

  @Transactional
  public List<GetPost> getPostsByCategoryId(Long categoryId) {
    return postRepository.findByCategoriesId(categoryId).stream().map(this::convertPostModelToDTO)
        .collect(Collectors.toList());
  }

  @Transactional
  public Optional<GetPost> getPostById(Long id) {
    return this.postRepository.findById(id).map(this::convertPostModelToDTO);
  }

  @Transactional
  public GetPost createPost(CreatePost createPost, UserDetails userDetails) {
    UserModel userModel = userRepository.findByUsername(userDetails.getUsername())
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    List<CategoryModel> categories = new ArrayList<>(
        categoryRepository.findAllById(createPost.getCategoryIds()));
    PostModel postModel = new PostModel();
    postModel.setTitle(createPost.getTitle());
    postModel.setContent(createPost.getContent());
    postModel.setCategories(categories);
    postModel.setUser(userModel);
    PostModel savedPost = postRepository.save(postModel);
    return convertPostModelToDTO(savedPost);
  }

  @Transactional
  public void updatePost(Long id, UpdatePost updatePost, UserDetails userDetails) {
    UserModel userModel = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(
        () -> new UsernameNotFoundException("User not found " + userDetails.getUsername()));
    PostModel postModel = postRepository.findById(id)
        .orElseThrow(() -> new PostNotFoundException("Post not found"));
    List<CategoryModel> categories = new ArrayList<>(
        categoryRepository.findAllById(updatePost.getCategoryIds()));
    if (Objects.equals(userModel.getId(), postModel.getUser().getId())) {
      postModel.setTitle(updatePost.getTitle());
      postModel.setContent(updatePost.getContent());
      postModel.setContent(updatePost.getContent());
      postModel.setCategories(categories);
      postModel.setUser(userModel);
      postRepository.save(postModel);
    } else {
      throw new AccessDeniedException("You are not accessible to update this post");
    }
  }

  @Transactional
  public void deletePost(Long id, UserDetails userDetails) {
    UserModel userModel = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(
        () -> new UsernameNotFoundException("User not found " + userDetails.getUsername()));
    PostModel postModel = postRepository.findById(id)
        .orElseThrow(() -> new PostNotFoundException("Post not found"));
    if (Objects.equals(userModel.getId(), postModel.getUser().getId())) {
      postRepository.deleteById(id);
    } else {
      throw new AccessDeniedException("You are not accessible to delete this post");
    }
  }

  private GetPost convertPostModelToDTO(PostModel postModel) {
    return GetPost.builder().id(postModel.getId()).title(postModel.getTitle())
        .content(postModel.getContent()).username(postModel.getUser().getUsername()).categories(
            postModel.getCategories().stream().map(
                categoryModel -> GetCategory.builder().id(categoryModel.getId())
                    .name(categoryModel.getName()).build()).collect(Collectors.toList()))
        .createdAt(postModel.getCreatedAt()).updatedAt(postModel.getUpdatedAt()).build();
  }
}
