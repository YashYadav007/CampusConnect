package com.campusconnect.marketplace.controller;

import com.campusconnect.common.ApiResponse;
import com.campusconnect.marketplace.dto.MarketplaceCheckoutResponse;
import com.campusconnect.marketplace.dto.PaymentTransactionResponse;
import com.campusconnect.marketplace.dto.VerifyMarketplacePaymentRequest;
import com.campusconnect.marketplace.service.MarketplacePaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/marketplace")
@RequiredArgsConstructor
public class MarketplacePaymentController {

  private final MarketplacePaymentService marketplacePaymentService;

  @PostMapping("/{id}/create-order")
  public ResponseEntity<ApiResponse<MarketplaceCheckoutResponse>> createOrder(@PathVariable Long id) {
    return ResponseEntity.ok(ApiResponse.success(
        "Marketplace payment order created successfully",
        marketplacePaymentService.createOrderForMarketplaceItem(id)
    ));
  }

  @PostMapping("/payments/verify")
  public ResponseEntity<ApiResponse<PaymentTransactionResponse>> verifyPayment(
      @Valid @RequestBody VerifyMarketplacePaymentRequest request
  ) {
    return ResponseEntity.ok(ApiResponse.success(
        "Marketplace payment verified successfully",
        marketplacePaymentService.verifyMarketplacePayment(request)
    ));
  }
}
