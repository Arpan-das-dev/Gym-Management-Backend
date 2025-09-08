package com.gym.adminservice.Controllers;

import com.gym.adminservice.Dto.Requests.CreateAdminRequestDto;
import com.gym.adminservice.Dto.Requests.CreateMemberRequestDto;
import com.gym.adminservice.Dto.Requests.CreateTrainerRequestDto;
import com.gym.adminservice.Enums.RoleType;
import com.gym.adminservice.Services.AuthService.AuthManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<String> createMember(@Valid @RequestBody CreateMemberRequestDto requestDto) {
        String response = managementService.createMember(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /*
     * Endpoint to create a new trainer directly without approval process so that
     * the trainer is ready to go and access all the trainer related features
     * without further approval
     */

    @PostMapping("addTrainer")
    public ResponseEntity<String> createTrainer(@Valid @RequestBody CreateTrainerRequestDto requestDto){
        String response = managementService.createTrainer(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /*
     * Endpoint to create a new admin directly without approval process so that
     * the super admin can add other admins who can then manage the system and any 
     * other admin requests from auth service will be invalid only super admin can add other admins
     */

    @PostMapping("addAdmin")
    public ResponseEntity<String> createAdmin(@Valid @RequestBody CreateAdminRequestDto requestDto){
        String response = managementService.createAdmin(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /*
     * Endpoint to set a custom id to an admin so that the admin can be identified
     * easily and have a unique identifier as well this set id feature is only for admins
     * as members and trainers will have their own ids from auth service
     */

    @PostMapping("setId")
    public ResponseEntity<String> setCustomId(@RequestParam String id,@RequestParam String role,
                                              @RequestParam String email){
        String response = managementService.setCustomIdToAdmin(id,role,email);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /*
     * Endpoint to delete a user (member/trainer/admin) from the system so that
     * the user can no longer access the system and all their data is removed
     * this is a hard delete and cannot be undone so use with caution
     */

    @DeleteMapping("delete")
    public ResponseEntity<?> deleteUser(@RequestParam String identifier,@RequestParam RoleType role){
        String response = managementService.deleteUser(identifier,role);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /*
     * Endpoint to freeze a user (member/trainer) account temporarily so that
     * the user can access the system but cannot user their dashboard and other features
     * but still can login and view basic info and contact admin for more info and also do shoping
     */

     /*
      * Endpoint to unfreeze a user (member/trainer) account so that
      * the user can access their dashboard and other features again.
      */
}
