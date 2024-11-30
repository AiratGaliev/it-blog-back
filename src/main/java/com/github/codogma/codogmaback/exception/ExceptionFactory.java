package com.github.codogma.codogmaback.exception;

import com.github.codogma.codogmaback.util.LocalizationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExceptionFactory {

  private final LocalizationUtil localizationUtil;

  public UserAlreadyExistsException userAlreadyExists() {
    return new UserAlreadyExistsException(localizationUtil.getMessage("auth.user.already.exists"));
  }

  public EmailAlreadyConfirmedException emailAlreadyConfirmed() {
    return new EmailAlreadyConfirmedException(
        localizationUtil.getMessage("auth.email.already.confirmed"));
  }

  public InvalidTokenException invalidToken() {
    return new InvalidTokenException(localizationUtil.getMessage("auth.invalid.token"));
  }

  public TokenExpiredException tokenExpired() {
    return new TokenExpiredException(localizationUtil.getMessage("auth.token.expired"));
  }

  public UsernameOrEmailNotFoundException usernameOrEmailNotFound() {
    return new UsernameOrEmailNotFoundException(
        localizationUtil.getMessage("auth.username.or.email.expired"));
  }

  public EmailSendException emailSend() {
    return new EmailSendException(localizationUtil.getMessage("email.send"));
  }

  public EmailNotConfirmedException emailNotConfirmed() {
    return new EmailNotConfirmedException(localizationUtil.getMessage("email.not.confirmed"));
  }

  public SubscriptionAlreadyExistsException subscriptionAlreadyExists() {
    return new SubscriptionAlreadyExistsException(
        localizationUtil.getMessage("user.already.subscribed"));
  }

  public UsernameNotFoundException targetUserNotFound() {
    return new UsernameNotFoundException(
        localizationUtil.getMessage("user.target.not.found"));
  }

  public IllegalArgumentException userCannotSubscribeToThemselves() {
    return new IllegalArgumentException(
        localizationUtil.getMessage("user.cannot.subscribe.to.themselves"));
  }
}
