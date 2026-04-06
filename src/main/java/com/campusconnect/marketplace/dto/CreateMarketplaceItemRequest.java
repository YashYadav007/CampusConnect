package com.campusconnect.marketplace.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMarketplaceItemRequest {

  @NotBlank
  @Size(min = 5, max = 200)
  private String title;

  private String description;

  @NotBlank
  @Size(min = 2, max = 50)
  private String category;

  @NotBlank
  @Size(min = 2, max = 30)
  private String conditionLabel;

  @NotNull
  @DecimalMin(value = "0.01")
  private BigDecimal price;

  @NotNull
  @DecimalMin(value = "0.01")
  private BigDecimal tokenAmount;

  private String imageUrl;
}
