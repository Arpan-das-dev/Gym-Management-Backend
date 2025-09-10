package com.gym.adminservice.Controllers.ProductController;

import com.gym.adminservice.Dto.ProductDtos.Request.ProductRequestDto;
import com.gym.adminservice.Services.ProductService.ProductManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${app.productService.managementUrl}")
@RequiredArgsConstructor
@Validated
public class ProductManagementController {

    private final ProductManagementService managementService;

    @PostMapping("createProduct")
    public ResponseEntity<String> createProduct(@Valid @RequestBody ProductRequestDto requestDto){
        managementService.CreateProduct(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Request for creating Product sent successfully");
    }


}
