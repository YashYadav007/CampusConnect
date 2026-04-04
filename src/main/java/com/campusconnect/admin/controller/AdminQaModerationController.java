package com.campusconnect.admin.controller;

import com.campusconnect.admin.dto.AdminAnswerResponse;
import com.campusconnect.admin.dto.AdminQuestionDetailResponse;
import com.campusconnect.admin.dto.AdminQuestionResponse;
import com.campusconnect.admin.service.AdminService;
import com.campusconnect.common.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminQaModerationController {

  private final AdminService adminService;

  @GetMapping("/questions")
  public ResponseEntity<ApiResponse<List<AdminQuestionResponse>>> listQuestions() {
    return ResponseEntity.ok(ApiResponse.success("Admin questions fetched", adminService.getQuestions()));
  }

  @GetMapping("/questions/{id}")
  public ResponseEntity<ApiResponse<AdminQuestionDetailResponse>> getQuestion(@PathVariable Long id) {
    return ResponseEntity.ok(ApiResponse.success("Admin question fetched", adminService.getQuestionDetail(id)));
  }

  @GetMapping("/questions/{id}/answers")
  public ResponseEntity<ApiResponse<List<AdminAnswerResponse>>> getQuestionAnswers(@PathVariable Long id) {
    return ResponseEntity.ok(ApiResponse.success("Admin answers fetched", adminService.getQuestionAnswers(id)));
  }

  @DeleteMapping("/questions/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteQuestion(@PathVariable Long id) {
    adminService.deleteQuestion(id);
    return ResponseEntity.ok(ApiResponse.success("Question deleted", null));
  }

  @DeleteMapping("/answers/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteAnswer(@PathVariable Long id) {
    adminService.deleteAnswer(id);
    return ResponseEntity.ok(ApiResponse.success("Answer deleted", null));
  }
}
