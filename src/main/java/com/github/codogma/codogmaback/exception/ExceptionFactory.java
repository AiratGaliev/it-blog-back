package com.github.codogma.codogmaback.exception;

import com.github.codogma.codogmaback.util.LocalizationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExceptionFactory {

  private final LocalizationUtil localizationUtil;

  public UserAlreadyExistsException userAlreadyExistsException() {
    return new UserAlreadyExistsException(localizationUtil.getMessage("user.already.exists"));
  }

  public EmailAlreadyConfirmedException emailAlreadyConfirmedException() {
    return new EmailAlreadyConfirmedException(
        localizationUtil.getMessage("email.already.confirmed"));
  }

  public InvalidTokenException invalidTokenException() {
    return new InvalidTokenException(localizationUtil.getMessage("invalid.token"));
  }

  public TokenExpiredException tokenExpiredException() {
    return new TokenExpiredException(localizationUtil.getMessage("token.expired"));
  }
}
