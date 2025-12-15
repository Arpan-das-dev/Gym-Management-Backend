package com.gym.adminservice.Services.AuthService;

import com.gym.adminservice.Dto.Requests.ApprovalRequestDto;
import com.gym.adminservice.Dto.Requests.TrainerAssignRequestDto;
import com.gym.adminservice.Dto.Responses.*;
import com.gym.adminservice.Dto.Wrappers.AllPendingRequestResponseWrapperDto;
import com.gym.adminservice.Enums.RoleType;
import com.gym.adminservice.Exceptions.Custom.RequestNotFoundException;
import com.gym.adminservice.Models.MemberRequest;
import com.gym.adminservice.Models.PendingRequest;
import com.gym.adminservice.Repository.MemberRequestRepository;
import com.gym.adminservice.Repository.PendingRequestRepository;
import com.gym.adminservice.Services.WebClientServices.WebClientAuthService;
import com.gym.adminservice.Services.WebClientServices.WebClientNotificationService;
import com.sun.jdi.request.DuplicateRequestException;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import java.util.Optional;
import java.util.UUID;

@Slf4j
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

    @CacheEvict(value = "pendingRequest", key = "'allRequest'")
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

    @Cacheable(value = "pendingRequest", key = "'allRequest'")
    public AllPendingRequestResponseWrapperDto getAll() {
        List<PendingRequest> requestList = pendingRequestRepository.findAll();
        List<PendingRequestResponseDto> responseDtoList = requestList.stream()
                .map(req->PendingRequestResponseDto.builder()
                        .requestId(req.getId())
                        .email(req.getEmail()).phone(req.getPhone())
                        .name(req.getName())
                        .role(req.getRole())
                        .joinDate(req.getJoinDate())
                        .build()).toList();
        return AllPendingRequestResponseWrapperDto.builder()
                .responseDtoList(responseDtoList)
                .build();
    }

    /*
     * this service will send the approval status to the auth service and also
     * send email notification to the user regarding the approval status
     * and also delete the request from the pending request table
     */

    @Transactional
    @CacheEvict(value = "pendingRequest", key = "'allRequest'")
    public ApprovalResponseDto sendApproval(ApprovalRequestDto requestDto) {

        ApprovalResponseDto responseDto = ApprovalResponseDto.builder()
                .email(requestDto.getEmail())
                .approval(true)
                .build();
        try {
            // send the approval status to the auth service
            webAuthClientService.sendApproval(responseDto.getEmail(), responseDto.isApproval());
            // send email notification to the user regarding the approval of the request
            webClientNotificationService.sendApproveMail(new ApproveEmailNotificationDto(
                    requestDto.getEmail(), requestDto.getName(), requestDto.getRole()));
            pendingRequestRepository.deleteByEmail(requestDto.getEmail());
            return responseDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * this service will decline the approval request and also delete the user from
     * the auth service
     * and also delete the trainer or member details from their respective services
     */

    @Transactional
    @CacheEvict(value = "pendingRequest", key = "'allRequest'")
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

    @Transactional
    @CacheEvict(value = "memberRequests", key = "'pending'")
    public void addMemberRequestForTrainer(TrainerAssignRequestDto requestDto) {
        Optional<MemberRequest> inComingReq = memberRequestRepository.findBYMemberId(requestDto.getMemberId());
        if(inComingReq.isPresent()) {
            throw  new DuplicateRequestException("Your Request is already Present Please Wait for Admin or Contact Admin");
        }

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


    @Cacheable(value = "memberRequests", key = "'pending'")
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

    @CacheEvict(value = "memberRequests", key = "'pending'")
    public Mono<String> assignTrainerToMember(String requestId, LocalDate eligiblyDate) {

        log.info("➡ Starting assignTrainerToMember(requestId={}, eligibleDate={})", requestId, eligiblyDate);

        MemberRequest request = memberRequestRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException("No request found with this id :" + requestId));

        log.info("✔ Retrieved MemberRequest from DB for requestId={} and memberId={}", requestId, request.getMemberId());

        TrainerAssignmentResponseDto trainerAssignmentResponseDto = TrainerAssignmentResponseDto.builder()
                .memberId(request.getMemberId())
                .trainerId(request.getTrainerId())
                .trainerName(request.getTrainerName())
                .trainerProfileImageUrl(request.getTrainerProfileImageUrl())
                .eligibilityEnd(eligiblyDate)
                .build();

        MemberAssignmentToTrainerResponseDto memberResponseDto = MemberAssignmentToTrainerResponseDto.builder()
                .memberId(request.getMemberId())
                .trainerId(request.getTrainerId())
                .memberName(request.getMemberName())
                .memberProfileImageUrl(request.getMemberProfileImageUrl())
                .eligibilityEnd(eligiblyDate)
                .build();

        log.info("📤 Sending DTO to Trainer-Service AssignMember endpoint...");
        log.debug("TrainerAssignmentResponseDto: {}", trainerAssignmentResponseDto);

        return webAuthClientService.sendDtoForAssignMemberToTrainer(memberResponseDto)
                .doOnSuccess(msg -> log.info("✔ Member sent to Trainer-Service: {}", msg))
                .doOnError(err -> log.error("❌ Failed sending member to Trainer-Service: {}", err.getMessage()))

                .then(webAuthClientService.sendDtoForAssignTrainerToMember(trainerAssignmentResponseDto)
                        .doOnSuccess(msg -> log.info("✔ Trainer sent to Member-Service: {}", msg))
                        .doOnError(err -> log.error("❌ Failed sending trainer to Member-Service: {}", err.getMessage()))
                )
                .then(deleteRequest(requestId))
                .thenReturn("Trainer successfully assigned to member.")
                .onErrorResume(ex -> {
                    log.error("❌ Error during assignment pipeline for requestId={} : {}", requestId, ex.getMessage());

                    log.warn("🔄 Executing rollback in Trainer-Service...");
                    return webAuthClientService.RollBackMemberFromTrainerService(
                                    trainerAssignmentResponseDto.getTrainerId(),
                                    trainerAssignmentResponseDto.getMemberId()
                            )
                            .doOnSuccess(msg -> log.warn("⚠ Rollback completed in Trainer-Service: {}", msg))
                            .doOnError(err -> log.error("❌ Rollback failed: {}", err.getMessage()))
                            .then(Mono.error(ex));
                });
    }


    @CacheEvict(value = "memberRequests", key = "'pending'")
    public Mono<Void> deleteRequest(String requestId) {

        log.info("➡ deleteRequest triggered for requestId={}", requestId);

        return Mono.fromRunnable(() -> {
                    log.info("🗑 JPA deleting requestId={} ...", requestId);
                    memberRequestRepository.deleteById(requestId);
                    log.info("✔ JPA deleteById completed for {}", requestId);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSubscribe(sub -> log.debug("🔧 deleteRequest subscribed for requestId={}", requestId))
                .doOnSuccess(v -> log.info("✔ deleteRequest finished successfully for requestId={}", requestId))
                .doOnError(err -> log.error("❌ deleteRequest error for requestId={} : {}", requestId, err.getMessage()))
                .then();
    }


    private String requestIdGen(String memberId, String trainerId, LocalDate requestDate) {
        String datePart = requestDate.format(DateTimeFormatter.BASIC_ISO_DATE); // yyyyMMdd
        String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 8); // 8 chars
        return memberId + "-" + trainerId + "-" + datePart + randomPart;
    }


}
