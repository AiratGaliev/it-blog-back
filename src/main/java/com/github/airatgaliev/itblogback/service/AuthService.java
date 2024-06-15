package com.github.airatgaliev.itblogback.service;

import com.github.airatgaliev.itblogback.dto.AuthRequest;
import com.github.airatgaliev.itblogback.dto.AuthResponse;
import com.github.airatgaliev.itblogback.dto.CreateUserDTO;
import com.github.airatgaliev.itblogback.model.ERole;
import com.github.airatgaliev.itblogback.model.RoleModel;
import com.github.airatgaliev.itblogback.model.UserModel;
import com.github.airatgaliev.itblogback.repository.RoleRepository;
import com.github.airatgaliev.itblogback.repository.UserRepository;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;

  private final RoleRepository roleRepository;

  public void register(CreateUserDTO createUserDTO) {
    UserModel user = new UserModel();
    user.setUsername(createUserDTO.getUsername());
    user.setEmail(createUserDTO.getEmail());
    user.setPassword(passwordEncoder.encode(createUserDTO.getPassword()));
    RoleModel userRole = roleRepository.findByRole(ERole.USER);
    user.setRoles(Set.of(userRole));
    userRepository.save(user);
  }

  public AuthResponse login(AuthRequest authRequest) {
    UserModel user = userRepository.findByUsername(authRequest.getUsername());
    if (user != null && passwordEncoder.matches(authRequest.getPassword(), user.getPassword())) {
      // Возвращаем токен (в реальной жизни тут должна быть логика создания JWT)
      return new AuthResponse("fake-jwt-token");
    } else {
      throw new RuntimeException("Invalid credentials");
    }
  }
}
