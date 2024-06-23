package com.github.airatgaliev.itblogback.config;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import com.github.airatgaliev.itblogback.model.Role;
import com.github.airatgaliev.itblogback.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
public class SecurityConfiguration {

  private final AuthenticationProvider authenticationProvider;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http.csrf(AbstractHttpConfigurer::disable).authorizeHttpRequests(
            auth -> auth.requestMatchers("/auth/**").permitAll().requestMatchers("/api-docs/**")
                .permitAll().requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll().requestMatchers("/v3/api-docs/**")
                .permitAll()
                .requestMatchers("/posts/**").permitAll()
                .requestMatchers("/posts/**").hasAnyRole(Role.AUTHOR.name(), Role.ADMIN.name())
                .requestMatchers(HttpMethod.GET, "/users/**").permitAll().requestMatchers("/users/**")
                .hasRole(Role.ADMIN.name()).requestMatchers("/subscriptions/**")
                .hasRole(Role.USER.name()).anyRequest().authenticated())
        .sessionManagement(manager -> manager.sessionCreationPolicy(STATELESS))
        .authenticationProvider(authenticationProvider)
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }
}
