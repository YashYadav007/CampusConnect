package com.campusconnect.marketplace.service;

import com.campusconnect.common.dto.UserSummaryResponse;
import com.campusconnect.marketplace.dto.MarketplaceItemResponse;
import com.campusconnect.marketplace.dto.MarketplaceItemSummary;
import com.campusconnect.marketplace.dto.PaymentTransactionResponse;
import com.campusconnect.marketplace.entity.MarketplaceItem;
import com.campusconnect.marketplace.entity.PaymentTransaction;
import com.campusconnect.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class MarketplaceMapper {

  public MarketplaceItemResponse toItemResponse(MarketplaceItem item) {
    return MarketplaceItemResponse.builder()
        .id(item.getId())
        .title(item.getTitle())
        .description(item.getDescription())
        .category(item.getCategory())
        .conditionLabel(item.getConditionLabel())
        .price(item.getPrice())
        .tokenAmount(item.getTokenAmount())
        .imageUrl(item.getImageUrl())
        .status(item.getStatus())
        .seller(toUserSummary(item.getSeller()))
        .sellerEmail(item.getSeller().getEmail())
        .reservedBy(item.getReservedBy() == null ? null : toUserSummary(item.getReservedBy()))
        .createdAt(item.getCreatedAt())
        .build();
  }

  public MarketplaceItemSummary toItemSummary(MarketplaceItem item) {
    return MarketplaceItemSummary.builder()
        .id(item.getId())
        .title(item.getTitle())
        .price(item.getPrice())
        .tokenAmount(item.getTokenAmount())
        .status(item.getStatus())
        .build();
  }

  public PaymentTransactionResponse toPaymentResponse(PaymentTransaction tx) {
    return PaymentTransactionResponse.builder()
        .id(tx.getId())
        .itemId(tx.getMarketplaceItem().getId())
        .buyer(toUserSummary(tx.getBuyer()))
        .amount(tx.getAmount())
        .status(tx.getStatus())
        .razorpayOrderId(tx.getRazorpayOrderId())
        .razorpayPaymentId(tx.getRazorpayPaymentId())
        .createdAt(tx.getCreatedAt())
        .build();
  }

  public UserSummaryResponse toUserSummary(User user) {
    return UserSummaryResponse.builder()
        .id(user.getId())
        .fullName(user.getFullName())
        .build();
  }
}
