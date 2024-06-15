package com.github.airatgaliev.itblogback.config;

import com.github.airatgaliev.itblogback.model.ERole;
import com.github.airatgaliev.itblogback.security.JwtAuthenticationFilter;
import com.github.airatgaliev.itblogback.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

  private final CustomUserDetailsService userDetailsService;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable).authorizeHttpRequests(
        auth -> auth.requestMatchers("/auth/**").permitAll().requestMatchers("/api-docs/**")
            .permitAll().requestMatchers("/swagger-ui.html").permitAll()
            .requestMatchers("/swagger-ui/**").permitAll().requestMatchers("/v3/api-docs/**")
            .permitAll().requestMatchers("/posts/**")
            .hasAnyRole(ERole.AUTHOR.name(), ERole.ADMIN.name())
            .requestMatchers("/users/**").hasRole(ERole.ADMIN.name())
            .requestMatchers("/subscriptions/**").hasRole(ERole.USER.name()).anyRequest()
            .authenticated());
    http.authenticationProvider(authenticationProvider());
    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfiguration)
      throws Exception {
    return authConfiguration.getAuthenticationManager();
  }
}
