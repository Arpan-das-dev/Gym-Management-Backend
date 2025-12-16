package com.gym.adminservice.Services.WebClientServices;

import com.gym.adminservice.Dto.Responses.MessageOrReportNotificationResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class WebClientMessageOrReportService {
    private final WebClient.Builder webclient;
    private final String notificationUrl;

    public WebClientMessageOrReportService(@Value("${app.messageOrReport_Notification.url}") String notificationUrl,
                                           WebClient.Builder webclient)
    {
        this.notificationUrl = notificationUrl;
        this.webclient = webclient;
    }


    public Mono<String> sendMessageOrReportResolverMessage(String to, String subject, String message) {
        String url = notificationUrl+ "/resolved";
        MessageOrReportNotificationResponseDto payLoad = MessageOrReportNotificationResponseDto.builder()
                .sendTo(to)
                .subject(subject)
                .message(message)
                .build();
        return webclient.build().post()
                .uri(url)
                .bodyValue(payLoad)
                .retrieve().bodyToMono(String.class)
                .doOnSuccess(s-> log.info("Successfully sent request to {}",url))
                .doOnError(e-> log.info("failed to sent request to {} due to {}",url,e.getMessage()));
    }
}
