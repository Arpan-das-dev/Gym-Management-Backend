package com.gym.member_service.Controllers;

import com.gym.member_service.Dto.MemberProfieDtos.Responses.AllMemberProfileImageResponseWrapperDto;
import com.gym.member_service.Dto.NotificationDto.GenericResponse;
import com.gym.member_service.Services.MemberServices.MemberProfileService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


/**

 Controller responsible for managing member profile images including upload, deletion, and retrieval.

 <p>This controller handles multipart file uploads and interacts with AWS S3 via the MemberProfileService.
 It provides REST endpoints to:

 <ul>
 <li>Upload member profile images and store the image URL in the database</li>
 <li>Delete profile images from AWS S3 and clear the database reference</li>
 <li>Retrieve profile image URLs for display</li>
 </ul>
 <p>Endpoints are protected and validated, with logging added to trace operations and important input values.
 Appropriate HTTP response statuses are used to denote operation results.

 @author Arpan Das

 @version 1.0

 @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("${member-service.Base_Url.profile}")
@RequiredArgsConstructor
@Validated

public class MemberProfileController {

    private final MemberProfileService profileService;

    /**

     Uploads a profile image for a member.

     <p>Accepts memberId and image multipart file as request parameters.
     Validates inputs and stores the image URL using MemberProfileService.

     Returns a GenericResponse containing the upload result with HTTP status 202 ACCEPTED.

     @param memberId member's unique identifier

     @param image multipart file containing the profile image to upload

     @return ResponseEntity containing the upload outcome wrapped in GenericResponse
     */
    @PostMapping("/member/upload")
    public ResponseEntity<GenericResponse> uploadImage(@RequestParam String memberId,
                                                       @RequestParam("image") MultipartFile image) {
        log.info("Received profile image upload request for memberId: {}", memberId);
        GenericResponse urlResponse = profileService.uploadImage(memberId, image);
        log.info("Profile image upload processed for memberId: {}", memberId);
        return ResponseEntity.accepted().body(urlResponse);
    }

    /**

     Deletes a profile image of a member.

     <p>Removes image from AWS S3 and clears the profile image URL in the database.
     Returns confirmation message with HTTP status 202 ACCEPTED.

     @param memberId member's unique identifier whose image is to be deleted

     @return ResponseEntity with deletion confirmation wrapped in GenericResponse
     */
    @DeleteMapping("/member/delete")
    public ResponseEntity<GenericResponse> deleteImage(@RequestParam String memberId) {
        log.info("Received profile image deletion request for memberId: {}", memberId);
        profileService.deleteImage(memberId);
        log.info("Profile image deleted successfully for memberId: {}", memberId);
        return ResponseEntity.accepted().body(new GenericResponse("Image deleted Successfully"));
    }

    @GetMapping("/admin/getProfileUrls")
    public ResponseEntity<AllMemberProfileImageResponseWrapperDto> getChunksOfProfileImage(
            @RequestBody List<@NotBlank String> memberIds
            ){
        log.info("©️©️ Request received to get profile image for {} members",memberIds.size());
        AllMemberProfileImageResponseWrapperDto response = profileService.getChunksOfMemberProfileImage(memberIds);
        log.info("Serving {} no of member Profile Images ",response.getMemberProfileUrlList().size());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    /**

     Retrieves the profile image URL for a specific member.

     <p>Validates that memberId is not blank, logs the request,
     and returns the URL wrapped in GenericResponse with HTTP 200 OK.

     @param memberId non-blank member identifier for which profile image URL is requested

     @return ResponseEntity containing the profile image URL wrapped in GenericResponse
     */
    @GetMapping("/all/getProfileImage")
    public ResponseEntity<GenericResponse> getProfileImageUrl(
            @RequestParam @NotBlank(message = "Cannot retrieve profile image without member ID") String memberId) {
        log.info("Request received to get profile image for memberId: {}", memberId);
        GenericResponse response = profileService.getProfileImageUrlByMemberId(memberId);
        String urlPreview = response.getMessage().length() > 7 ? response.getMessage().substring(0, 7) : response.getMessage();
        log.info("Sending profile URL preview: {}...", urlPreview);
        return ResponseEntity.ok(response);
    }
}
