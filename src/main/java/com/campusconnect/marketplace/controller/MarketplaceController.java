package com.campusconnect.marketplace.controller;

import com.campusconnect.common.ApiResponse;
import com.campusconnect.enums.MarketplaceItemStatus;
import com.campusconnect.marketplace.dto.CreateMarketplaceItemRequest;
import com.campusconnect.marketplace.dto.MarketplaceItemResponse;
import com.campusconnect.marketplace.service.MarketplaceService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/marketplace")
@RequiredArgsConstructor
public class MarketplaceController {

  private final MarketplaceService marketplaceService;

  @PostMapping
  public ResponseEntity<ApiResponse<MarketplaceItemResponse>> createItem(
      @Valid @RequestBody CreateMarketplaceItemRequest request
  ) {
    return ResponseEntity.ok(ApiResponse.success(
        "Marketplace item created successfully",
        marketplaceService.createItem(request)
    ));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<MarketplaceItemResponse>>> listItems(
      @RequestParam(required = false) String category,
      @RequestParam(required = false) MarketplaceItemStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    return ResponseEntity.ok(ApiResponse.success(
        "Marketplace items fetched successfully",
        marketplaceService.listItems(category, status, page, size)
    ));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<MarketplaceItemResponse>> getItem(@PathVariable Long id) {
    return ResponseEntity.ok(ApiResponse.success(
        "Marketplace item fetched successfully",
        marketplaceService.getItemById(id)
    ));
  }

  @GetMapping("/my-listings")
  public ResponseEntity<ApiResponse<List<MarketplaceItemResponse>>> getMyListings() {
    return ResponseEntity.ok(ApiResponse.success(
        "Marketplace listings fetched successfully",
        marketplaceService.getMyListings()
    ));
  }

  @PatchMapping("/{id}/mark-sold")
  public ResponseEntity<ApiResponse<MarketplaceItemResponse>> markSold(@PathVariable Long id) {
    return ResponseEntity.ok(ApiResponse.success(
        "Marketplace item marked as sold",
        marketplaceService.markSold(id)
    ));
  }
}
