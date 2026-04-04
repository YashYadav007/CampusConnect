package com.campusconnect.qa.dto;

import com.campusconnect.enums.VoteType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoteRequest {

  @NotNull
  private VoteType voteType;
}
