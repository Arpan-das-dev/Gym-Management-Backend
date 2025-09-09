package com.gym.adminservice.Dto.ProductDtos.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductVariantResponseDto {
    private Integer mrp;
    private Integer sellingPrice;
    private Integer weight;
    private String  unit;
    private Integer quantity;
    private List<String> url;
}
