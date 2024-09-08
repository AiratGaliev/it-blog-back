package com.github.airatgaliev.itblogback.service;

import static com.github.airatgaliev.itblogback.util.TokenUtils.extractToken;
import static com.github.airatgaliev.itblogback.util.TokenUtils.invalidateToken;

import com.github.airatgaliev.itblogback.dto.AuthenticationResponse;
import com.github.airatgaliev.itblogback.dto.GetUser;
import com.github.airatgaliev.itblogback.dto.SignInRequest;
import com.github.airatgaliev.itblogback.dto.SignUpRequest;
import com.github.airatgaliev.itblogback.model.Role;
import com.github.airatgaliev.itblogback.model.UserModel;
import com.github.airatgaliev.itblogback.repository.UserRepository;
import com.github.airatgaliev.itblogback.util.FileUploadUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final UserDetailsService userDetailsService;
  private final JwtService jwtService;
  private final FileUploadUtil fileUploadUtil;

  @Transactional
  public GetUser signup(SignUpRequest signUpRequest) {
    UserModel user = UserModel.builder().username(signUpRequest.getUsername())
        .email(signUpRequest.getEmail())
        .password(passwordEncoder.encode(signUpRequest.getPassword())).role(Role.ROLE_USER).build();
    if (signUpRequest.getAvatar() != null && !signUpRequest.getAvatar().isEmpty()) {
      String avatarUrl = fileUploadUtil.uploadUserAvatar(signUpRequest.getAvatar(),
          user.getUsername());
      user.setAvatarUrl(avatarUrl);
    }
    userRepository.save(user);
    return convertUserModelToDto(user);
  }

  public AuthenticationResponse authenticate(SignInRequest input, HttpServletResponse response) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(input.getUsernameOrEmail(), input.getPassword()));
    UserModel user = userRepository.findByUsernameOrEmail(input.getUsernameOrEmail(),
        input.getUsernameOrEmail()).orElseThrow(
        () -> new UsernameNotFoundException("User not found " + input.getUsernameOrEmail()));
    String jwtToken = jwtService.generateToken(user);
    setAuthTokenCookie(response, jwtToken);
    return AuthenticationResponse.builder().token(jwtToken)
        .expiresIn(jwtService.getExpirationTime()).build();
  }

  private void setAuthTokenCookie(HttpServletResponse response, String token) {
    ResponseCookie cookie = ResponseCookie.from("auth-token", token).httpOnly(true).secure(false)
        .path("/").build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }

  public AuthenticationResponse handleTokenRefresh(HttpServletRequest request,
      HttpServletResponse response) {
    String token = extractToken(request);
    if (token == null || jwtService.isTokenExpired(token)) {
      throw new RuntimeException("Token has expired or is invalid");
    }
    AuthenticationResponse refreshedToken = refreshToken(token);
    setAuthTokenCookie(response, refreshedToken.getToken());
    return refreshedToken;
  }

  public AuthenticationResponse refreshToken(String token) {
    String username = jwtService.extractUsername(token);
    UserModel user = userRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found " + username));

    String newToken = jwtService.generateToken(user);
    return AuthenticationResponse.builder().token(newToken)
        .expiresIn(jwtService.getExpirationTime()).build();
  }

  public Optional<GetUser> currentUser(HttpServletRequest request) {
    String token = extractToken(request);
    if (token != null) {
      String username = jwtService.extractUsername(token);
      return userRepository.findByUsername(username).map(this::convertUserModelToDto);
    }
    return Optional.empty();
  }

  public void logout(HttpServletRequest request, HttpServletResponse response) {
    String token = extractToken(request);
    String username = jwtService.extractUsername(token);
    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
    if (token != null && jwtService.isTokenValid(token, userDetails)) {
      invalidateToken(response);
    }
  }

  private GetUser convertUserModelToDto(UserModel userModel) {
    return GetUser.builder().username(userModel.getUsername()).email(userModel.getEmail())
        .firstName(userModel.getFirstName()).lastName(userModel.getLastName())
        .bio(userModel.getBio()).role(userModel.getRole()).avatarUrl(userModel.getAvatarUrl())
        .build();
  }
}