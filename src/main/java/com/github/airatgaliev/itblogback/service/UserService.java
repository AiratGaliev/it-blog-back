package com.github.airatgaliev.itblogback.service;

import com.github.airatgaliev.itblogback.dto.GetUser;
import com.github.airatgaliev.itblogback.dto.UpdateUser;
import com.github.airatgaliev.itblogback.exception.EmailNotFoundException;
import com.github.airatgaliev.itblogback.exception.IncorrectPasswordException;
import com.github.airatgaliev.itblogback.model.ArticleModel;
import com.github.airatgaliev.itblogback.model.Role;
import com.github.airatgaliev.itblogback.model.UserModel;
import com.github.airatgaliev.itblogback.repository.UserRepository;
import com.github.airatgaliev.itblogback.util.FileUploadUtil;
import java.util.List;
import java.util.Objects;
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
    user.setFirstName(updateUser.getFirstName());
    user.setLastName(updateUser.getLastName());
    user.setBio(updateUser.getBio());
    if (Objects.equals(updateUser.getCurrentEmail(), user.getEmail())) {
      user.setEmail(updateUser.getNewEmail());
    } else if (updateUser.getCurrentEmail() != null) {
      throw new EmailNotFoundException("Current email is incorrect");
    }
    if (passwordEncoder.matches(updateUser.getCurrentPassword(), user.getPassword())) {
      user.setPassword(passwordEncoder.encode(updateUser.getNewPassword()));
    } else if (updateUser.getNewPassword() != null) {
      throw new IncorrectPasswordException("Current password is incorrect");
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
        .articlesIds(userModel.getArticles().stream().map(ArticleModel::getId).toList())
        .role(userModel.getRole()).build();
  }
}
