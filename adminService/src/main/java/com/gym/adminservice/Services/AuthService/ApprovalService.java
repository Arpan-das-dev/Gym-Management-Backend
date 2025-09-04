package com.gym.adminservice.Services;

import com.gym.adminservice.Dto.Requests.ApprovalRequestDto;
import com.gym.adminservice.Dto.Responses.ApprovalResponseDto;
import com.gym.adminservice.Dto.Responses.ApproveEmailNotificationDto;
import com.gym.adminservice.Enums.RoleType;
import com.gym.adminservice.Models.PendingRequest;
import com.gym.adminservice.Repository.PendingRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApprovalService {
    private final PendingRequestRepository pendingRequestRepository;
    private final WebClientAuthService webClientService;
    private final WebClientNotificationService webClientNotificationService;

    public String insertRequest(ApprovalRequestDto requestDto){
        if(pendingRequestRepository.existsByEmail(requestDto.getEmail())) {
            throw new RuntimeException("request already present please wait until verified");
        }
        PendingRequest pendingRequest = PendingRequest.builder()
                .email(requestDto.getEmail())
                .name(requestDto.getName())
                .phone(requestDto.getPhone())
                .role(requestDto.getRole())
                .joinDate(requestDto.getJoinDate())
                .build();
        pendingRequestRepository.save(pendingRequest);
        return "successfully sent request";
    }

    public List<PendingRequest> getAll(){
        return pendingRequestRepository.findAll();
    }

    @Transactional
    public ApprovalResponseDto sendApproval(ApprovalRequestDto requestDto) {
        pendingRequestRepository.deleteByEmail(requestDto.getEmail());
        ApprovalResponseDto responseDto = ApprovalResponseDto.builder()
                .email(requestDto.getEmail())
                .approval(true)
                .build();
        webClientService.sendApproval(responseDto.getEmail(),responseDto.isApproval());
        webClientNotificationService.sendApproveMail(new ApproveEmailNotificationDto(
                requestDto.getEmail(),requestDto.getName(),requestDto.getRole()
        ));
        return responseDto;
    }

    @Transactional
    public ApprovalResponseDto declineApproval(ApprovalRequestDto requestDto){
        PendingRequest request = pendingRequestRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(); // user not found exception
        pendingRequestRepository.delete(request);
        if(request.getRole().equals(RoleType.TRAINER_PENDING )){
            webClientService.deleteTrainer(request.getEmail());
        } else if (request.getRole().equals(RoleType.MEMBER)) {
            webClientService.deleteMember(request.getEmail());
        }
        webClientService.deleteUser(requestDto.getEmail());
        webClientNotificationService.sendDeclinedMail(new ApproveEmailNotificationDto
                (requestDto.getEmail(),requestDto.getName(),requestDto.getRole())
        );
        return ApprovalResponseDto.builder()
                .email(requestDto.getEmail())
                .approval(false)
                .build();
    }



}
