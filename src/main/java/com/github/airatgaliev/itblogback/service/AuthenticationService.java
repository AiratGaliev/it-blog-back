package com.github.airatgaliev.itblogback.service;

import static com.github.airatgaliev.itblogback.util.TokenUtils.extractToken;
import static com.github.airatgaliev.itblogback.util.TokenUtils.invalidateToken;
import static com.github.airatgaliev.itblogback.util.TokenUtils.setAuthCookie;

import com.github.airatgaliev.itblogback.dto.AuthenticationResponse;
import com.github.airatgaliev.itblogback.dto.GetUser;
import com.github.airatgaliev.itblogback.dto.SignInRequest;
import com.github.airatgaliev.itblogback.dto.SignUpRequest;
import com.github.airatgaliev.itblogback.exception.EmailAlreadyConfirmedException;
import com.github.airatgaliev.itblogback.exception.EmailNotConfirmedException;
import com.github.airatgaliev.itblogback.exception.InvalidTokenException;
import com.github.airatgaliev.itblogback.exception.TokenExpiredException;
import com.github.airatgaliev.itblogback.exception.UserAlreadyExistsException;
import com.github.airatgaliev.itblogback.handler.oauth.OAuth2ProviderHandler;
import com.github.airatgaliev.itblogback.model.ConfirmationToken;
import com.github.airatgaliev.itblogback.model.Role;
import com.github.airatgaliev.itblogback.model.UserModel;
import com.github.airatgaliev.itblogback.repository.UserRepository;
import com.github.airatgaliev.itblogback.util.FileUploadUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final UserDetailsService userDetailsService;
  private final JwtService jwtService;
  private final FileUploadUtil fileUploadUtil;
  private final List<OAuth2ProviderHandler> providerHandlers;
  private final EmailService emailService;
  private final ConfirmationTokenService tokenService;

  @Transactional
  public GetUser signUp(SignUpRequest signUpRequest, String origin) {
    userRepository.findByUsernameOrEmail(signUpRequest.getUsername(), signUpRequest.getEmail())
        .ifPresent((user) -> {
          throw new UserAlreadyExistsException("User with this username or email already exists");
        });
    UserModel user = UserModel.builder().username(signUpRequest.getUsername())
        .email(signUpRequest.getEmail())
        .password(passwordEncoder.encode(signUpRequest.getPassword())).role(Role.ROLE_USER).build();
    if (signUpRequest.getAvatar() != null && !signUpRequest.getAvatar().isEmpty()) {
      String avatarUrl = fileUploadUtil.uploadUserAvatar(signUpRequest.getAvatar(),
          user.getUsername());
      user.setAvatarUrl(avatarUrl);
    }
    userRepository.save(user);
    String token = jwtService.generateToken(user);
    ConfirmationToken confirmationToken = ConfirmationToken.builder().token(token).user(user)
        .createdAt(LocalDateTime.now()).expiresAt(LocalDateTime.now().plusHours(24)).build();
    tokenService.saveConfirmationToken(confirmationToken);
    emailService.sendEmailVerification(user.getEmail(), token, origin);
    return convertUserModelToDto(user);
  }

  @Transactional
  public void confirmEmail(String token) {
    ConfirmationToken confirmationToken = tokenService.getToken(token)
        .orElseThrow(() -> new InvalidTokenException("Token not found"));
    if (confirmationToken.getConfirmedAt() != null) {
      throw new EmailAlreadyConfirmedException("Email already confirmed");
    }
    if (confirmationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
      throw new TokenExpiredException("Confirmation token has expired");
    }
    UserModel user = confirmationToken.getUser();
    user.setEnabled(true);
    userRepository.save(user);
    tokenService.setConfirmedAt(token);
    log.info("Email confirmed for user: {}", user.getUsername());
  }

  @Transactional
  public AuthenticationResponse signIn(SignInRequest input, HttpServletResponse response) {
    log.info("Attempting to authenticate user: {}", input.getUsernameOrEmail());
    try {
      UserModel user = userRepository.findByUsernameOrEmail(input.getUsernameOrEmail(),
          input.getUsernameOrEmail()).orElseThrow(() -> new UsernameNotFoundException(
          "User not found with these credentials " + input.getUsernameOrEmail()));
      if (!user.isEnabled()) {
        throw new EmailNotConfirmedException(
            "Email not confirmed! Please confirm your email before signing in!");
      }
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(input.getUsernameOrEmail(), input.getPassword()));
      String jwtToken = jwtService.generateToken(user);
      setAuthCookie(response, jwtToken);
      return AuthenticationResponse.builder().token(jwtToken)
          .expiresIn(jwtService.getExpirationTime()).build();
    } catch (Exception e) {
      log.error("Authentication failed for user: {}", input.getUsernameOrEmail(), e);
      throw e;
    }
  }

  @Transactional
  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    String registrationId = userRequest.getClientRegistration().getRegistrationId();
    OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);

    OAuth2ProviderHandler handler = providerHandlers.stream()
        .filter(h -> h.supports(registrationId)).findFirst()
        .orElseThrow(() -> new OAuth2AuthenticationException("Unknown provider"));

    UserModel user = handler.processOAuth2User(oAuth2User);
    user.setPassword(passwordEncoder.encode(generateRandomPassword()));

    UserModel existingUser = userRepository.findByEmail(user.getEmail())
        .orElseGet(() -> {
          UserModel newUser = userRepository.save(user);
          newUser.setUsername("username_" + newUser.getId());
          return newUser;
        });
    existingUser.setEnabled(true);
    existingUser.updateFrom(user);
    userRepository.save(existingUser);

    String jwtToken = jwtService.generateToken(existingUser);
    HttpServletResponse response = ((ServletRequestAttributes) Objects.requireNonNull(
        RequestContextHolder.getRequestAttributes())).getResponse();
    if (response != null) {
      setAuthCookie(response, jwtToken);
    }
    log.info("User {} authenticated with provider {}", existingUser.getUsername(), registrationId);
    return oAuth2User;
  }

  public AuthenticationResponse handleTokenRefresh(HttpServletRequest request,
      HttpServletResponse response) {
    String token = extractToken(request);
    if (token == null || jwtService.isTokenExpired(token)) {
      throw new TokenExpiredException("Token has expired or is invalid");
    }
    AuthenticationResponse refreshedToken = refreshToken(token);
    setAuthCookie(response, refreshedToken.getToken());
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
    log.info("Attempting to logout user");
    String token = extractToken(request);
    if (token != null) {
      try {
        String username = jwtService.extractUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (jwtService.isTokenValid(token, userDetails)) {
          invalidateToken(request, response);
          log.info("User logged out successfully: {}", username);
        }
      } catch (Exception e) {
        log.error("Error during logout", e);
        invalidateToken(request, response);
      }
    } else {
      invalidateToken(request, response);
    }
  }

  private GetUser convertUserModelToDto(UserModel userModel) {
    return GetUser.builder().username(userModel.getUsername()).email(userModel.getEmail())
        .firstName(userModel.getFirstName()).lastName(userModel.getLastName())
        .bio(userModel.getBio()).role(userModel.getRole()).avatarUrl(userModel.getAvatarUrl())
        .build();
  }

  private String generateRandomPassword() {
    return UUID.randomUUID().toString();
  }
}