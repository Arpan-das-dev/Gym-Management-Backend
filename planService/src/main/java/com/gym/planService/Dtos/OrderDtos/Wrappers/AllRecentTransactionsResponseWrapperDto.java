package com.gym.planService.Dtos.OrderDtos.Wrappers;

import com.gym.planService.Dtos.OrderDtos.Responses.RecentTransactionsResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AllRecentTransactionsResponseWrapperDto {
   private List<RecentTransactionsResponseDto> responseDtoList;
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private int totalPages;

    private boolean lastPage;
}
