package com.gym.adminservice.Controllers;

import com.gym.adminservice.Dto.Requests.ApprovalRequestDto;
import com.gym.adminservice.Dto.Requests.TrainerAssignRequestDto;
import com.gym.adminservice.Dto.Responses.AllMemberRequestDtoList;
import com.gym.adminservice.Dto.Responses.ApprovalResponseDto;
import com.gym.adminservice.Dto.Responses.TrainerAssignMentResponseDto;
import com.gym.adminservice.Models.PendingRequest;
import com.gym.adminservice.Services.AuthService.ApprovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
@Slf4j
@RestController
@RequestMapping("${admin.approval.url}")
@RequiredArgsConstructor
@Validated
public class ApprovalController {

    private final ApprovalService approvalService;

    /*
     * Endpoint to add a new approval request which will be processed later via
     * admin panel
     */

    @PostMapping("/insert")
    public ResponseEntity<String> addRequests(@RequestBody ApprovalRequestDto requestDto) {
        String response = approvalService.insertRequest(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /*
     * Endpoint to get all pending approval requests stored in the database so that
     * admin can take action on them (approve/decline)
     */
    @GetMapping("/getList")
    public ResponseEntity<List<PendingRequest>> getAll() {
        List<PendingRequest> response = approvalService.getAll();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /*
     * Endpoint to approve a pending request. This will also send the data to auth
     * service to set them as approved by admin as a role of member/trainer
     */

    @PostMapping("/approved")
    public ResponseEntity<ApprovalResponseDto> approved(@RequestBody ApprovalRequestDto requestDto) {
        ApprovalResponseDto response = approvalService.sendApproval(requestDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /*
     * Endpoint to decline a pending request. This will also send the data to auth
     * service to set them as declined by admin and delete the user
     */

    @PostMapping("/declined")
    public ResponseEntity<ApprovalResponseDto> decline(@RequestBody ApprovalRequestDto requestDto) {
        ApprovalResponseDto response = approvalService.declineApproval(requestDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @PostMapping("/memberRequest")
    public ResponseEntity<String> addMemberRequestForTrainer(@RequestBody TrainerAssignRequestDto requestDto) {
        approvalService.addMeberRequestForTrainer(requestDto);
        String response = "Add request for trainer for the member: " + requestDto.getMemberName();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/memberRequests")
    public ResponseEntity<AllMemberRequestDtoList> getAllMemberRequestDto() {
        AllMemberRequestDtoList response = approvalService.getAllRequests();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/approve-memberRequest")
    public ResponseEntity<TrainerAssignMentResponseDto> assignTrainerToMember(@RequestParam String requestId,
                                                                              @RequestParam
                                                                              @DateTimeFormat(
                                                                                      iso = DateTimeFormat.ISO.DATE)
                                                                              LocalDate eligibleDate)
    {
        log.info("Received requestId={} eligibleDate={}", requestId, eligibleDate);
        TrainerAssignMentResponseDto response = approvalService.assignTrainerToMember(requestId, eligibleDate);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/delete-request")
    public ResponseEntity<String> deleteMemberRequest(@RequestParam String requestId){
        approvalService.deleteRequest(requestId);
        return ResponseEntity.status(HttpStatus.OK).body("Request deleted successfully");
    }

}
