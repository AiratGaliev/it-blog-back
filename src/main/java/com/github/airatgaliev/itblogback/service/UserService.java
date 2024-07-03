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
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  @Transactional
  public List<GetUserDTO> getAllUsers() {
    return userRepository.findAll().stream().map(this::convertUserModelToDto)
        .collect(Collectors.toList());
  }

  @Transactional
  public Optional<GetUserDTO> getUserByUsername(String username) {
    return userRepository.findByUsername(username).map(this::convertUserModelToDto);
  }

  @Transactional
  public void updateUser(String username, UpdateUserDTO updateUserDTO) {
    UserModel userModel = userRepository.findByUsername(username)
        .orElseThrow(() -> new RuntimeException("User not found"));
    userModel.setUsername(updateUserDTO.getUsername());
    userRepository.save(userModel);
  }

  @Transactional
  public void deleteUser(String username) {
    userRepository.deleteByUsername(username);
  }

  private GetUserDTO convertUserModelToDto(UserModel userModel) {
    return GetUserDTO.builder().username(userModel.getUsername()).email(userModel.getEmail())
        .firstName(userModel.getFirstName()).lastName(userModel.getLastName()).posts(
            userModel.getPosts().stream().map(
                postModel -> GetPostDTO.builder().id(postModel.getId()).title(postModel.getTitle())
                    .content(postModel.getContent()).username(postModel.getUser().getUsername())
                    .build()).toList()).role(userModel.getRole()).build();
  }
}
