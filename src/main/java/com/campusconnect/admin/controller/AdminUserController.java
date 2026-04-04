package com.campusconnect.admin.controller;

import com.campusconnect.admin.dto.AdminUserResponse;
import com.campusconnect.admin.service.AdminService;
import com.campusconnect.common.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

  private final AdminService adminService;

  @GetMapping
  public ResponseEntity<ApiResponse<List<AdminUserResponse>>> listUsers() {
    return ResponseEntity.ok(ApiResponse.success("Admin users fetched", adminService.getUsers()));
  }

  @PatchMapping("/{id}/deactivate")
  public ResponseEntity<ApiResponse<AdminUserResponse>> deactivate(@PathVariable Long id) {
    return ResponseEntity.ok(ApiResponse.success("User deactivated", adminService.deactivateUser(id)));
  }

  @PatchMapping("/{id}/activate")
  public ResponseEntity<ApiResponse<AdminUserResponse>> activate(@PathVariable Long id) {
    return ResponseEntity.ok(ApiResponse.success("User activated", adminService.activateUser(id)));
  }
}
