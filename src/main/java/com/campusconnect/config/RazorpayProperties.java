package com.campusconnect.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.razorpay")
public class RazorpayProperties {

  private String keyId;
  private String keySecret;

  @PostConstruct
  public void validate() {
    if (isBlank(keyId) || isBlank(keySecret)) {
      throw new IllegalStateException("Razorpay configuration is missing. Set RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET.");
    }
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isBlank();
  }
}
