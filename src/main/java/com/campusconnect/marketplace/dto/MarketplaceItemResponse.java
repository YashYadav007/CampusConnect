package com.campusconnect.marketplace.dto;

import com.campusconnect.common.dto.UserSummaryResponse;
import com.campusconnect.enums.MarketplaceItemStatus;
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
public class MarketplaceItemResponse {
  private Long id;
  private String title;
  private String description;
  private String category;
  private String conditionLabel;
  private BigDecimal price;
  private BigDecimal tokenAmount;
  private String imageUrl;
  private MarketplaceItemStatus status;
  private UserSummaryResponse seller;
  private String sellerEmail;
  private UserSummaryResponse reservedBy;
  private LocalDateTime createdAt;
}
