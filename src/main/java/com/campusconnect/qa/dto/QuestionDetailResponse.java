package com.campusconnect.qa.dto;

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
public class QuestionDetailResponse {
  private Long id;
  private String title;
  private String description;
  private AuthorResponse user;
  private List<String> tags;
  private LocalDateTime createdAt;
  private List<AnswerResponse> answers;
}
