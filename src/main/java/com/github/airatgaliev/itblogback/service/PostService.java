package com.github.airatgaliev.itblogback.service;

import com.github.airatgaliev.itblogback.dto.CreatePostDTO;
import com.github.airatgaliev.itblogback.dto.GetCategoryDTO;
import com.github.airatgaliev.itblogback.dto.GetPostDTO;
import com.github.airatgaliev.itblogback.dto.UpdatePostDTO;
import com.github.airatgaliev.itblogback.model.CategoryModel;
import com.github.airatgaliev.itblogback.model.PostModel;
import com.github.airatgaliev.itblogback.model.UserModel;
import com.github.airatgaliev.itblogback.repository.PostRepository;
import com.github.airatgaliev.itblogback.repository.UserRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

  private final PostRepository postRepository;
  private final UserRepository userRepository;

  @Transactional
  public List<GetPostDTO> getAllPosts() {
    return this.postRepository.findAll().stream().map(this::convertPostModelToDTO)
        .collect(Collectors.toList());
  }

  @Transactional
  public Optional<GetPostDTO> getPostById(Long id) {
    return this.postRepository.findById(id).map(this::convertPostModelToDTO);
  }

  @Transactional
  public void createPost(CreatePostDTO createPostDTO, UserDetails userDetails) {
    UserModel userModel = userRepository.findByUsername(userDetails.getUsername())
        .orElseThrow(() -> new RuntimeException("User not found"));
    PostModel postModel = new PostModel();
    postModel.setTitle(createPostDTO.getTitle());
    postModel.setContent(createPostDTO.getContent());
    postModel.setUser(userModel);
    postRepository.save(postModel);
  }

  @Transactional
  public void updatePost(Long id, UpdatePostDTO getPostDTO, UserDetails userDetails) {
    UserModel userModel = userRepository.findByUsername(userDetails.getUsername())
        .orElseThrow(() -> new RuntimeException("User not found"));
    PostModel postModel = postRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Post not found"));
    if (Objects.equals(userModel.getId(), postModel.getUser().getId())) {
      postModel.setTitle(getPostDTO.getTitle());
      postModel.setContent(getPostDTO.getContent());
      postModel.setUser(userModel);
      postRepository.save(postModel);
    } else {
      throw new RuntimeException("You are not accessible to update this post");
    }
  }

  @Transactional
  public void deletePost(Long id, UserDetails userDetails) {
    UserModel userModel = userRepository.findByUsername(userDetails.getUsername())
        .orElseThrow(() -> new RuntimeException("User not found"));
    PostModel postModel = postRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Post not found"));
    if (Objects.equals(userModel.getId(), postModel.getUser().getId())) {
      postRepository.deleteById(id);
    } else {
      throw new RuntimeException("You are not accessible to delete this post");
    }
  }

  private GetPostDTO convertPostModelToDTO(PostModel postModel) {
    return GetPostDTO.builder().id(postModel.getId()).title(postModel.getTitle())
        .content(postModel.getContent()).userId(postModel.getUser().getId()).categories(
            postModel.getCategories().stream().map(this::convertCategoryToDTO)
                .collect(Collectors.toList())).createdAt(postModel.getCreatedAt())
        .updatedAt(postModel.getUpdatedAt()).build();
  }

  private GetCategoryDTO convertCategoryToDTO(CategoryModel category) {
    return GetCategoryDTO.builder().id(category.getId()).name(category.getName()).build();
  }
}
