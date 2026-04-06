package com.campusconnect.marketplace.dto;

import com.campusconnect.enums.MarketplaceItemStatus;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketplaceItemSummary {
  private Long id;
  private String title;
  private BigDecimal price;
  private BigDecimal tokenAmount;
  private MarketplaceItemStatus status;
}
