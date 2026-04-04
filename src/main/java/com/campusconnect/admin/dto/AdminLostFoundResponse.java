package com.campusconnect.admin.dto;

import com.campusconnect.common.dto.UserSummaryResponse;
import com.campusconnect.enums.ItemStatus;
import com.campusconnect.enums.PostType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminLostFoundResponse {
  private Long id;
  private PostType type;
  private String title;
  private String description;
  private String imageUrl;
  private UserSummaryResponse owner;
  private String location;
  private LocalDate dateOfIncident;
  private ItemStatus status;
  private LocalDateTime createdAt;
}
