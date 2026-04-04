package com.campusconnect.admin.dto;

import com.campusconnect.common.dto.UserSummaryResponse;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAnswerResponse {
  private Long id;
  private String content;
  private UserSummaryResponse author;
  private LocalDateTime createdAt;
  private Boolean isAccepted;
  private Long upvoteCount;
  private Long downvoteCount;
  private Long score;
}
