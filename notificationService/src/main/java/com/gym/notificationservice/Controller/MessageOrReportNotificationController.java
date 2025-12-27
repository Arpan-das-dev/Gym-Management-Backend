package com.gym.notificationservice.Controller;

import com.gym.notificationservice.Dto.AuthNotificationRequests.MessageOrReportNotificationRequestDto;
import com.gym.notificationservice.Dto.MailNotificationDtos.FreezeTrainerRequestDto;
import com.gym.notificationservice.Dto.PaymentNotificationDtos.Responses.GenericAsyncResponseDto;
import com.gym.notificationservice.Dto.PaymentNotificationDtos.Responses.GenericResponseDto;
import com.gym.notificationservice.Services.AuthNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequestMapping("${messageOrReport.notification.url}")
@RequiredArgsConstructor
public class MessageOrReportNotificationController {

    private final  AuthNotificationService notificationService;
    @PostMapping("/resolved")
    public ResponseEntity<GenericResponseDto> sendMessageStatusMailByService(MessageOrReportNotificationRequestDto requestDto){
        return ResponseEntity.status(HttpStatus.OK)
                .body(notificationService.sendMessageOrReportResolvedStatus(requestDto));
    }
    @PostMapping("/sendMail")
    public ResponseEntity<GenericResponseDto> sendMailToMember(
           @RequestBody MessageOrReportNotificationRequestDto requestDto){
        return ResponseEntity.status(HttpStatus.OK)
                .body(notificationService.sendMessageOrReportResolvedStatus(requestDto));
    }

    @PostMapping("/freeze")
    public ResponseEntity<Void> informTrainer(FreezeTrainerRequestDto requestDto){
        return void;
    }
}
