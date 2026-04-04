package com.campusconnect.lostfound.controller;

import com.campusconnect.common.ApiResponse;
import com.campusconnect.lostfound.dto.ClaimResponseDto;
import com.campusconnect.lostfound.service.ClaimService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
public class ClaimController {

  private final ClaimService claimService;

  @PostMapping("/{id}/approve")
  public ResponseEntity<ApiResponse<ClaimResponseDto>> approve(@PathVariable Long id) {
    return ResponseEntity.ok(ApiResponse.success("Claim approved successfully", claimService.approveClaim(id)));
  }

  @PostMapping("/{id}/reject")
  public ResponseEntity<ApiResponse<ClaimResponseDto>> reject(@PathVariable Long id) {
    return ResponseEntity.ok(ApiResponse.success("Claim rejected successfully", claimService.rejectClaim(id)));
  }
}

