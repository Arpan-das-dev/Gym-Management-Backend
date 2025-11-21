package com.gym.notificationservice.Controller;

import com.gym.notificationservice.Dto.AuthNotificationRequests.MessageOrReportNotificationRequestDto;
import com.gym.notificationservice.Services.AuthNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("${messageOrReport.notification.url}")
@RequiredArgsConstructor
public class MessageOrReportNotificationController {

    private AuthNotificationService notificationService;
    @PostMapping("/resolved")
    public String sendMessageStatusMailByService(MessageOrReportNotificationRequestDto requestDto){
        return notificationService.sendMessageOrReportResolvedStatus(requestDto);
    }
}
