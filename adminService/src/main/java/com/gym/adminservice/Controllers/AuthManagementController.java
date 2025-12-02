package com.gym.adminservice.Controllers;

import com.gym.adminservice.Dto.Requests.CreateAdminRequestDto;
import com.gym.adminservice.Dto.Requests.CreateMemberRequestDto;
import com.gym.adminservice.Dto.Requests.CreateTrainerRequestDto;
import com.gym.adminservice.Dto.Responses.GenericResponseDto;
import com.gym.adminservice.Dto.Responses.UserCreationResponseDto;
import com.gym.adminservice.Enums.RoleType;
import com.gym.adminservice.Services.AuthService.AuthManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("${app.management.url}")
@RequiredArgsConstructor
@Validated

/*
 * Controller to manage creation and deletion of users (members, trainers, admins) 
 * an admin can create or delete users directly without approval process
 */
public class AuthManagementController {
    private final AuthManagementService managementService;

    /*
     * Endpoint to create a new member/trainer/admin directly without approval
     * process so that admin can add users manually as well and they can login without
     * further approval
     */

    @PostMapping("addMember")
    public ResponseEntity<UserCreationResponseDto> createMember(
            @Valid @RequestBody CreateMemberRequestDto requestDto) {
        UserCreationResponseDto response = managementService.createMember(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /*
     * Endpoint to create a new trainer directly without approval process so that
     * the trainer is ready to go and access all the trainer related features
     * without further approval
     */

    @PostMapping("addTrainer")
    public ResponseEntity<UserCreationResponseDto> createTrainer(@Valid @RequestBody CreateTrainerRequestDto requestDto){
        UserCreationResponseDto response = managementService.createTrainer(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /*
     * Endpoint to create a new admin directly without approval process so that
     * the super admin can add other admins who can then manage the system and any 
     * other admin requests from auth service will be invalid only super admin can add other admins
     */

    @PostMapping("addAdmin")
    public ResponseEntity<UserCreationResponseDto> createAdmin(@Valid @RequestBody CreateAdminRequestDto requestDto){
        UserCreationResponseDto response = managementService.createAdmin(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /*
     * Endpoint to set a custom id to an admin so that the admin can be identified
     * easily and have a unique identifier as well this set id feature is only for admins
     * as members and trainers will have their own ids from auth service
     */

    @PostMapping("setId")
    public ResponseEntity<UserCreationResponseDto> setCustomId(@RequestParam String id,@RequestParam String role,
                                              @RequestParam String email){
        UserCreationResponseDto response = managementService.setCustomIdToAdmin(id,role,email);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /*
     * Endpoint to delete a user (member/trainer/admin) from the system so that
     * the user can no longer access the system and all their data is removed
     * this is a hard delete and cannot be undone so use with caution
     */

    @DeleteMapping("delete")
    public ResponseEntity<GenericResponseDto> deleteUser(@RequestParam String identifier, @RequestParam RoleType role){
        try {
            GenericResponseDto response = managementService.deleteUser(identifier,role);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


}
