package com.campusconnect.marketplace.repository;

import com.campusconnect.enums.MarketplaceItemStatus;
import com.campusconnect.marketplace.entity.MarketplaceItem;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MarketplaceItemRepository extends JpaRepository<MarketplaceItem, Long> {

  @EntityGraph(attributePaths = {"seller", "reservedBy"})
  Page<MarketplaceItem> findAllByOrderByCreatedAtDesc(Pageable pageable);

  @EntityGraph(attributePaths = {"seller", "reservedBy"})
  Page<MarketplaceItem> findByCategoryIgnoreCaseOrderByCreatedAtDesc(String category, Pageable pageable);

  @EntityGraph(attributePaths = {"seller", "reservedBy"})
  Page<MarketplaceItem> findByStatusOrderByCreatedAtDesc(MarketplaceItemStatus status, Pageable pageable);

  @EntityGraph(attributePaths = {"seller", "reservedBy"})
  Page<MarketplaceItem> findByCategoryIgnoreCaseAndStatusOrderByCreatedAtDesc(
      String category,
      MarketplaceItemStatus status,
      Pageable pageable
  );

  @EntityGraph(attributePaths = {"seller", "reservedBy"})
  Optional<MarketplaceItem> findWithUsersById(Long id);

  @EntityGraph(attributePaths = {"seller", "reservedBy"})
  List<MarketplaceItem> findBySellerIdOrderByCreatedAtDesc(Long sellerId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select i from MarketplaceItem i where i.id = :id")
  Optional<MarketplaceItem> findByIdForUpdate(@Param("id") Long id);
}
