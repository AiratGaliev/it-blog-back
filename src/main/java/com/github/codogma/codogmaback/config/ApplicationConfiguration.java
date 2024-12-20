package com.github.codogma.codogmaback.config;


import com.github.codogma.codogmaback.exception.ExceptionFactory;
import com.github.codogma.codogmaback.interceptor.localization.LocalizationInterceptor;
import com.github.codogma.codogmaback.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.EncodedResourceResolver;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfiguration implements WebMvcConfigurer {

  private final UserRepository userRepository;
  private final LocalizationInterceptor localizationInterceptor;
  private final ExceptionFactory exceptionFactory;

  @Value("${user.avatar.upload-dir}")
  private String avatarUploadDir;
  @Value("${article.image.upload-dir}")
  private String articleImageUploadDir;
  @Value("${category.image.upload-dir}")
  private String categoryImageUploadDir;
  @Value("${compilation.image.upload-dir}")
  private String compilationImageUploadDir;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/users/avatars/**")
        .addResourceLocations("file:" + avatarUploadDir + "/");
    registry.addResourceHandler("/articles/images/**")
        .addResourceLocations("file:" + articleImageUploadDir + "/");
    registry.addResourceHandler("/categories/images/**")
        .addResourceLocations("file:" + categoryImageUploadDir + "/");
    registry.addResourceHandler("/compilations/images/**")
        .addResourceLocations("file:" + compilationImageUploadDir + "/").setCachePeriod(3600)
        .resourceChain(true).addResolver(new EncodedResourceResolver());
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(localizationInterceptor);
  }

  @Bean
  UserDetailsService userDetailsService() {
    return usernameOrEmail -> userRepository.findByUsername(usernameOrEmail)
        .or(() -> userRepository.findByEmail(usernameOrEmail))
        .orElseThrow(exceptionFactory::usernameOrEmailNotFound);
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService());
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }
}
