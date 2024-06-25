package com.github.airatgaliev.itblogback.service;

import com.github.airatgaliev.itblogback.dto.GetPostDTO;
import com.github.airatgaliev.itblogback.dto.GetUserDTO;
import com.github.airatgaliev.itblogback.dto.UpdateUserDTO;
import com.github.airatgaliev.itblogback.model.UserModel;
import com.github.airatgaliev.itblogback.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  public List<GetUserDTO> getAllUsers() {
    return userRepository.findAll().stream().map(this::convertUserModelToDto)
        .collect(Collectors.toList());
  }

  public Optional<GetUserDTO> getUserById(Long id) {
    return userRepository.findByIdWithPosts(id).map(this::convertUserModelToDto);
  }

  public void updateUser(Long id, UpdateUserDTO updateUserDTO) {
    UserModel userModel = userRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("User not found"));
    userModel.setUsername(updateUserDTO.getUsername());
    userRepository.save(userModel);
  }

  public void deleteUser(Long id) {
    userRepository.deleteById(id);
  }

  private GetUserDTO convertUserModelToDto(UserModel userModel) {
    return GetUserDTO.builder().id(userModel.getId()).username(userModel.getUsername())
        .email(userModel.getEmail())
        .firstName(userModel.getFirstName()).lastName(userModel.getLastName()).posts(
            userModel.getPosts().stream().map(
                    postModel -> GetPostDTO.builder().id(postModel.getId()).title(postModel.getTitle())
                        .content(postModel.getContent()).userId(postModel.getUser().getId()).build())
                .toList()).role(userModel.getRole()).build();
  }
}
