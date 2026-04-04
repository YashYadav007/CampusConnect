package com.campusconnect.auth.controller;

import com.campusconnect.auth.dto.AuthResponse;
import com.campusconnect.auth.dto.LoginRequest;
import com.campusconnect.auth.dto.RegisterRequest;
import com.campusconnect.auth.service.AuthService;
import com.campusconnect.common.ApiResponse;
import com.campusconnect.user.dto.UserResponse;
import com.campusconnect.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final UserService userService;

  @PostMapping("/register")
  public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest req) {
    return ResponseEntity.ok(ApiResponse.success("User registered", authService.register(req)));
  }

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest req) {
    return ResponseEntity.ok(ApiResponse.success("Login successful", authService.login(req)));
  }

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<UserResponse>> me() {
    return ResponseEntity.ok(ApiResponse.success("Current user", userService.toResponse(userService.getCurrentUserEntity())));
  }
}
