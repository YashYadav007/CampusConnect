package com.campusconnect.marketplace.entity;

import com.campusconnect.common.BaseEntity;
import com.campusconnect.enums.PaymentStatus;
import com.campusconnect.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "payment_transactions",
    indexes = {
        @Index(name = "idx_payment_transaction_item_id", columnList = "marketplace_item_id"),
        @Index(name = "idx_payment_transaction_buyer_id", columnList = "buyer_id"),
        @Index(name = "idx_payment_transaction_status", columnList = "status"),
        @Index(name = "idx_payment_transaction_created_at", columnList = "created_at"),
        @Index(name = "idx_payment_transaction_order_id", columnList = "razorpay_order_id", unique = true),
        @Index(name = "idx_payment_transaction_payment_id", columnList = "razorpay_payment_id", unique = true)
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransaction extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "marketplace_item_id", nullable = false)
  private MarketplaceItem marketplaceItem;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "buyer_id", nullable = false)
  private User buyer;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal amount;

  @Column(nullable = false, length = 100)
  private String razorpayOrderId;

  @Column(length = 100)
  private String razorpayPaymentId;

  @Column(length = 255)
  private String razorpaySignature;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PaymentStatus status;

  @PrePersist
  void prePersist() {
    if (status == null) {
      status = PaymentStatus.CREATED;
    }
  }
}
