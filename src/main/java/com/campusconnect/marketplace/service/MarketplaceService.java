package com.campusconnect.marketplace.service;

import com.campusconnect.common.exception.BadRequestException;
import com.campusconnect.common.exception.ResourceNotFoundException;
import com.campusconnect.common.exception.UnauthorizedException;
import com.campusconnect.enums.MarketplaceItemStatus;
import com.campusconnect.marketplace.dto.CreateMarketplaceItemRequest;
import com.campusconnect.marketplace.dto.MarketplaceItemResponse;
import com.campusconnect.marketplace.entity.MarketplaceItem;
import com.campusconnect.marketplace.repository.MarketplaceItemRepository;
import com.campusconnect.user.entity.User;
import com.campusconnect.user.service.UserService;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MarketplaceService {

  private static final int MAX_PAGE_SIZE = 50;

  private final MarketplaceItemRepository marketplaceItemRepository;
  private final MarketplaceMapper marketplaceMapper;
  private final UserService userService;

  @Transactional
  public MarketplaceItemResponse createItem(CreateMarketplaceItemRequest request) {
    User seller = userService.getCurrentUserEntity();

    String title = requireTrimmed(request.getTitle(), "title");
    String category = requireTrimmed(request.getCategory(), "category");
    String conditionLabel = requireTrimmed(request.getConditionLabel(), "conditionLabel");
    String description = normalizeOptionalText(request.getDescription());
    String imageUrl = normalizeOptionalText(request.getImageUrl());

    BigDecimal price = requirePositive(request.getPrice(), "price");
    BigDecimal tokenAmount = requirePositive(request.getTokenAmount(), "tokenAmount");
    if (tokenAmount.compareTo(price) > 0) {
      throw new BadRequestException("tokenAmount must be less than or equal to price");
    }

    MarketplaceItem item = MarketplaceItem.builder()
        .seller(seller)
        .title(title)
        .description(description)
        .category(category)
        .conditionLabel(conditionLabel)
        .price(price)
        .tokenAmount(tokenAmount)
        .imageUrl(imageUrl)
        .status(MarketplaceItemStatus.AVAILABLE)
        .build();

    MarketplaceItem saved = marketplaceItemRepository.save(item);
    log.info("Marketplace item created: id={} seller={}", saved.getId(), seller.getEmail());
    return marketplaceMapper.toItemResponse(saved);
  }

  @Transactional(readOnly = true)
  public List<MarketplaceItemResponse> listItems(String category, MarketplaceItemStatus status, int page, int size) {
    Pageable pageable = PageRequest.of(Math.max(page, 0), clampPageSize(size));
    String normalizedCategory = normalizeOptionalText(category);

    Page<MarketplaceItem> items;
    if (normalizedCategory != null && status != null) {
      items = marketplaceItemRepository.findByCategoryIgnoreCaseAndStatusOrderByCreatedAtDesc(normalizedCategory, status, pageable);
    } else if (normalizedCategory != null) {
      items = marketplaceItemRepository.findByCategoryIgnoreCaseOrderByCreatedAtDesc(normalizedCategory, pageable);
    } else if (status != null) {
      items = marketplaceItemRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
    } else {
      items = marketplaceItemRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    return items.getContent().stream().map(marketplaceMapper::toItemResponse).toList();
  }

  @Transactional(readOnly = true)
  public MarketplaceItemResponse getItemById(Long id) {
    MarketplaceItem item = marketplaceItemRepository.findWithUsersById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Marketplace item not found"));
    return marketplaceMapper.toItemResponse(item);
  }

  @Transactional(readOnly = true)
  public List<MarketplaceItemResponse> getMyListings() {
    User seller = userService.getCurrentUserEntity();
    return marketplaceItemRepository.findBySellerIdOrderByCreatedAtDesc(seller.getId()).stream()
        .map(marketplaceMapper::toItemResponse)
        .toList();
  }

  @Transactional
  public MarketplaceItemResponse markSold(Long itemId) {
    User seller = userService.getCurrentUserEntity();
    MarketplaceItem item = marketplaceItemRepository.findByIdForUpdate(itemId)
        .orElseThrow(() -> new ResourceNotFoundException("Marketplace item not found"));

    if (!item.getSeller().getId().equals(seller.getId())) {
      throw new UnauthorizedException("You can only mark your own listing as sold");
    }

    if (item.getStatus() == MarketplaceItemStatus.SOLD) {
      throw new BadRequestException("Item is already marked as SOLD");
    }

    item.setStatus(MarketplaceItemStatus.SOLD);
    MarketplaceItem saved = marketplaceItemRepository.save(item);
    log.info("Marketplace item marked sold: id={} seller={}", itemId, seller.getEmail());
    return marketplaceMapper.toItemResponse(saved);
  }

  @Transactional(readOnly = true)
  public MarketplaceItem getItemEntity(Long itemId) {
    return marketplaceItemRepository.findWithUsersById(itemId)
        .orElseThrow(() -> new ResourceNotFoundException("Marketplace item not found"));
  }

  private int clampPageSize(int size) {
    if (size <= 0) {
      return 20;
    }
    return Math.min(size, MAX_PAGE_SIZE);
  }

  private BigDecimal requirePositive(BigDecimal value, String field) {
    if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
      throw new BadRequestException(field + " must be greater than 0");
    }
    return value;
  }

  private String normalizeOptionalText(String raw) {
    if (raw == null) {
      return null;
    }
    String trimmed = raw.trim();
    return trimmed.isBlank() ? null : trimmed;
  }

  private String requireTrimmed(String raw, String field) {
    String normalized = normalizeOptionalText(raw);
    if (normalized == null) {
      throw new BadRequestException(field + " is required");
    }
    return normalized;
  }
}
