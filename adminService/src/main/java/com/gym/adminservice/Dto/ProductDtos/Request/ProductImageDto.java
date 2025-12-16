package com.gym.adminservice.Dto.ProductDtos.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ProductImageDto {
    private Object image;
    public boolean isUrl(){
        return image instanceof String;
    }
}
