package com.github.codogma.codogmaback.interceptor.localization;

import com.github.codogma.codogmaback.model.Language;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class LocalizationContext {

  private static final ThreadLocal<Language> localeHolder = new ThreadLocal<>();
  private static final ThreadLocal<List<Language>> supportedLanguagesHolder = new ThreadLocal<>();

  public Language getLocale() {
    return localeHolder.get();
  }

  public void setLocale(Language locale) {
    localeHolder.set(locale);
  }

  public List<Language> getSupportedLanguages() {
    return supportedLanguagesHolder.get();
  }

  public void setSupportedLanguages(List<Language> languages) {
    supportedLanguagesHolder.set(languages);
  }

  public void clear() {
    localeHolder.remove();
    supportedLanguagesHolder.remove();
  }
}
