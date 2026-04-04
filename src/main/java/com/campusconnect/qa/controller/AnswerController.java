package com.campusconnect.qa.controller;

import com.campusconnect.common.ApiResponse;
import com.campusconnect.qa.dto.AnswerResponse;
import com.campusconnect.qa.dto.CreateAnswerRequest;
import com.campusconnect.qa.service.AnswerService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/questions/{id}/answers")
@RequiredArgsConstructor
public class AnswerController {

  private final AnswerService answerService;

  @PostMapping
  public ResponseEntity<ApiResponse<AnswerResponse>> addAnswer(
      @PathVariable Long id,
      @Valid @RequestBody CreateAnswerRequest req
  ) {
    return ResponseEntity.ok(ApiResponse.success("Answer added", answerService.addAnswer(id, req)));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<AnswerResponse>>> getAnswers(@PathVariable Long id) {
    return ResponseEntity.ok(ApiResponse.success("Answers fetched", answerService.getAnswersByQuestionId(id)));
  }
}
