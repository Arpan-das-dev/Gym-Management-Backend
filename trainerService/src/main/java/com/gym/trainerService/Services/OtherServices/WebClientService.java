package com.gym.trainerService.Services.OtherServices;

import com.gym.trainerService.Dto.SessionDtos.Responses.SessionResponseDto;
import com.gym.trainerService.Dto.SessionDtos.Responses.UpdateSessionResponseDto;
import com.gym.trainerService.Dto.TrainerMangementDto.Responses.FreezeTrainerResponseDto;
import com.gym.trainerService.Exception.Custom.InterServiceCommunicationError;
import com.gym.trainerService.Models.Session;
import com.gym.trainerService.Models.Trainer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service responsible for inter-service communication between Trainer Service and Member Service.
 * <p>
 * Uses Spring’s reactive {@link WebClient} to perform non-blocking HTTP requests for session synchronization.
 * </p>
 *
 * <p><b>Responsibilities:</b></p>
 * <ul>
 *   <li>Send newly created session data to Member Service.</li>
 *   <li>Update session information in Member Service when modified.</li>
 *   <li>Notify Member Service when a session is deleted.</li>
 * </ul>
 *
 * <p><b>Annotations Used:</b></p>
 * <ul>
 *   <li>{@code @Service} – Marks this class as a Spring-managed service component.</li>
 *   <li>{@code @Slf4j} – Enables structured logging using SLF4J.</li>
 *   <li>{@code @Async} – Ensures non-blocking background calls to Member Service.</li>
 * </ul>
 *
 * <p><b>Threading:</b> All HTTP calls are asynchronous to avoid blocking the request thread in Trainer Service.</p>
 *
 * @author Arpan Das
 * @since 1.0
 */

@Service
@Slf4j
public class WebClientService {

    /** WebClient builder for creating HTTP clients. */
    private final WebClient.Builder webClient;

    /** Base URL for Member Service session-related endpoints. */
    private final String MemberService_BaseUrl_Session;
    private final String NotificationService_Freeze;
    /**
     * Constructs the {@link WebClientService} with required dependencies.
     *
     * @param webClient                     WebClient builder for creating HTTP clients
     * @param memberService_BaseUrl_Session base URL for Member Service session endpoints (injected from application.properties)
     */
    public WebClientService(WebClient.Builder webClient,
                            @Value("${app.memberService.Base_Url.session}") String memberService_BaseUrl_Session,
                            @Value("${app.notificationService.Base_Url.freeze}") String Notification) {
        this.webClient = webClient;
        this.NotificationService_Freeze = Notification;
        MemberService_BaseUrl_Session = memberService_BaseUrl_Session;
    }

    /**
     * Sends a newly created session from Trainer Service to Member Service.
     * <p>
     * The call is asynchronous and non-blocking using {@link WebClient}.
     * </p>
     *
     * @param session  the created {@link Session} entity
     * @param duration session duration in hours
     */

    public Mono<String> sendSessionToMember(Session session, double duration) {
        log.info("Request received to webclient service to create session for member");
        // Building session response DTO
        SessionResponseDto responseDto = SessionResponseDto.builder()
                .sessionId(session.getSessionId()).sessionName(session.getSessionName())
                .sessionDate(session.getSessionStartTime()).duration(duration)
                .build();

        // Building request URL and inserting query params
        String url = MemberService_BaseUrl_Session + "/addSession"
                + "?memberId=" + session.getMemberId()
                + "&trainerId=" + session.getTrainerId();
        
        // Making asynchronous POST request to Member Service
        return webClient.build().post()
                .uri(url)
                .bodyValue(responseDto)
                .exchangeToMono(res-> {
                    if(res.statusCode().is2xxSuccessful()) {
                        return res.bodyToMono(String.class)
                                .defaultIfEmpty("Successfully Added Session ")
                                .doOnNext(msg-> log.info("👍🏻👍🏻 response received from memberservice {}",
                                        msg));
                    } else {
                        throw new InterServiceCommunicationError("Unable to Add Sessions right now to due Internal Issue in Member Service");
                    }
                });
    }

    /**
     * Sends an update request to Member Service when a session is modified in Trainer Service.
     *
     * @param session the updated {@link Session} entity
     */

