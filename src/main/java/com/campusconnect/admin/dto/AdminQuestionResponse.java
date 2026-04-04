package com.campusconnect.admin.dto;

import com.campusconnect.common.dto.UserSummaryResponse;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminQuestionResponse {
  private Long id;
  private String title;
  private UserSummaryResponse author;
  private List<String> tags;
  private Long answerCount;
  private LocalDateTime createdAt;
}
