package com.github.airatgaliev.itblogback.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

  @Value("${token.signing.key}")
  private String jwtSigningKey;

  private final long expiration = 1000 * 60 * 60 * 10; // 10 часов

  private SecretKey getSigningKey() {
    byte[] keyBytes = Decoders.BASE64.decode(jwtSigningKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  public String generateToken(String username) {
    Map<String, Object> claims = new HashMap<>();
    return Jwts.builder()
        .claims(claims)
        .subject(username)
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(getSigningKey())
        .compact();
  }

  public boolean validateToken(String token, String username) {
    final String extractedUsername = extractUsername(token);
    return (extractedUsername.equals(username) && !isTokenExpired(token));
  }

  public String extractUsername(String token) {
    return extractAllClaims(token).getSubject();
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token)
        .getPayload();
  }

  private boolean isTokenExpired(String token) {
    return extractAllClaims(token).getExpiration().before(new Date());
  }
}