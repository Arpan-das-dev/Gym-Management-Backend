package com.gym.planService.Dtos.OrderDtos.Wrappers;

import com.gym.planService.Dtos.OrderDtos.Responses.ReceiptResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReceiptResponseWrapperDto {
    private List<ReceiptResponseDto> responseDtoList;
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private int totalPages;

    private boolean lastPage;
}
