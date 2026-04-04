package com.campusconnect.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
  private String secret;
  private long expirationMs;

  @PostConstruct
  public void validateSecret() {
    if (secret == null || secret.length() < 32) {
      throw new RuntimeException("JWT secret must be at least 32 characters long");
    }
  }
}
