package com.gym.member_service.Controllers;

import com.gym.member_service.Dto.MemberPlanDto.Requests.MembersPlanMeticsRequestDto;
import com.gym.member_service.Dto.MemberPlanDto.Requests.PlanRequestDto;
import com.gym.member_service.Dto.MemberPlanDto.Responses.MemberPlansMeticsResponseDto;
import com.gym.member_service.Services.MemberPlanSerVice;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${member-service.BASE_URL}")
@RequiredArgsConstructor
public class MemberPlanController {

    private final MemberPlanSerVice planSerVice;

    @PostMapping("plan")
    public ResponseEntity<String> updatePlanInfo(@RequestParam String id,
                                                 @Valid @RequestBody PlanRequestDto requestDto){
        String response = planSerVice.updatePlan(id,requestDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("admin/matrics")
    public ResponseEntity<List<MemberPlansMeticsResponseDto>> getAllMetrics (@RequestBody @Valid
                                                                       MembersPlanMeticsRequestDto requestDto){
        List<MemberPlansMeticsResponseDto> response = planSerVice.getAllMatrices(requestDto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
