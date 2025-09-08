package com.gym.authservice.Controller;

import com.gym.authservice.Dto.Request.SignupRequestDto;
import com.gym.authservice.Dto.Response.SignUpResponseDto;
import com.gym.authservice.Dto.Response.SignupDetailsInfoDto;
import com.gym.authservice.Service.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${authService.base_url}")
@RequiredArgsConstructor
public class ManagementController {

    private final UserManagementService managementService;
    @PostMapping("admin/CreateMember")
    public ResponseEntity<SignUpResponseDto> createMember(@Valid @RequestBody SignupRequestDto requestDto){
        SignUpResponseDto responseDto = managementService.createMemberByAdmin(requestDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @PostMapping("admin/createTrainer")
    public ResponseEntity<SignUpResponseDto> createTrainer(@Valid @RequestBody SignupRequestDto requestDto){
        SignUpResponseDto responseDto = managementService.createTrainerByAdmin(requestDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @PostMapping("admin/createAdmin")
    public ResponseEntity<SignUpResponseDto> createAdmin(@Valid @RequestBody SignupRequestDto requestDto){
        SignUpResponseDto responseDto = managementService.createAdminByAdmin(requestDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }
    @GetMapping("admin/getUser-id/{id}")
    public ResponseEntity<SignupDetailsInfoDto> getUserById(@PathVariable String id){
        SignupDetailsInfoDto responseDto = managementService.getUserById(id);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @GetMapping ("admin/getUser-email/{email}")
    public ResponseEntity<SignupDetailsInfoDto> getUserByEmail(@PathVariable String email){
        SignupDetailsInfoDto responseDto = managementService.getUserByEmail(email);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @PostMapping("admin/approve")
    public ResponseEntity<String> approveUser(@RequestParam String email,@RequestParam boolean approve){
        String response = managementService.approve(email,approve);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("admin/delete")
    public ResponseEntity<String> deleteAccount(@RequestParam String identifier ){
        String response = managementService.deleteByIdentifier(identifier);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
