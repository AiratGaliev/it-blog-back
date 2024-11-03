package com.github.airatgaliev.itblogback.interceptor.localization;

import com.github.airatgaliev.itblogback.model.Language;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class LocalizationInterceptor implements HandlerInterceptor {

  private final LocalizationContext localizationContext;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
      Object handler) {
    String intl = Arrays.stream(Optional.ofNullable(request.getCookies()).orElse(new Cookie[0]))
        .filter(cookie -> "intl".equals(cookie.getName())).map(Cookie::getValue)
        .filter(Language::isSupported).findFirst().orElse(Language.EN.getCode());
    localizationContext.setLocale(intl);

    String contlCookie = Arrays.stream(
            Optional.ofNullable(request.getCookies()).orElse(new Cookie[0]))
        .filter(cookie -> "contl".equals(cookie.getName())).map(Cookie::getValue).findFirst()
        .orElse(Language.EN.getCode());
    contlCookie = URLDecoder.decode(contlCookie, StandardCharsets.UTF_8);
    String[] languages = contlCookie.split(",");
    List<String> supportedLanguages = Arrays.stream(languages).map(String::trim)
        .filter(Language::isSupported).map(String::toUpperCase).toList();
    localizationContext.setSupportedLanguages(supportedLanguages);
    return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
      Object handler, Exception ex) {
    localizationContext.clear();
  }
}