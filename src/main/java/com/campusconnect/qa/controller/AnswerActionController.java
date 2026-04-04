package com.campusconnect.qa.controller;

import com.campusconnect.common.ApiResponse;
import com.campusconnect.qa.dto.AnswerResponse;
import com.campusconnect.qa.dto.VoteRequest;
import com.campusconnect.qa.service.AnswerService;
import com.campusconnect.qa.service.VoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/answers")
@RequiredArgsConstructor
public class AnswerActionController {

  private final VoteService voteService;
  private final AnswerService answerService;

  @PostMapping("/{id}/vote")
  public ResponseEntity<ApiResponse<AnswerResponse>> vote(
      @PathVariable Long id,
      @Valid @RequestBody VoteRequest req
  ) {
    return ResponseEntity.ok(ApiResponse.success("Vote recorded", voteService.vote(id, req.getVoteType())));
  }

  @PostMapping("/{id}/accept")
  public ResponseEntity<ApiResponse<AnswerResponse>> accept(@PathVariable Long id) {
    return ResponseEntity.ok(ApiResponse.success("Answer accepted", answerService.acceptAnswer(id)));
  }
}
