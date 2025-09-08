package com.gym.adminservice.Dto.ProductDtos.Request;

import com.gym.adminservice.Enums.ProductCategories;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class ProductRequestDto {

    @NotBlank(message = "Product name cannot be empty")
    @Size(max = 100, message = "Product name must not exceed 100 characters")
    private String productName;

    @NotNull(message = "Please select the product category")
    private ProductCategories category;

    @NotBlank(message = "Select a brand")
    private String brand;
    @NotBlank(message = "Please provide a product description")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotEmpty(message = "Select at least one product flavour")
    @Valid
    private List<FlavourRequestDto> flavours;
}
