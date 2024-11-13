package com.github.codogma.codogmaback.interceptor.localization;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class LocalizationContext {

  private static final ThreadLocal<String> localeHolder = new ThreadLocal<>();
  private static final ThreadLocal<List<String>> supportedLanguagesHolder = new ThreadLocal<>();

  public String getLocale() {
    return localeHolder.get();
  }

  public void setLocale(String locale) {
    localeHolder.set(locale);
  }

  public List<String> getSupportedLanguages() {
    return supportedLanguagesHolder.get();
  }

  public void setSupportedLanguages(List<String> languages) {
    supportedLanguagesHolder.set(languages);
  }

  public void clear() {
    localeHolder.remove();
    supportedLanguagesHolder.remove();
  }
}
