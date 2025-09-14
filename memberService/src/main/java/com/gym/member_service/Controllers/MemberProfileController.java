package com.gym.member_service.Controllers;

import com.gym.member_service.Services.MemberServices.MemberProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam String id,
                                              @RequestParam("image") MultipartFile image){
        // setting the response for return which is a profile image url
        String urlResponse =  profileService.uploadImage(id,image);
        return ResponseEntity.accepted().body(urlResponse);
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
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteImage(@RequestParam String id){
        // deleting the profile image from
        // AWS s3 bucket and also from database
        profileService.deleteImage(id);
        return ResponseEntity.accepted().body("Image deleted Successfully");
        // if deletes successfully return message with ACCEPTED http status.
    }


}
