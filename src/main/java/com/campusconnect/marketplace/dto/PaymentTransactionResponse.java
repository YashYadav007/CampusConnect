package com.campusconnect.marketplace.dto;

import com.campusconnect.common.dto.UserSummaryResponse;
import com.campusconnect.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransactionResponse {
  private Long id;
  private Long itemId;
  private UserSummaryResponse buyer;
  private BigDecimal amount;
  private PaymentStatus status;
  private String razorpayOrderId;
  private String razorpayPaymentId;
  private LocalDateTime createdAt;
}
