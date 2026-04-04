package com.campusconnect.qa.controller;

import com.campusconnect.common.ApiResponse;
import com.campusconnect.qa.dto.CreateQuestionRequest;
import com.campusconnect.qa.dto.QuestionDetailResponse;
import com.campusconnect.qa.dto.QuestionResponse;
import com.campusconnect.qa.service.QuestionService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

  private final QuestionService questionService;

  @PostMapping
  public ResponseEntity<ApiResponse<QuestionResponse>> createQuestion(
      @Valid @RequestBody CreateQuestionRequest req
  ) {
    return ResponseEntity.ok(ApiResponse.success("Question created", questionService.createQuestion(req)));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<QuestionResponse>>> getAllQuestions(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    return ResponseEntity.ok(ApiResponse.success("Questions fetched", questionService.getAllQuestions(page, size)));
  }

  @GetMapping("/{id:\\d+}")
  public ResponseEntity<ApiResponse<QuestionDetailResponse>> getQuestionById(@PathVariable Long id) {
    return ResponseEntity.ok(ApiResponse.success("Question fetched", questionService.getQuestionById(id)));
  }

  @GetMapping("/search")
  public ResponseEntity<ApiResponse<List<QuestionResponse>>> searchQuestions(
      @RequestParam String keyword,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    return ResponseEntity.ok(ApiResponse.success("Questions fetched", questionService.searchQuestions(keyword, page, size)));
  }
}
