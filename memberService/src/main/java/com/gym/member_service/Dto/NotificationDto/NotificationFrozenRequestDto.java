package com.gym.member_service.Dto.NotificationDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationFrozenRequestDto {

    private String name;
    private String mailId;
    private LocalDate frozenDate;
    private boolean frozen;
}
