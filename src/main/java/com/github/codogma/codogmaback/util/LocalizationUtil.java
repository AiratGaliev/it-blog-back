package com.github.codogma.codogmaback.util;

import com.github.codogma.codogmaback.interceptor.localization.LocalizationContext;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocalizationUtil {

  private final MessageSource messageSource;
  private final LocalizationContext localizationContext;

  public Locale getLocale() {
    return Locale.forLanguageTag(localizationContext.getLocale().getCode());
  }

  public String getMessage(String key, Object[] args) {
    Locale locale = getLocale();
    return messageSource.getMessage(key, args, locale);
  }

  public String getMessage(String key) {
    return getMessage(key, null);
  }
}
