package com.gym.adminservice.Controllers;

import com.gym.adminservice.Dto.Requests.ApprovalRequestDto;
import com.gym.adminservice.Dto.Responses.ApprovalResponseDto;
import com.gym.adminservice.Models.PendingRequest;
import com.gym.adminservice.Services.AuthService.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    public ResponseEntity addRequests( ApprovalRequestDto requestDto) {
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
    public ResponseEntity<ApprovalResponseDto> approved(ApprovalRequestDto requestDto) {
        ApprovalResponseDto response = approvalService.sendApproval(requestDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /*
     * Endpoint to decline a pending request. This will also send the data to auth
     * service to set them as declined by admin and delete the user 
     */

    @PostMapping("/declined")
    public ResponseEntity<ApprovalResponseDto> decline(ApprovalRequestDto requestDto) {
        ApprovalResponseDto response = approvalService.declineApproval(requestDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
