package com.campusconnect.lostfound.controller;

import com.campusconnect.common.ApiResponse;
import com.campusconnect.lostfound.dto.ClaimResponseDto;
import com.campusconnect.lostfound.dto.CreateClaimRequestDto;
import com.campusconnect.lostfound.service.ClaimService;
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
@RequestMapping("/api/lost-found")
@RequiredArgsConstructor
public class LostFoundClaimController {

  private final ClaimService claimService;

  @PostMapping("/{id}/claim")
  public ResponseEntity<ApiResponse<ClaimResponseDto>> createClaim(
      @PathVariable Long id,
      @Valid @RequestBody CreateClaimRequestDto req
  ) {
    return ResponseEntity.ok(ApiResponse.success("Claim submitted successfully", claimService.createClaim(id, req)));
  }

  @GetMapping("/{id}/claims")
  public ResponseEntity<ApiResponse<List<ClaimResponseDto>>> getClaimsForPost(@PathVariable Long id) {
    return ResponseEntity.ok(ApiResponse.success("Claims fetched successfully", claimService.getClaimsForPost(id)));
  }
}

