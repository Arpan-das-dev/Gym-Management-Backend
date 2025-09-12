package com.gym.member_service.Controllers;

import com.gym.member_service.Services.MemberProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("${member-service.Base_Url.profile}")
@RequiredArgsConstructor
@Validated
public class MemberProfileController {

    private final MemberProfileService profileService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam String id,
                                              @RequestParam("image") MultipartFile image){
        String urlResponse =  profileService.uploadImage(id,image);
        return ResponseEntity.accepted().body(urlResponse);
    }
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteImage(@RequestParam String id){
        profileService.deleteImage(id);
        return ResponseEntity.accepted().body("Image deleted Successfully");
    }


}
