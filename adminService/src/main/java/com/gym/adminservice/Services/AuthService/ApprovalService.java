package com.gym.adminservice.Services.AuthService;

import com.gym.adminservice.Dto.Requests.ApprovalRequestDto;
import com.gym.adminservice.Dto.Requests.TrainerAssignRequestDto;
import com.gym.adminservice.Dto.Responses.AllMemberRequestDtoList;
import com.gym.adminservice.Dto.Responses.ApprovalResponseDto;
import com.gym.adminservice.Dto.Responses.ApproveEmailNotificationDto;
import com.gym.adminservice.Dto.Responses.MemberAssignmentToTrainerResponseDto;
import com.gym.adminservice.Dto.Responses.MemberRequestResponse;
import com.gym.adminservice.Dto.Responses.TrainerAssignMentResponseDto;
import com.gym.adminservice.Enums.RoleType;
import com.gym.adminservice.Exceptions.RequestNotFoundException;
import com.gym.adminservice.Models.MemberRequest;
import com.gym.adminservice.Models.PendingRequest;
import com.gym.adminservice.Repository.MemberRequestRepository;
import com.gym.adminservice.Repository.PendingRequestRepository;
import com.gym.adminservice.Services.WebClientServices.WebClientAuthService;
import com.gym.adminservice.Services.WebClientServices.WebClientNotificationService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import java.util.UUID;

@Service
@RequiredArgsConstructor

/*
 * this service class will handle also the approval and decline of the request
 * which
 * is sent from the auth service when a user signs up as a trainer or member,
 * and also it will send the approval status to the auth service, also send
 * email notification and
 * role regarding services.
 */

public class ApprovalService {
    private final PendingRequestRepository pendingRequestRepository;
    private final WebClientAuthService webAuthClientService;
    private final WebClientNotificationService webClientNotificationService;
    private final MemberRequestRepository memberRequestRepository;

    /*
     * this service will insert all those request in the pending request table
     * which are sent from the auth service when a user signs up as a trainer or
     * member
     * and also if the request is already present it will throw an exception
     */

    public String insertRequest(ApprovalRequestDto requestDto) {
        if (pendingRequestRepository.existsByEmail(requestDto.getEmail())) {
            throw new RuntimeException("request already present please wait until verified");
        }
        PendingRequest pendingRequest = PendingRequest.builder()
                .id(String.valueOf(UUID.randomUUID()))
                .email(requestDto.getEmail())
                .name(requestDto.getName())
                .phone(requestDto.getPhone())
                .role(requestDto.getRole())
                .joinDate(requestDto.getJoinDate())
                .build();
        pendingRequestRepository.save(pendingRequest);
        return "successfully sent request";
    }

    /*
     * this service will return all the pending requests from the pending request
     * table
     * which we will show in the frontend for the admin to approve or decline
     */

    public List<PendingRequest> getAll() {
        return pendingRequestRepository.findAll();
    }

    /*
     * this service will send the approval status to the auth service and also
     * send email notification to the user regarding the approval status
     * and also delete the request from the pending request table
     */

    @Transactional
    public ApprovalResponseDto sendApproval(ApprovalRequestDto requestDto) {
        pendingRequestRepository.deleteByEmail(requestDto.getEmail());
        ApprovalResponseDto responseDto = ApprovalResponseDto.builder()
                .email(requestDto.getEmail())
                .approval(true)
                .build();
        // send the approval status to the auth service
        webAuthClientService.sendApproval(responseDto.getEmail(), responseDto.isApproval());
        // send email notification to the user regarding the approval of the request
        webClientNotificationService.sendApproveMail(new ApproveEmailNotificationDto(
                requestDto.getEmail(), requestDto.getName(), requestDto.getRole()));
        return responseDto;
    }

    /*
     * this service will decline the approval request and also delete the user from
     * the auth service
     * and also delete the trainer or member details from their respective services
     */

