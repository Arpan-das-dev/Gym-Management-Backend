package com.gym.member_service.Controllers;

import com.gym.member_service.Dto.NotificationDto.GenericResponse;
import com.gym.member_service.Services.MemberServices.MemberProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("${member-service.Base_Url.profile}")
@RequiredArgsConstructor
@Validated
/*
 * This controller is responsible for
 * member to upload or delete image
 * The endpoint is defined in application.properties
 * it uses Aws s3 service which is used in
 * MemberProfileService
 */
public class MemberProfileController {
    // injecting the service class by constructor
    private final MemberProfileService profileService;

    /*
     * This method allows members to upload
     * image(profile image) and save as
     * the profile image url in the database,
     * at a specific endpoint ("/upload")
     * it took member id and file as
     * RequestParam to do so using MemberProfile service
     */
    @PostMapping("/member/upload")
    public ResponseEntity<GenericResponse> uploadImage(@RequestParam String memberId,
                                              @RequestParam("image") MultipartFile image){
        // setting the response for return which is a profile image url
        String urlResponse =  profileService.uploadImage(memberId,image);
        return ResponseEntity.accepted().body(new GenericResponse(urlResponse));
        // after successfully upload returns response with ACCEPTED http status.
    }
    /*
     * This method allows members to delete
     * image(profile image) and save as
     * the profile image url(as null) in the database,
     * at a specific endpoint ("/delete")
     * it took member id  as RequestParam
     * to do so using MemberProfile service
     */
    @DeleteMapping("/member/delete")
    public ResponseEntity<GenericResponse> deleteImage(@RequestParam String memberId){
        // deleting the profile image from
        // AWS s3 bucket and also from database
        profileService.deleteImage(memberId);
        return ResponseEntity.accepted().body(new GenericResponse("Image deleted Successfully"));
        // if deletes successfully return message with ACCEPTED http status.
    }


    @GetMapping("/all/getProfileImage")
    public ResponseEntity <GenericResponse> getProfileImageUrl(@RequestParam String memberId) {
        log.info("Request received to get profile image at controller [**/all/getProfileImage]");
        GenericResponse response = profileService.getProfileImageUrlByMemberId(memberId);
        log.info("Sending profile url {} ...........",response.getMessage().substring(0,7));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
