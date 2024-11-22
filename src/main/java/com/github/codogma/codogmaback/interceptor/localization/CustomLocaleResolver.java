package com.github.codogma.codogmaback.interceptor.localization;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;

@Component
@RequiredArgsConstructor
public class CustomLocaleResolver implements LocaleResolver {

  private final LocalizationContext localizationContext;

  @Override
  public Locale resolveLocale(HttpServletRequest request) {
    String localeCode = localizationContext.getLocale();
    if (localeCode == null || localeCode.isBlank()) {
      return Locale.ENGLISH;
    }
    return Locale.forLanguageTag(localeCode);
  }

  @Override
  public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
  }
}
