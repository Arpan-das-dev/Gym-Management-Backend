package com.gym.trainerService.Controllers;

import com.gym.trainerService.Dto.MemberDtos.Requests.AssignMemberRequestDto;
import com.gym.trainerService.Dto.MemberDtos.Responses.GenericResponse;
import com.gym.trainerService.Dto.MemberDtos.Responses.MemberResponseDto;
import com.gym.trainerService.Dto.MemberDtos.Wrappers.AllMemberResponseWrapperDto;
import com.gym.trainerService.Services.MemberServices.MemberManagementService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * Controller class for managing member-trainer relationships in the Trainer Service.
 * Provides REST endpoints for assigning, fetching, and deleting members associated with a trainer.
 * <p>
 * All routes are prefixed with the base URL defined in `trainer-service.Base_Url.memberManagement`.
 * </p>
 *
 * <p><b>Endpoints:</b></p>
 * <ul>
 *   <li>POST /all/addMember – Assigns a member to a trainer.</li>
 *   <li>GET /trainer/getMemberList – Retrieves all members assigned to a specific trainer.</li>
 *   <li>DELETE /trainer/deleteMember – Removes a member from a trainer.</li>
 * </ul>
 *
 * @author Arpan
 * @since 1.0
 */
@RestController
@RequestMapping("${trainer-service.Base_Url.memberManagement}")
@Slf4j
@RequiredArgsConstructor
@Validated
public class MemberManagementController {

    private final MemberManagementService memberManagementService;
    /**
     * Assigns a member to a specific trainer.
     *
     * @param trainerId  unique identifier of the trainer
     * @param requestDto request body containing member assignment details
     * @return {@link GenericResponse} with response message
     */
    @PostMapping("/admin/addMember")
    public ResponseEntity<GenericResponse> assignMembersToTrainer(@RequestParam String trainerId,
                                                                  @Valid @RequestBody AssignMemberRequestDto requestDto)
    {
        log.info("Request received to assign member for trainer with id: {}",trainerId);
        MemberResponseDto response = memberManagementService.addMember(trainerId,requestDto);
        log.info("member {} saved for trainer {}",response.getMemberName(),response.getTrainerId());
        String res = "Assigned Member In Trainer Service Successfully";
        return ResponseEntity.status(HttpStatus.CREATED).body(new GenericResponse(res));
    }

    /**
     * Retrieves all members currently assigned to a given trainer.
     *
     * @param trainerId unique identifier of the trainer
     * @return {@link AllMemberResponseWrapperDto} containing the list of members
     */
    @GetMapping("/trainer/getMemberList")
    public ResponseEntity<AllMemberResponseWrapperDto> getAllMembers (
            @RequestParam
            @NotBlank(message = "Can not Proceed Any request Without Having Valid Credentials") String trainerId) {
        log.info("Request received to get all members list for trainer id: {}",trainerId);
        AllMemberResponseWrapperDto response = memberManagementService.getAllMembersByTrainerId(trainerId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Deletes a member from a trainer’s list by their IDs.
     *
     * @param trainerId ID of the trainer
     * @param memberId  ID of the member to delete
     * @return {@link GenericResponse} with confirmation message string
     */
    @DeleteMapping("/trainer/deleteMember")
    public ResponseEntity<Mono<GenericResponse>> deleteMember(@RequestParam String trainerId,
                                                              @RequestParam String memberId,
                                                              @RequestParam boolean value) {
        log.info("Request received to delete member {} for trainer {}",memberId,trainerId);
        Mono<GenericResponse> response = memberManagementService.deleteMemberByIds(trainerId,memberId,value);
        return  ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }



}
