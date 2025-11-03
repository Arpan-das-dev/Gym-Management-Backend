package com.gym.authservice.Controller;

import com.gym.authservice.Dto.Request.AdminCreationRequestDto;
import com.gym.authservice.Dto.Request.SignupRequestDto;
import com.gym.authservice.Dto.Response.SignUpResponseDto;
import com.gym.authservice.Dto.Response.SignupDetailsInfoDto;
import com.gym.authservice.Service.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("${authService.base_url}")
@RequiredArgsConstructor
/*
 * This controller provides administrative functionalities for managing users within the gym management system.
 * It includes endpoints for creating members, trainers, and admins, as well as retrieving user details
 * by ID or email, approving users, and deleting accounts.
 * The controller leverages UserManagementService to handle the underlying business logic.
 * All endpoints are prefixed with "admin" to indicate their administrative nature.
 */
public class ManagementController {

    private final UserManagementService managementService;

    /*
     * Endpoint to create a new member by an admin.
     * Accepts a SignupRequestDto containing member details.
     * Returns a SignUpResponseDto upon successful creation.
     */
    @PostMapping("admin/CreateMember")
    public ResponseEntity<Mono<SignUpResponseDto>> createMember(@Valid @RequestBody SignupRequestDto requestDto){
        Mono<SignUpResponseDto> responseDto = managementService.createMemberByAdmin(requestDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    /*
     * Endpoint to create a new trainer by an admin.
     * Accepts a SignupRequestDto containing trainer details.
     * Returns a SignUpResponseDto upon successful creation.
     */
    @PostMapping("admin/createTrainer")
    public ResponseEntity<Mono<SignUpResponseDto>> createTrainer(@Valid @RequestBody SignupRequestDto requestDto){
        try{
            Mono<SignUpResponseDto> responseDto = managementService.createTrainerByAdmin(requestDto);
            return ResponseEntity.status(HttpStatus.OK).body(responseDto);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
     * Endpoint to create a new admin by an existing admin.
     * Accepts a SignupRequestDto containing admin details.
     * Returns a SignUpResponseDto upon successful creation.
     */
    @PostMapping("admin/createAdmin")
    public ResponseEntity<Mono<SignUpResponseDto>> createAdmin(@Valid @RequestBody AdminCreationRequestDto requestDto){
        try{
            Mono<SignUpResponseDto> responseDto = managementService.createAdminByAdmin(requestDto);
            return ResponseEntity.status(HttpStatus.OK).body(responseDto);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /*
     * Endpoint to retrieve user details by user ID.
     * Accepts a user ID as a path variable and returns a SignupDetailsInfoDto containing user information.
     */
    /**@GetMapping("admin/getUser-id/{id}")
    public ResponseEntity<SignupDetailsInfoDto> getUserById(@PathVariable String id){
        SignupDetailsInfoDto responseDto = managementService.getUserById(id);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }
*/
    /*
     * Endpoint to retrieve user details by email address.
     * Accepts an email address as a path variable and returns a SignupDetailsInfoDto containing user information.
     */
//    @GetMapping ("admin/getUser-email/{email}")
//    public ResponseEntity<SignupDetailsInfoDto> getUserByEmail(@PathVariable String email){
//        SignupDetailsInfoDto responseDto = managementService.getUserByEmail(email);
//        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
//    }

    /*
     * Endpoint to approve or reject a user (e.g., trainer) by an admin.
     * Accepts an email address and an approval boolean as request parameters.
     * Returns a success message indicating the result of the operation.
     */
    @PostMapping("admin/approve")
    public ResponseEntity<Mono<String>> approveUser(@RequestParam String email,@RequestParam boolean approve){
       Mono<String> response = managementService.approve(email,approve);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /*
     * Endpoint to delete a user account by an admin.
     * Accepts a user identifier (email or ID) as a request parameter.
     * Returns a success message upon successful deletion of the account.
     */
    @DeleteMapping("admin/delete")
    public ResponseEntity<String> deleteAccount(@RequestParam String identifier ){
        String response = managementService.deleteByIdentifier(identifier);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
