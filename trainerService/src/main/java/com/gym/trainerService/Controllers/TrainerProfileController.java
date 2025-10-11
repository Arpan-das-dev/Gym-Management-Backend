package com.gym.trainerService.Controllers;

import com.gym.trainerService.Services.TrainerServices.TrainerProfileService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("${trainer-service.Base_Url.Profile}")
@RequiredArgsConstructor
public class TrainerProfileController {

    private final TrainerProfileService profileService;

    @PostMapping("/trainer/upload")
    public ResponseEntity<String> uploadTrainerImage(@RequestParam @NotBlank String trainerId,
                                                     @RequestParam("image") MultipartFile image) {
        String response = profileService.uploadImage(trainerId,image);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/all/image")
    public ResponseEntity<String> getProfileImage(@RequestParam @NotBlank String trainerId) {
        String response = profileService.getProfileImageUrl(trainerId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/trainer/delete")
    public ResponseEntity<String> deleteTrainerImage(@RequestParam @NotBlank String trainerId) {
        String response = profileService.deleteProfileImageUrl(trainerId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
