package com.campusconnect.admin.controller;

import com.campusconnect.admin.dto.AdminClaimResponse;
import com.campusconnect.admin.dto.AdminLostFoundResponse;
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
public class AdminLostFoundModerationController {

  private final AdminService adminService;

  @GetMapping("/lost-found")
  public ResponseEntity<ApiResponse<List<AdminLostFoundResponse>>> listPosts() {
    return ResponseEntity.ok(ApiResponse.success("Admin lost & found posts fetched", adminService.getLostFoundPosts()));
  }

  @GetMapping("/lost-found/{id}")
  public ResponseEntity<ApiResponse<AdminLostFoundResponse>> getPost(@PathVariable Long id) {
    return ResponseEntity.ok(ApiResponse.success("Admin lost & found post fetched", adminService.getLostFoundPost(id)));
  }

  @DeleteMapping("/lost-found/{id}")
  public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long id) {
    adminService.deleteLostFoundPost(id);
    return ResponseEntity.ok(ApiResponse.success("Lost & found post deleted", null));
  }

  @GetMapping("/claims")
  public ResponseEntity<ApiResponse<List<AdminClaimResponse>>> listClaims() {
    return ResponseEntity.ok(ApiResponse.success("Admin claims fetched", adminService.getClaims()));
  }
}
