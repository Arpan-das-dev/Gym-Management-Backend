package com.gym.member_service.Controllers;

import com.gym.member_service.Dto.MemberPlanDto.Requests.MembersPlanMeticsRequestDto;
import com.gym.member_service.Dto.MemberPlanDto.Requests.PlanRequestDto;
import com.gym.member_service.Dto.MemberPlanDto.Responses.MemberPlansMeticsResponseDto;
import com.gym.member_service.Services.FeatureServices.MemberPlanSerVice;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${member-service.BASE_URL}")
@RequiredArgsConstructor
/*
 * This controller is responsible for interaction with database
 * using MemberPlanService Class
 * @RequestMapping: map this controller on specific url
 * which is defined in application.properties
 */
public class MemberPlanController {

    private final MemberPlanSerVice planSerVice;

    /*
     * using logic present in MemberFitService
     * this method update plan for a member
     * and take memberId(@RequestParam) and a request dto(@RequestBody)
     * as method parameter
     */
    @PostMapping("plan")
    public ResponseEntity<String> updatePlanInfo(@RequestParam String id,
                                                  @RequestBody PlanRequestDto requestDto)
    {   // set the response using method present in MemberFitService
        String response = planSerVice.updatePlan(id,requestDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        // if successfully updates then returns ACCEPTED as http status
    }

    /*
     * using logic present in MemberFitService
     * this method returns all metrics of each plan
     * and take  request dto(@RequestBody) which contains list of all plan name
     * as method parameter
     */
    @GetMapping("admin/matrics")
    public ResponseEntity<List<MemberPlansMeticsResponseDto>> getAllMetrics (@RequestBody @Valid
                                                                       MembersPlanMeticsRequestDto requestDto)
    {   // set the response using method present in MemberFitService
        List<MemberPlansMeticsResponseDto> response = planSerVice.getAllMatrices(requestDto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
        // if successfully retrieves data from data then returns ACCEPTED as http status
    }
}
