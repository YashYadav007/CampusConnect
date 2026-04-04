package com.campusconnect.lostfound.entity;

import com.campusconnect.common.BaseEntity;
import com.campusconnect.enums.ItemStatus;
import com.campusconnect.enums.PostType;
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
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "lost_found_posts",
    indexes = {
        @Index(name = "idx_lf_type", columnList = "type"),
        @Index(name = "idx_lf_status", columnList = "status"),
        @Index(name = "idx_lf_created_at", columnList = "created_at"),
        @Index(name = "idx_lf_date_of_incident", columnList = "date_of_incident")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LostFoundPost extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private PostType type;

  @NotBlank
  @Size(min = 5, max = 200)
  @Column(nullable = false, length = 200)
  private String title;

  @Lob
  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(length = 2048)
  private String imageUrl;

  @NotBlank
  @Size(min = 2, max = 255)
  @Column(nullable = false, length = 255)
  private String location;

  @NotNull
  @Column(name = "date_of_incident", nullable = false)
  private LocalDate dateOfIncident;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  @Builder.Default
  private ItemStatus status = ItemStatus.OPEN;
}
