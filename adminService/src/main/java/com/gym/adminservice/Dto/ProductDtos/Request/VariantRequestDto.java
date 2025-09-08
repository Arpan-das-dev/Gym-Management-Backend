package com.gym.adminservice.Dto.ProductDtos.Request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@AllArgsConstructor
@Data
public class VariantRequestDto {
    @NotNull(message = "MRP cannot be null")
    @Positive(message = "MRP must be greater than 0")
    private Integer mrp;

    @NotNull(message = "Selling price cannot be null")
    @Positive(message = "Selling price must be greater than 0")
    private Integer sellingPrice;

    @NotNull(message = "Weight cannot be null")
    @Positive(message = "Weight must be greater than 0")
    private Integer weight;

    @NotBlank(message = "Unit cannot be blank (e.g., kg, g, ml)")
    private String unit;

    @NotBlank(message = "Please provide the quantity")
    private Integer quantity;

    @Valid
    private List<ProductImageDto> productImageDtoList;
}
