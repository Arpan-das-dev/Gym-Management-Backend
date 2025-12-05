package com.gym.member_service.Dto.MemberTrainerDtos.Wrapper;

import com.gym.member_service.Dto.MemberTrainerDtos.Responses.SessionsResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AllSessionInfoResponseDto {
    List<SessionsResponseDto> sessionsResponseDtoList;
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private int totalPages;

    private boolean lastPage;
}
