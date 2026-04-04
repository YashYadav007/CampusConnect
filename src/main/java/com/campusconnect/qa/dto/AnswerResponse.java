package com.campusconnect.qa.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerResponse {
  private Long id;
  private String content;
  private AuthorResponse user;
  private LocalDateTime createdAt;
  private Boolean isAccepted;
  private Long upvoteCount;
  private Long downvoteCount;
  private Long score;
}
