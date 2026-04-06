package com.campusconnect.marketplace.repository;

import com.campusconnect.enums.PaymentStatus;
import com.campusconnect.marketplace.entity.PaymentTransaction;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

  @EntityGraph(attributePaths = {"marketplaceItem", "marketplaceItem.seller", "marketplaceItem.reservedBy", "buyer"})
  Optional<PaymentTransaction> findByRazorpayOrderId(String razorpayOrderId);

  @EntityGraph(attributePaths = {"marketplaceItem", "marketplaceItem.seller", "marketplaceItem.reservedBy", "buyer"})
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<PaymentTransaction> findForUpdateByRazorpayOrderId(String razorpayOrderId);

  @EntityGraph(attributePaths = {"marketplaceItem", "marketplaceItem.seller", "marketplaceItem.reservedBy", "buyer"})
  Optional<PaymentTransaction> findByRazorpayPaymentId(String razorpayPaymentId);

  Optional<PaymentTransaction> findFirstByMarketplaceItemIdAndStatus(Long marketplaceItemId, PaymentStatus status);
}
