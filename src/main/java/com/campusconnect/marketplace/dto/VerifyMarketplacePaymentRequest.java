package com.campusconnect.marketplace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyMarketplacePaymentRequest {

  @NotNull
  private Long itemId;

  @NotBlank
  private String razorpayOrderId;

  @NotBlank
  private String razorpayPaymentId;

  @NotBlank
  private String razorpaySignature;
}
