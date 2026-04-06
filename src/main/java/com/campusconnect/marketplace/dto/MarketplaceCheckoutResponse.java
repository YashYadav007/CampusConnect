package com.campusconnect.marketplace.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketplaceCheckoutResponse {
  private String keyId;
  private long amount;
  private String currency;
  private String orderId;
  private BigDecimal displayAmount;
  private MarketplaceItemSummary item;
}
