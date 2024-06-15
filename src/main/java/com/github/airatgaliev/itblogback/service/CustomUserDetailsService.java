package com.github.airatgaliev.itblogback.service;

import com.github.airatgaliev.itblogback.model.UserModel;
import com.github.airatgaliev.itblogback.repository.UserRepository;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    UserModel userModel = userRepository.findByUsername(username);
    if (userModel == null) {
      throw new UsernameNotFoundException("User not found");
    }
    return new User(userModel.getUsername(),
        userModel.getPassword(),
        userModel.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority(role.getRole().name()))
            .collect(Collectors.toList()));
  }
}