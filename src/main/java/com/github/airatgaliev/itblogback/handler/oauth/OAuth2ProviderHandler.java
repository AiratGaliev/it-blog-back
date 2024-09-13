package com.github.airatgaliev.itblogback.handler.oauth;

import com.github.airatgaliev.itblogback.model.UserModel;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface OAuth2ProviderHandler {

  boolean supports(String registrationId);

  UserModel processOAuth2User(OAuth2User oAuth2User);
}
