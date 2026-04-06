package com.campusconnect.marketplace.service;

import com.campusconnect.common.exception.BadRequestException;
import com.campusconnect.common.exception.ResourceNotFoundException;
import com.campusconnect.enums.MarketplaceItemStatus;
import com.campusconnect.enums.PaymentStatus;
import com.campusconnect.marketplace.dto.MarketplaceCheckoutResponse;
import com.campusconnect.marketplace.dto.PaymentTransactionResponse;
import com.campusconnect.marketplace.dto.VerifyMarketplacePaymentRequest;
import com.campusconnect.marketplace.entity.MarketplaceItem;
import com.campusconnect.marketplace.entity.PaymentTransaction;
import com.campusconnect.marketplace.repository.MarketplaceItemRepository;
import com.campusconnect.marketplace.repository.PaymentTransactionRepository;
import com.campusconnect.user.entity.User;
import com.campusconnect.user.service.UserService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.campusconnect.config.RazorpayProperties;

@Service
@Slf4j
@RequiredArgsConstructor
public class MarketplacePaymentService {

  private final MarketplaceItemRepository marketplaceItemRepository;
  private final PaymentTransactionRepository paymentTransactionRepository;
  private final MarketplaceMapper marketplaceMapper;
  private final UserService userService;
  private final RazorpayGateway razorpayGateway;
  private final RazorpayProperties razorpayProperties;

  @Transactional
  public MarketplaceCheckoutResponse createOrderForMarketplaceItem(Long itemId) {
    User buyer = userService.getCurrentUserEntity();
    MarketplaceItem item = marketplaceItemRepository.findByIdForUpdate(itemId)
        .orElseThrow(() -> new ResourceNotFoundException("Marketplace item not found"));

    validatePurchasable(item, buyer);

    long amountInPaise = toPaise(item.getTokenAmount());
    RazorpayOrderResult order = razorpayGateway.createOrder(amountInPaise, "marketplace-item-" + item.getId());

    PaymentTransaction tx = PaymentTransaction.builder()
        .marketplaceItem(item)
        .buyer(buyer)
        .amount(item.getTokenAmount())
        .razorpayOrderId(order.orderId())
        .status(PaymentStatus.CREATED)
        .build();

    try {
      PaymentTransaction saved = paymentTransactionRepository.save(tx);
      log.info("Marketplace order created: itemId={} buyer={} orderId={}", itemId, buyer.getEmail(), order.orderId());
      return MarketplaceCheckoutResponse.builder()
          .keyId(razorpayProperties.getKeyId())
          .amount(order.amount())
          .currency(order.currency())
          .orderId(order.orderId())
          .displayAmount(item.getTokenAmount())
          .item(marketplaceMapper.toItemSummary(item))
          .build();
    } catch (DataIntegrityViolationException ex) {
      throw new BadRequestException("Failed to create payment order");
    }
  }

  @Transactional
  public PaymentTransactionResponse verifyMarketplacePayment(VerifyMarketplacePaymentRequest request) {
    PaymentTransaction tx = paymentTransactionRepository.findForUpdateByRazorpayOrderId(request.getRazorpayOrderId())
        .orElseThrow(() -> new ResourceNotFoundException("Payment transaction not found"));

    MarketplaceItem item = marketplaceItemRepository.findByIdForUpdate(tx.getMarketplaceItem().getId())
        .orElseThrow(() -> new ResourceNotFoundException("Marketplace item not found"));

    if (!item.getId().equals(request.getItemId())) {
      throw new BadRequestException("Payment verification item mismatch");
    }

    User currentUser = userService.getCurrentUserEntity();
    if (!tx.getBuyer().getId().equals(currentUser.getId())) {
      throw new BadRequestException("This payment does not belong to the current user");
    }

    if (tx.getStatus() == PaymentStatus.SUCCESS) {
      return marketplaceMapper.toPaymentResponse(tx);
    }

    if (item.getStatus() != MarketplaceItemStatus.AVAILABLE) {
      throw new BadRequestException("Item is no longer available");
    }

    if (!verifySignature(request.getRazorpayOrderId(), request.getRazorpayPaymentId(), request.getRazorpaySignature())) {
      tx.setStatus(PaymentStatus.FAILED);
      tx.setRazorpayPaymentId(request.getRazorpayPaymentId().trim());
      tx.setRazorpaySignature(request.getRazorpaySignature().trim());
      paymentTransactionRepository.save(tx);
      throw new BadRequestException("Invalid payment signature");
    }

    tx.setRazorpayPaymentId(request.getRazorpayPaymentId().trim());
    tx.setRazorpaySignature(request.getRazorpaySignature().trim());
    tx.setStatus(PaymentStatus.SUCCESS);

    item.setStatus(MarketplaceItemStatus.RESERVED);
    item.setReservedBy(tx.getBuyer());

    PaymentTransaction saved = paymentTransactionRepository.save(tx);
    marketplaceItemRepository.save(item);

    log.info("Marketplace payment verified: itemId={} buyer={} paymentId={}", item.getId(), tx.getBuyer().getEmail(), saved.getRazorpayPaymentId());
    return marketplaceMapper.toPaymentResponse(saved);
  }

  private void validatePurchasable(MarketplaceItem item, User buyer) {
    if (item.getStatus() != MarketplaceItemStatus.AVAILABLE) {
      throw new BadRequestException("Item is not available for reservation");
    }
    if (item.getSeller().getId().equals(buyer.getId())) {
      throw new BadRequestException("Seller cannot reserve their own item");
    }
    if (paymentTransactionRepository.findFirstByMarketplaceItemIdAndStatus(item.getId(), PaymentStatus.SUCCESS).isPresent()) {
      throw new BadRequestException("Item is already reserved");
    }
  }

  private long toPaise(BigDecimal amount) {
    return amount.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValueExact();
  }

  private boolean verifySignature(String orderId, String paymentId, String signature) {
    String expected = hmacSha256(orderId.trim() + "|" + paymentId.trim(), razorpayProperties.getKeySecret());
    return expected.equals(signature.trim());
  }

  private String hmacSha256(String data, String secret) {
    try {
      Mac sha256Hmac = Mac.getInstance("HmacSHA256");
      sha256Hmac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      byte[] hash = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (Exception ex) {
      throw new IllegalStateException("Could not verify Razorpay signature", ex);
    }
  }
}
