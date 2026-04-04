package com.campusconnect.config;

import com.campusconnect.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {

  private final JwtProperties jwtProperties;

  public long getExpirationMs() {
    return jwtProperties.getExpirationMs();
  }

  public String generateToken(User user) {
    Map<String, Object> claims = Map.of(
        "roles", user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet())
    );
    return buildToken(claims, user.getEmail());
  }

  public String extractUsername(String token) {
    return extractAllClaims(token).getSubject();
  }

  public boolean validateToken(String token, String expectedUsername) {
    String username = extractUsername(token);
    return username != null && username.equals(expectedUsername) && !isTokenExpired(token);
  }

  private String buildToken(Map<String, Object> extraClaims, String subject) {
    long now = System.currentTimeMillis();
    Date issuedAt = new Date(now);
    Date expiry = new Date(now + jwtProperties.getExpirationMs());

    return Jwts.builder()
        .setClaims(extraClaims)
        .setSubject(subject)
        .setIssuedAt(issuedAt)
        .setExpiration(expiry)
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  private boolean isTokenExpired(String token) {
    Date exp = extractAllClaims(token).getExpiration();
    return exp.before(new Date());
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(getSigningKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  private Key getSigningKey() {
    String secret = jwtProperties.getSecret();
    if (secret == null || secret.isBlank()) {
      throw new IllegalStateException("JWT secret is not configured (app.jwt.secret)");
    }

    // Support plain-text secrets in dev; keep it simple and predictable.
    byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
    if (keyBytes.length < 32) {
      throw new IllegalStateException("JWT secret must be at least 32 characters for HS256");
    }

    return Keys.hmacShaKeyFor(keyBytes);
  }
}
