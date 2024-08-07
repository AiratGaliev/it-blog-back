package com.github.airatgaliev.itblogback.service;

import com.github.airatgaliev.itblogback.dto.GetUser;
import com.github.airatgaliev.itblogback.dto.UpdateUser;
import com.github.airatgaliev.itblogback.exception.EmailAlreadyExistsException;
import com.github.airatgaliev.itblogback.exception.IncorrectPasswordException;
import com.github.airatgaliev.itblogback.exception.UsernameAlreadyExistsException;
import com.github.airatgaliev.itblogback.model.ArticleModel;
import com.github.airatgaliev.itblogback.model.Role;
import com.github.airatgaliev.itblogback.model.UserModel;
import com.github.airatgaliev.itblogback.repository.UserRepository;
import com.github.airatgaliev.itblogback.util.FileUploadUtil;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final FileUploadUtil fileUploadUtil;
  private final PasswordEncoder passwordEncoder;

  @Value("${server.servlet.context-path}")
  private String contextPath;

  @Transactional
  public List<GetUser> getAllUsers() {
    return userRepository.findAll().stream().map(this::convertUserModelToDto)
        .collect(Collectors.toList());
  }

  @Transactional
  public List<GetUser> getAllAuthors() {
    return userRepository.findAllByRole(Role.ROLE_AUTHOR).stream()
        .map(this::convertUserModelToDto)
        .collect(Collectors.toList());
  }

  @Transactional
  public Optional<GetUser> getUserByUsername(String username) {
    return userRepository.findByUsername(username).map(this::convertUserModelToDto);
  }

  @Transactional
  public void updateUser(UpdateUser updateUser, UserDetails userDetails) {
    UserModel user = userRepository.findByUsername(userDetails.getUsername())
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    if (updateUser.getUsername() != null && !updateUser.getUsername().equals(user.getUsername())) {
      boolean isExistsUser = userRepository.existsByUsername(updateUser.getUsername());
      if (isExistsUser) {
        throw new UsernameAlreadyExistsException("This username is already taken");
      }
      user.setUsername(updateUser.getUsername());
    }
    if (updateUser.getFirstName() != null) {
      user.setFirstName(updateUser.getFirstName());
    }
    if (updateUser.getLastName() != null) {
      user.setLastName(updateUser.getLastName());
    }
    if (updateUser.getBio() != null) {
      user.setBio(updateUser.getBio());
    }
    if (updateUser.getNewEmail() != null && !updateUser.getNewEmail().equals(user.getEmail())) {
      boolean isExistsEmail = userRepository.existsByEmail(updateUser.getNewEmail());
      if (isExistsEmail) {
        throw new EmailAlreadyExistsException("This email is already taken");
      }
      user.setEmail(updateUser.getNewEmail());
    }
    if (updateUser.getNewPassword() != null && !updateUser.getNewPassword().isEmpty()) {
      if (passwordEncoder.matches(updateUser.getCurrentPassword(), user.getPassword())) {
        user.setPassword(passwordEncoder.encode(updateUser.getNewPassword()));
      } else {
        throw new IncorrectPasswordException("Current password is incorrect");
      }
    }
    if (updateUser.getAvatar() != null && !updateUser.getAvatar().isEmpty()) {
      String avatarFilename = fileUploadUtil.uploadUserAvatar(updateUser.getAvatar(),
          user.getUsername());
      String avatarUrl = String.format("%s/users/avatars/%s", contextPath, avatarFilename);
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
        .bio(userModel.getBio()).avatarUrl(userModel.getAvatarUrl())
        .articlesIds(userModel.getArticles().stream().map(ArticleModel::getId).toList())
        .role(userModel.getRole()).build();
  }
}
