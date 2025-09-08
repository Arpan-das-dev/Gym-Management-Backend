package com.gym.adminservice.Dto.ProductDtos.Responses;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@AllArgsConstructor
@Data
public class CreateProductResponseDto {
    private String productCode;
    private String description;
    private List<ProductFlavourResponseDto> flavours;
}
