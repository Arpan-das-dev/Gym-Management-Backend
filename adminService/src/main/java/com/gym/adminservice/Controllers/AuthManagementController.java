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
public class AuthManagementController {
    private final AuthManagementService managementService;

    @PostMapping("addMember")
    public ResponseEntity<String> createMember(@Valid @RequestBody CreateMemberRequestDto requestDto) {
        String response = managementService.createMember(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("addTrainer")
    public ResponseEntity<String> createTrainer(@Valid @RequestBody CreateTrainerRequestDto requestDto){
        String response = managementService.createTrainer(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("addAdmin")
    public ResponseEntity<String> createAdmin(@Valid @RequestBody CreateAdminRequestDto requestDto){
        String response = managementService.createAdmin(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("setId")
    public ResponseEntity<String> setCustomId(@RequestParam String id,@RequestParam String role,
                                              @RequestParam String email){
        String response = managementService.setCustomIdToAdmin(id,role,email);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @DeleteMapping("delete")
    public ResponseEntity<?> deleteUser(@RequestParam String identifier,@RequestParam RoleType role){
        String response = managementService.deleteUser(identifier,role);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