    @Transactional
    public ApprovalResponseDto declineApproval(ApprovalRequestDto requestDto) {
        PendingRequest request = pendingRequestRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(); // user not found exception
        pendingRequestRepository.delete(request);
        /*
         * if the user role is trainer then delete the trainer details from the trainer
         * service's database
         * if the user role is member then delete the member details from the member
         * service's database,
         * but we will implement this later when we will implement the trainer and
         * member services
         */
        if (request.getRole().equals(RoleType.TRAINER_PENDING)) {
        // delete the trainer details from trainer service's database
        webAuthClientService.deleteTrainer(requestDto.getEmail());
        } else if (request.getRole().equals(RoleType.MEMBER)) {
        // delete the member details from member service's database
        webAuthClientService.deleteMember(requestDto.getEmail());
        }
        /*
         * now delete the user from the auth service's database
         */
        webAuthClientService.deleteUser(requestDto.getEmail());
        // send email notification to the user regarding the decline of the request
        webClientNotificationService.sendDeclinedMail(
                new ApproveEmailNotificationDto(requestDto.getEmail(), requestDto.getName(), requestDto.getRole()));
        return ApprovalResponseDto.builder()
                .email(requestDto.getEmail())
                .approval(false)
                .build();
    }

    public void addMemberRequestForTrainer(TrainerAssignRequestDto requestDto) {
        String requestId = requestIdGen(requestDto.getMemberId(), requestDto.getTrainerId(),
                requestDto.getRequestDate());
        MemberRequest request = MemberRequest.builder()
                .requestId(requestId)
                .memberId(requestDto.getMemberId())
                .memberProfileImageUrl(requestDto.getMemberProfileImageUrl())
                .memberName(requestDto.getMemberName())
                .trainerId(requestDto.getTrainerId())
                .trainerProfileImageUrl(requestDto.getTrainerProfileImageUrl())
                .trainerName(requestDto.getTrainerName())
                .requestDate(requestDto.getRequestDate())
                .memberPlanExpirationDate(requestDto.getMemberPlanExpirationDate())
                .memberPlanName(requestDto.getMemberPlanName())
                .build();

        memberRequestRepository.save(request);
    }

    public AllMemberRequestDtoList getAllRequests() {
        List<MemberRequest> requests = memberRequestRepository.findAll();
        List<MemberRequestResponse> result = requests.stream().map(res -> MemberRequestResponse.builder()
                .requestId(res.getRequestId())
                .memberId(res.getMemberId())
                .memberPlanName(res.getMemberPlanName())
                .memberProfileImageUrl(res.getMemberProfileImageUrl())
                .memberName(res.getMemberName())
                .trainerId(res.getTrainerId())
                .trainerName(res.getTrainerName())
                .trainerProfileImageUrl(res.getTrainerProfileImageUrl())
                .memberPlanExpirationDate(res.getMemberPlanExpirationDate())
                .requestDate(res.getRequestDate())
                .build()).toList();
        return AllMemberRequestDtoList.builder()
                .requestDtoList(result)
                .build();
    }

    public TrainerAssignMentResponseDto assignTrainerToMember(String requestId, LocalDate elidgiblitlyDate) {
        MemberRequest request = memberRequestRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException("No request found with this id :" + requestId));
        TrainerAssignMentResponseDto responseDto = TrainerAssignMentResponseDto.builder()
                .memberId(request.getMemberId())
                .trainerId(request.getTrainerId())
                .trainerName(request.getTrainerName())
                .trainerProfileImageUrl(request.getTrainerProfileImageUrl())
                .eligibilityEnd(elidgiblitlyDate)
                .build();
        MemberAssignmentToTrainerResponseDto memberResponseDto = MemberAssignmentToTrainerResponseDto.builder()
                .memberId(request.getMemberId())
                .memberName(request.getMemberName())
                .memberProfileImageUrl(request.getMemberProfileImageUrl())
                .elidgibilityDate(elidgiblitlyDate)
                .build();

        webAuthClientService.sendDtoForAssignTrainerToMember(responseDto);
        webAuthClientService.sendDtoForAssignMemberToTrainer(memberResponseDto);
        return responseDto;
    }

    public void deleteRequest(String requestId){
       memberRequestRepository.deleteById(requestId);
    }


    /**
 * Generates a unique requestId for member-trainer assignment requests.
 * Format: {memberId}-{trainerId}-{yyyyMMdd}-{8-char-random}
 * Example: M123-T456-20250916-ab12cd34
 */
private String requestIdGen(String memberId, String trainerId, LocalDate requestDate) {
    String datePart = requestDate.format(DateTimeFormatter.BASIC_ISO_DATE); // yyyyMMdd
    String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 8); // 8 chars
    return memberId + "-" + trainerId + "-" + datePart + randomPart;
}


}
