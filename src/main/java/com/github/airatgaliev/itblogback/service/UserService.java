package com.github.airatgaliev.itblogback.service;

import com.github.airatgaliev.itblogback.dto.CreateUserDTO;
import com.github.airatgaliev.itblogback.dto.UpdateUserDTO;
import com.github.airatgaliev.itblogback.dto.UserDTO;
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

  public List<UserDTO> getAllAuthors() {
    return userRepository.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
  }

  public Optional<UserDTO> getAuthorById(Long id) {
    return userRepository.findById(id).map(this::convertToDto);
  }

  public void createAuthor(CreateUserDTO createUserDTO) {
    UserModel userModel = new UserModel();
    userModel.setUsername(createUserDTO.getUsername());
    userRepository.save(userModel);
  }

  public void updateAuthor(Long id, UpdateUserDTO updateUserDTO) {
    UserModel userModel = userRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Author not found"));
    userModel.setUsername(updateUserDTO.getUsername());
    userRepository.save(userModel);
  }

  public void deleteAuthor(Long id) {
    userRepository.deleteById(id);
  }

  private UserDTO convertToDto(UserModel userModel) {
    UserDTO userDTO = new UserDTO();
    userDTO.setName(userModel.getUsername());
    return userDTO;
  }
}
