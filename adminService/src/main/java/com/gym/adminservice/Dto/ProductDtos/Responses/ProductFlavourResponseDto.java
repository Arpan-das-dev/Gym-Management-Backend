package com.gym.adminservice.Dto.ProductDtos.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductFlavourResponseDto {
    private String productId;
    private String flavourName;
    private List<ProductVariantResponseDto> variantResponseDto;
}
