package com.gym.adminservice.Dto.ProductDtos.Responses;


import com.gym.adminservice.Enums.ProductCategories;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@AllArgsConstructor
@Data
public class CreateProductResponseDto {
    private String productName;
    private String productCode;
    private ProductCategories category;
    private String brand;
    private String description;
    private List<ProductFlavourResponseDto> flavours;
}
