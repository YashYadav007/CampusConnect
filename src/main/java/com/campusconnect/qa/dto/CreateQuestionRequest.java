package com.campusconnect.qa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuestionRequest {

  @NotBlank
  @Size(min = 5, max = 200)
  private String title;

  @Size(max = 20000)
  private String description;

  @Size(max = 10)
  private List<String> tags;
}
