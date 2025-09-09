package com.gym.adminservice.Dto.ProductDtos.Request;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@Builder
public class ProductImageDto {
    private Object image;
    public boolean isUrl(){
        if(image instanceof String) return true;
        return false;
    }
}
