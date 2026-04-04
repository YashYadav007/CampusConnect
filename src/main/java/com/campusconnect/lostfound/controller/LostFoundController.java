package com.campusconnect.lostfound.controller;

import com.campusconnect.common.ApiResponse;
import com.campusconnect.enums.ItemStatus;
import com.campusconnect.enums.PostType;
import com.campusconnect.lostfound.dto.CreateLostFoundPostRequest;
import com.campusconnect.lostfound.dto.LostFoundPostResponse;
import com.campusconnect.lostfound.service.LostFoundService;
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
@RequestMapping("/api/lost-found")
@RequiredArgsConstructor
public class LostFoundController {

  private final LostFoundService lostFoundService;

  @PostMapping
  public ResponseEntity<ApiResponse<LostFoundPostResponse>> create(@Valid @RequestBody CreateLostFoundPostRequest req) {
    return ResponseEntity.ok(ApiResponse.success("Lost & found post created successfully", lostFoundService.createPost(req)));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<LostFoundPostResponse>>> list(
      @RequestParam(required = false) PostType type,
      @RequestParam(required = false) ItemStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    return ResponseEntity.ok(ApiResponse.success(
        "Lost & found posts fetched successfully",
        lostFoundService.filterPosts(type, status, page, size)
    ));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<LostFoundPostResponse>> get(@PathVariable Long id) {
    return ResponseEntity.ok(ApiResponse.success("Lost & found post fetched successfully", lostFoundService.getPostById(id)));
  }
}
