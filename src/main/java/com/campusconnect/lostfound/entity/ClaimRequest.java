package com.campusconnect.lostfound.entity;

import com.campusconnect.common.BaseEntity;
import com.campusconnect.enums.ClaimStatus;
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
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "claim_requests",
    indexes = {
        @Index(name = "idx_claim_post_id", columnList = "post_id"),
        @Index(name = "idx_claim_claimer_id", columnList = "claimer_id"),
        @Index(name = "idx_claim_status", columnList = "status"),
        @Index(name = "idx_claim_created_at", columnList = "created_at"),
        // Supports the "only one PENDING claim per user per post" rule efficiently.
        @Index(name = "idx_claim_post_claimer_status", columnList = "post_id, claimer_id, status")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimRequest extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "post_id", nullable = false)
  private LostFoundPost post;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "claimer_id", nullable = false)
  private User claimer;

  @NotBlank
  @Size(min = 10, max = 1000)
  @Column(nullable = false, length = 1000)
  private String message;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  @Builder.Default
  private ClaimStatus status = ClaimStatus.PENDING;
}
