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
public class QuestionResponse {
  private Long id;
  private String title;
  private String description;
  private AuthorResponse user;
  private List<String> tags;
  private Long answerCount;
  private LocalDateTime createdAt;
}
