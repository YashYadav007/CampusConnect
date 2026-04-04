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
public class AdminQuestionDetailResponse {
  private Long id;
  private String title;
  private String description;
  private UserSummaryResponse author;
  private List<String> tags;
  private LocalDateTime createdAt;
  private List<AdminAnswerResponse> answers;
}
