package com.github.codogma.codogmaback.handler.oauth;

import com.github.codogma.codogmaback.model.Role;
import com.github.codogma.codogmaback.model.UserModel;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class GitlabOAuth2ProviderHandler implements OAuth2ProviderHandler {

  @Override
  public boolean supports(String registrationId) {
    return "gitlab".equals(registrationId);
  }

  @Override
  public UserModel processOAuth2User(OAuth2User oAuth2User) {
    String email = oAuth2User.getAttribute("email");
    Integer gitlabId = oAuth2User.getAttribute("id");
    String username = oAuth2User.getAttribute("username");

    return UserModel.builder()
        .gitlabId(gitlabId)
        .username(username)
        .email(email)
        .role(Role.ROLE_USER)
        .build();
  }
}
