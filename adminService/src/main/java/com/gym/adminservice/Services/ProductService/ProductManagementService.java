package com.gym.adminservice.Services.ProductService;

import com.gym.adminservice.Dto.ProductDtos.Request.ProductImageDto;
import com.gym.adminservice.Dto.ProductDtos.Request.ProductRequestDto;
import com.gym.adminservice.Dto.ProductDtos.Responses.CreateProductResponseDto;
import com.gym.adminservice.Dto.ProductDtos.Responses.ProductFlavourResponseDto;
import com.gym.adminservice.Dto.ProductDtos.Responses.ProductVariantResponseDto;
import com.gym.adminservice.Services.AwsServices.AWSS3Service;
import com.gym.adminservice.Utils.ProductIDGenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
/*
 * this service class is responsible for managing products by admin
 * as of now it only has a method to create a product but near future it will have methods to update, delete and get products
 * it uses the ProductIDGenUtil class to generate unique product codes and IDs
 * it uses the AWSS3Service class to upload product images to AWS S3 and get the public URLs of the images,
 * and also we will allow to download and delete images from S3 in future and edit product details
 */
public class ProductManagementService {

    private final ProductIDGenUtil idGenUtil;
    private final AWSS3Service awss3Service;

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

        // Generate Flavour and Variant Response DTOs with unique product IDs for each
        // flavour
        List<ProductFlavourResponseDto> flavourResponseDto = requestDto.getFlavours().stream()
                .map(flavour -> {
                    String productId = idGenUtil.generateProductId(productCode, flavour.getFlavourName());

                    return ProductFlavourResponseDto.builder()
                            .productId(productId)
                            .flavourName(flavour.getFlavourName())
                            .variantResponseDto(flavour.getVariants().stream()
                                    .map(variant -> ProductVariantResponseDto.builder()
                                            .mrp(variant.getMrp())
                                            .sellingPrice(variant.getSellingPrice())
                                            .quantity(variant.getQuantity())
                                            .weight(variant.getWeight())
                                            .unit(variant.getUnit())
                                            .url(processImages(productId, variant.getProductImageDtoList()))
                                            .build())
                                    .collect(Collectors.toList()))
                            .build();
                })
                .collect(Collectors.toList()); // collect flavours to list
        CreateProductResponseDto productResponseDto = CreateProductResponseDto.builder()
                .productName(requestDto.getProductName())
                .productCode(productCode)
                .brand(requestDto.getBrand())
                .category(requestDto.getCategory())
                .description(requestDto.getDescription())
                .flavours(flavourResponseDto) // set flavours which contains variants is get from above mapping
                .build();

    }

    private List<String> processImages(String productId, List<ProductImageDto> imageDtoList) {
        List<String> url = new LinkedList<>();
        if (imageDtoList.isEmpty()) {
            return Collections.emptyList();
        } else {
            for (ProductImageDto images : imageDtoList) {
                if (images.isUrl())
                    url.add((String) images.getImage());
                else {
                    MultipartFile file = (MultipartFile) images.getImage();
                    try {
                        String awsUrl = awss3Service.uploadImage(productId, file);
                        url.add(awsUrl);
                    } catch (RuntimeException e) {
                        throw new RuntimeException("S3 upload failed for productId: " + productId
                                + ", file: " + file.getOriginalFilename(), e);
                    }
                }
            }
        }
        return url;
    }

    public void DeleteProduct(String productCode){
        //
    }

   public void DeleteById(String id){
        // webclient will send the request later to product service, but we now just design the logic inside
       // the service class
   }
}
