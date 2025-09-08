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
    @Pattern(
            regexp = "^(http|https)://.*$",
            message = "Image URL must be valid and start with http or https"
    )
    private String imageUrl;
    private MultipartFile imageFile;

    public boolean isUrl() {
        return imageUrl != null && !imageUrl.isBlank();
    }

    public boolean isFile() {
        return imageFile != null && !imageFile.isEmpty();
    }
}
