package com.gym.member_service.Controllers;

import com.gym.member_service.Dto.MemberManagementDto.Requests.FreezeRequestDto;
import com.gym.member_service.Dto.MemberManagementDto.Requests.MemberCreationRequestDto;
import com.gym.member_service.Dto.MemberManagementDto.Requests.PlanRequestDto;
import com.gym.member_service.Dto.MemberManagementDto.Responses.AllMemberResponseDto;
import com.gym.member_service.Services.MemberManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${member-service.BASE_URL}")
@RequiredArgsConstructor
@Validated
public class MemberManagementController {

    private final MemberManagementService memberManagementService;

    @PostMapping("create")
    public ResponseEntity<String> createMember (@Valid @RequestBody MemberCreationRequestDto requestDto){
        String  response = memberManagementService.createMember(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("member created successfully");
    }

    @PostMapping("admin/freeze")
    public ResponseEntity<String> freeze(@Valid @RequestBody FreezeRequestDto requestDto){
        String response =  memberManagementService.freezeOrUnFreezed(requestDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @PostMapping("plan")
    public ResponseEntity <String> updatePlanInfo(@RequestParam String id,
                                                  @Valid @RequestBody PlanRequestDto requestDto){
        String response = memberManagementService.updatePlan(id,requestDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("streak")
    public ResponseEntity<Integer> getLoginStreak(@RequestParam String id){
        Integer response = memberManagementService.setAndGetLoginStreak(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("getBy")
    public ResponseEntity<?> getMemberById(@RequestParam String id){
        return ResponseEntity.status(HttpStatus.OK).body(
                memberManagementService.getMemberById(id)
        );
    }

    @GetMapping("admin/getAll")
    public ResponseEntity<List<AllMemberResponseDto>> getAllMembers(){
        List<AllMemberResponseDto> response = memberManagementService.getAllMember();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("delete")
    public ResponseEntity<String> deleteMemberById(@Valid @RequestParam String id){
        String  response = memberManagementService.deleteMemberById(id);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
