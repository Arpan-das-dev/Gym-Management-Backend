package com.gym.adminservice.Dto.ProductDtos.Request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
public class FlavourRequestDto {
    @NotBlank(message = "Flavour name cannot be empty")
    private String flavourName;

    @NotEmpty(message = "Select at least one product variant")
    @Valid
    private List<VariantRequestDto> variants;
}
