package com.github.airatgaliev.itblogback.interceptor.localization;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class LocalizationContext {

  private static final ThreadLocal<String> localeHolder = new ThreadLocal<>();
  private static final ThreadLocal<List<String>> supportedLanguagesHolder = new ThreadLocal<>();

  public void setLocale(String locale) {
    localeHolder.set(locale);
  }

  public String getLocale() {
    return localeHolder.get();
  }

  public void setSupportedLanguages(List<String> languages) {
    supportedLanguagesHolder.set(languages);
  }

  public List<String> getSupportedLanguages() {
    return supportedLanguagesHolder.get();
  }

  public void clear() {
    localeHolder.remove();
    supportedLanguagesHolder.remove();
  }
}
