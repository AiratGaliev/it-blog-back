package com.github.airatgaliev.itblogback.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.airatgaliev.itblogback.dto.AuthRequest;
import com.github.airatgaliev.itblogback.dto.AuthResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

  private final AuthenticationManager authenticationManager;
  private final JwtUtil jwtUtil;

  @Override
  public Authentication attemptAuthentication(HttpServletRequest request,
      HttpServletResponse response) {
    try {
      AuthRequest authRequest = new ObjectMapper().readValue(request.getInputStream(),
          AuthRequest.class);
      return authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(authRequest.getUsername(),
              authRequest.getPassword(), new ArrayList<>())
      );
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain, Authentication authResult) throws IOException, ServletException {
    String token = jwtUtil.generateToken(authResult.getName());
    response.setContentType("application/json");
    response.getWriter().write(new ObjectMapper().writeValueAsString(new AuthResponse(token)));
  }
}