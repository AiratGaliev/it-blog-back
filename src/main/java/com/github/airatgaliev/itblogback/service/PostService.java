package com.github.airatgaliev.itblogback.service;

import com.github.airatgaliev.itblogback.dto.CreatePostDTO;
import com.github.airatgaliev.itblogback.dto.GetPostDTO;
import com.github.airatgaliev.itblogback.dto.UpdatePostDTO;
import com.github.airatgaliev.itblogback.model.PostModel;
import com.github.airatgaliev.itblogback.model.UserModel;
import com.github.airatgaliev.itblogback.repository.PostRepository;
import com.github.airatgaliev.itblogback.repository.UserRepository;
import java.util.List;
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

  public List<GetPostDTO> getAllPosts() {
    return this.postRepository.findAll().stream().map(this::convertPostModelToDTO)
        .collect(Collectors.toList());
  }

  public Optional<GetPostDTO> getPostById(Long id) {
    return this.postRepository.findById(id).map(this::convertPostModelToDTO);
  }

  @Transactional
  public void createPost(CreatePostDTO createPostDTO, UserDetails userDetails) {
    log.info("Creating post for user: {}", userDetails.getUsername());
    UserModel userModel = userRepository.findByUsername(userDetails.getUsername())
        .orElseThrow(() -> new RuntimeException("Author not found"));
    PostModel postModel = new PostModel();
    postModel.setTitle(createPostDTO.getTitle());
    postModel.setContent(createPostDTO.getContent());
    postModel.setUser(userModel);
    postRepository.save(postModel);
    log.info("Post saved with title: {}", createPostDTO.getTitle());
  }

  @Transactional
  public void updatePost(Long id, UpdatePostDTO getPostDTO) {
    UserModel userModel = userRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Author not found"));
    PostModel postModel = postRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Post not found"));
    postModel.setTitle(getPostDTO.getTitle());
    postModel.setContent(getPostDTO.getContent());
    postModel.setUser(userModel);
    postRepository.save(postModel);
  }

  @Transactional
  public void deletePost(Long id) {
    postRepository.deleteById(id);
  }

  private GetPostDTO convertPostModelToDTO(PostModel postModel) {
    return GetPostDTO.builder().title(postModel.getTitle()).content(postModel.getContent())
        .authorId(postModel.getUser().getId()).build();
  }
}