    public Mono<String> updateSessionToMember(Session session) {
        log.info("Request received to webclient service for session update");
        // Building update session response DTO
        UpdateSessionResponseDto responseDto = UpdateSessionResponseDto.builder()
                .trainerId(session.getTrainerId())
                .sessionName(session.getSessionName())
                .sessionStartTime(session.getSessionStartTime()).sessionEndTime(session.getSessionEndTime())
                .build();

        // Building request URL and inserting query params
        String url = MemberService_BaseUrl_Session + "/update-session"
                + "?sessionId=" + session.getSessionId()
                + "&memberId=" + session.getMemberId();

        // Making asynchronous PUT request to Member Service
        return webClient.build().put()
                .uri(url)
                .bodyValue(responseDto)
                .exchangeToMono(res -> {
                    if (res.statusCode().is2xxSuccessful()) {
                        return res.bodyToMono(String.class)
                                .defaultIfEmpty("Successfully Updated Session")
                                .doOnNext(msg -> log.info("Get response from member service--> {}", msg));
                    } else {
                        throw new InterServiceCommunicationError("Unable to Add Sessions right now " +
                                "to due Internal Issue in Member Service");
                    }
                });

}

    /**
     * Sends a delete request to Member Service when a session is removed from Trainer Service.
     *
     * @param sessionId unique session identifier
     * @param memberId  identifier of the member linked to this session
     */

    public Mono<String> deleteSessionForMember(String sessionId, String memberId) {
        log.info("Request received to webclient service for session delete");
        // Building request URL and inserting query params
        String url = MemberService_BaseUrl_Session+"/session"+
                "?sessionId=" + sessionId +
                "&memberId=" + memberId;
        // Making asynchronous DELETE request to Member Service
        return webClient.build().delete()
                .uri(url)
                .exchangeToMono(res -> {
                    if (res.statusCode().is2xxSuccessful()) {
                        return res.bodyToMono(String.class)
                                .defaultIfEmpty("Successfully Updated Session")
                                .doOnNext(msg -> log.info("Get response from member service --> {}", msg));
                    } else {
                        throw new InterServiceCommunicationError("Unable to Add Sessions right now " +
                                "to due Internal Issue in Member Service");
                    }
                });
    }

    public Mono<String> updateSessionStatusForMember(String sessionId, String memberId, String trainerId,String status) {
        log.info("Request received to webclient service for set session status");
        String url = MemberService_BaseUrl_Session+"/setStatus"+
                "?sessionId="+sessionId+"&memberId="+memberId+"&trainerId="+trainerId
                +"&status="+status;
        return webClient.build().put()
                .uri(url)
                .exchangeToMono(res-> {
                    if(res.statusCode().is2xxSuccessful()) {
                        return res.bodyToMono(String.class)
                                .defaultIfEmpty("Successfully Updated Session Status as "+status)
                                .doOnNext(msg->log.info("{} get response from member service on url --> {}"
                                        ,res.statusCode(),url));
                    } else {
                        throw new InterServiceCommunicationError("Unable to Add Sessions right now " +
                                "to due Internal Issue in Member Service");
                    }
                }).onErrorResume(err->{
                    log.warn("an error occurred due to {}",err.getMessage());
                    return null;
                });
    }

    public Mono<Boolean> notifyForFreezeOrUnFreeze(boolean value, Trainer trainer) {
        String endpoint = NotificationService_Freeze+"/freeze";
        String subject = value ? "Account Frozen" : "Account UnFrozen";

        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String formattedString = dateTime.format(formatter);

        FreezeTrainerResponseDto requestDto = FreezeTrainerResponseDto.builder()
                .frozen(value)
                .trainerName(trainer.getFirstName()+" "+trainer.getLastName())
                .subject(subject)
                .trainerMail(trainer.getEmail())
                .time(formattedString)
                .build();
        return webClient.build()
                .post()
                .uri(endpoint)
                .bodyValue(requestDto)
                .exchangeToMono(res -> {
                    if (res.statusCode().is2xxSuccessful()) {
                        log.info("📩 Notification sent to {}", trainer.getEmail());
                        return Mono.just(true);
                    }
                    log.warn("⚠️ Notification failed | status={} | trainerId={}",
                            res.statusCode(), trainer.getTrainerId());
                    return Mono.just(false);
                })
                .onErrorResume(err -> {
                    log.warn("⚠️ Notification service unreachable | trainerId={} | reason={}",
                            trainer.getTrainerId(), err.getMessage());
                    return Mono.just(false);
                });
    }
}
