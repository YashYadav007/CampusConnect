package com.campusconnect.marketplace.entity;

import com.campusconnect.common.BaseEntity;
import com.campusconnect.enums.MarketplaceItemStatus;
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
    name = "marketplace_items",
    indexes = {
        @Index(name = "idx_marketplace_item_status", columnList = "status"),
        @Index(name = "idx_marketplace_item_category", columnList = "category"),
        @Index(name = "idx_marketplace_item_created_at", columnList = "created_at")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketplaceItem extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "seller_id", nullable = false)
  private User seller;

  @Column(nullable = false, length = 200)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(nullable = false, length = 50)
  private String category;

  @Column(nullable = false, length = 30)
  private String conditionLabel;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal price;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal tokenAmount;

  private String imageUrl;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private MarketplaceItemStatus status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reserved_by_id")
  private User reservedBy;

  @PrePersist
  void prePersist() {
    if (status == null) {
      status = MarketplaceItemStatus.AVAILABLE;
    }
  }
}
