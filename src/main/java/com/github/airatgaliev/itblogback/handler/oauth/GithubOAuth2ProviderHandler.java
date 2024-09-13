package com.github.airatgaliev.itblogback.handler.oauth;

import com.github.airatgaliev.itblogback.model.Role;
import com.github.airatgaliev.itblogback.model.UserModel;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class GithubOAuth2ProviderHandler implements OAuth2ProviderHandler {

  @Override
  public boolean supports(String registrationId) {
    return "github".equals(registrationId);
  }

  @Override
  public UserModel processOAuth2User(OAuth2User oAuth2User) {
    String email = oAuth2User.getAttribute("email");
    Integer githubId = oAuth2User.getAttribute("id");
    String username = oAuth2User.getAttribute("login");

    return UserModel.builder()
        .githubId(githubId)
        .username(username)
        .email(email)
        .role(Role.ROLE_USER)
        .build();
  }
}
