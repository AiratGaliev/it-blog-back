package com.github.codogma.codogmaback.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

public class TokenUtils {

  public static String extractToken(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    }

    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if ("auth-token".equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }

  public static void setAuthCookie(HttpServletResponse response, String token) {
    ResponseCookie cookie = ResponseCookie.from("auth-token", token).httpOnly(true).secure(false)
        .path("/").build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }

  public static void invalidateToken(HttpServletResponse response) {
    ResponseCookie authTokenCookie = ResponseCookie.from("auth-token", "").httpOnly(true)
        .secure(false).path("/").maxAge(0).build();
    response.addHeader(HttpHeaders.SET_COOKIE, authTokenCookie.toString());
    ResponseCookie userCookie = ResponseCookie.from("user", "").httpOnly(true).secure(false)
        .path("/").maxAge(0).build();
    response.addHeader(HttpHeaders.SET_COOKIE, userCookie.toString());
  }
}
