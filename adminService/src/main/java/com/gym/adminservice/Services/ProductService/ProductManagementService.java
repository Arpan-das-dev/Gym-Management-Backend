package com.gym.adminservice.Services.ProductService;

import com.gym.adminservice.Dto.ProductDtos.Request.ProductRequestDto;
import com.gym.adminservice.Dto.ProductDtos.Responses.CreateProductResponseDto;
import com.gym.adminservice.Dto.ProductDtos.Responses.ProductFlavourResponseDto;
import com.gym.adminservice.Dto.ProductDtos.Responses.ProductVariantResponseDto;
import com.gym.adminservice.Utils.ProductIDGenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class ProductManagementService {

    private final ProductIDGenUtil idGenUtil;

    /*
     * this method creates a new product with a unique product code and IDs for each
     * and product code will be generated using the ProductIDGenUtil class. and the
     * productCode can be
     * same for different products but the product id will be unique for each
     * product and their variations
     */
    public void CreateProduct(ProductRequestDto requestDto) {

        // Generate product code using the utility class
        String productCode = idGenUtil.genProductCode(
                requestDto.getProductName(), requestDto.getCategory().name(), requestDto.getBrand());

        // Generate Flavour and Variant Response DTOs with unique product IDs for each  flavour
        List<ProductFlavourResponseDto> flavourResponseDto = requestDto.getFlavours().stream()
                .map(flavour -> ProductFlavourResponseDto.builder() // using builder pattern and map
                        .productId(idGenUtil.generateProductId(productCode, flavour.getFlavourName())) // generate
                                                                                                       // unique product
                                                                                                       // id
                        .flavourName(flavour.getFlavourName()) // set flavour name
                        .variantResponseDto(flavour.getVariants().stream() // using stream to map variants
                                .map(variant -> ProductVariantResponseDto.builder()
                                        .mrp(variant.getMrp()) // set mrp
                                        .sellingPrice(variant.getSellingPrice()) // set selling price
                                        .quantity(variant.getQuantity()) // set quantity
                                        .weight(variant.getWeight()) // set weight
                                        .unit(variant.getUnit()) // set unit
                                        .url("") // set url as empty for now
                                        .build())
                                .collect(Collectors.toList())) // collect variants to list
                        .build())
                .collect(Collectors.toList()); // collect flavours to list
        CreateProductResponseDto productResponseDto = CreateProductResponseDto.builder()
                .productCode(productCode)
                .description(requestDto.getDescription())
                .flavours(flavourResponseDto) // set flavours which contains variants is get from above mapping
                .build();

    }

}
