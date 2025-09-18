package com.gym.adminservice.Dto.Statuses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MemberRequestStatusForTrainer {
    private String status;
    private String date;
}
