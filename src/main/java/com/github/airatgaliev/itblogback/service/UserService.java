package com.github.airatgaliev.itblogback.service;

import com.github.airatgaliev.itblogback.dto.GetUser;
import com.github.airatgaliev.itblogback.dto.UpdateUser;
import com.github.airatgaliev.itblogback.model.PostModel;
import com.github.airatgaliev.itblogback.model.UserModel;
import com.github.airatgaliev.itblogback.repository.UserRepository;
import com.github.airatgaliev.itblogback.util.FileUploadUtil;
import jakarta.validation.ValidationException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final FileUploadUtil fileUploadUtil;

  @Transactional
  public List<GetUser> getAllUsers() {
    return userRepository.findAll().stream().map(this::convertUserModelToDto)
        .collect(Collectors.toList());
  }

  @Transactional
  public Optional<GetUser> getUserByUsername(String username) {
    return userRepository.findByUsername(username).map(this::convertUserModelToDto);
  }

  @Transactional
  public void updateUser(UpdateUser updateUser, UserDetails userDetails) {
    UserModel user = userRepository.findByUsername(userDetails.getUsername())
        .orElseThrow(() -> new ValidationException("User not found"));

    user.setUsername(updateUser.getUsername());
    user.setEmail(updateUser.getEmail());

    if (updateUser.getAvatar() != null && !updateUser.getAvatar().isEmpty()) {
      String avatarUrl = fileUploadUtil.uploadUserAvatar(updateUser.getAvatar(),
          user.getUsername());
      user.setAvatarUrl(avatarUrl);
    }

    userRepository.save(user);
  }

  @Transactional
  public void deleteUser(String username) {
    userRepository.deleteByUsername(username);
  }

  private GetUser convertUserModelToDto(UserModel userModel) {
    return GetUser.builder().username(userModel.getUsername()).email(userModel.getEmail())
        .firstName(userModel.getFirstName()).lastName(userModel.getLastName())
        .postsIds(userModel.getPosts().stream().map(PostModel::getId).toList())
        .role(userModel.getRole()).build();
  }
}
