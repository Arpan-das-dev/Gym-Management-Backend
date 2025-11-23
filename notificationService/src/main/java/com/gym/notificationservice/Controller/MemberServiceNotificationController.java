package com.gym.notificationservice.Controller;

import com.gym.notificationservice.Dto.MailNotificationDtos.MailNotificationRequestDto;
import com.gym.notificationservice.Dto.MailNotificationDtos.NotificationFrozenRequestDto;
import com.gym.notificationservice.Dto.MailNotificationDtos.PlanActivationNotificationRequestDto;
import com.gym.notificationservice.Services.MemberNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("${memberService.url}")
@RequiredArgsConstructor

/*
 * This controller opens up a url
 * for the member Service (microservice) to send notification
 * for -
 *       1) When plan left for 3 days
 *       2) When plan has expired
 *       3) When account is frozen due not active plans for last 10 days or admin frozen the account
 * url for this controller is defined in application.properties
 */

public class MemberServiceNotificationController {
    // injecting the dependency using constructor(@RequiredArgConstructor)
    private final MemberNotificationService memberNotificationService;

    /*
     * This method maps a url to send an alert message
     * it sends mail and sms to the member when the plan duration left for 3 days
     * it takes a valid request dto as parameter and use MemberNotification service
     * to send notification.
     */
    @PostMapping("/alert")
    public ResponseEntity<String> sendAlertNotification(@RequestBody @Valid MailNotificationRequestDto requestDto) {
        // using memberService to send sms/mail
        memberNotificationService.sendAlertMail(requestDto);
        return ResponseEntity.status(HttpStatus.OK).body("Email send Successfully");
        // if successfully sends mail then returns OK as http status
    }

    /*
     * This method maps a url to send a plan expiration message
     * it sends mail and sms to the member when the plan duration is over
     * it takes a valid request dto as parameter and use MemberNotification service
     * to send notification.
     */
    @PostMapping("/expired")
    public ResponseEntity<String> sendExpirationNotification(@RequestBody @Valid MailNotificationRequestDto requestDto){
        // using memberService to send sms/mail
        memberNotificationService.sendPlanExpirationMail(requestDto);
        return ResponseEntity.status(HttpStatus.OK).body("Email send Successfully");
        // if successfully sends mail then returns OK as http status
    }

    /*
     * This method maps a url to send an account frozen message
     * it sends mail and sms to the member when the plan duration is over for more than 10 days
     * it takes a valid request dto as parameter and use MemberNotification service
     * to send notification.
     */
    @PostMapping("/frozen")
    public ResponseEntity<String> sendFrozenAccountNotification(@RequestBody @Valid MailNotificationRequestDto
                                                                requestDto){
        // using memberService to send sms/mail
        memberNotificationService.sendFrozenMail(requestDto);
        return ResponseEntity.status(HttpStatus.OK).body("Email send Successfully");
        // if successfully sends mail then returns OK as http status
    }

    /*
     * This method maps a url to send a plan renewal message
     * it sends mail and sms to the member when a new plan is added or renewed
     * it takes a valid request dto as parameter and use MemberNotification service
     * to send notification.
     */
    @PostMapping("/activePlan")
    public ResponseEntity<String> sendUpdatePLanNotification (
            @RequestBody PlanActivationNotificationRequestDto requestDto) {
        memberNotificationService.sendPlanUpdateEmail(requestDto);
        return ResponseEntity.status(HttpStatus.OK).body("Email sent Successfully");
    }

    @PostMapping("/accountStatus")
    public ResponseEntity<String> sendAccountStatusInfo(@RequestBody NotificationFrozenRequestDto requestDto) {
        log.info("Request received to update status for member {},",requestDto.getName());
        String response =  memberNotificationService.sendAccountStatusUpdate(requestDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
