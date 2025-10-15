package com.gym.trainerService.Services.OtherServices;

import com.gym.trainerService.Dto.SessionDtos.Responses.SessionResponseDto;
import com.gym.trainerService.Dto.SessionDtos.Responses.UpdateSessionResponseDto;
import com.gym.trainerService.Models.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

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

    /**
     * Constructs the {@link WebClientService} with required dependencies.
     *
     * @param webClient                     WebClient builder for creating HTTP clients
     * @param memberService_BaseUrl_Session base URL for Member Service session endpoints (injected from application.properties)
     */
    public WebClientService(WebClient.Builder webClient,
                            @Value("${app.memberService.Base_Url.session}") String memberService_BaseUrl_Session) {
        this.webClient = webClient;
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
    @Async
    public void sendSessionToMember(Session session, double duration) {
        log.info("Request received to webclient service to create session for member");
        /** Building session response DTO */
        SessionResponseDto responseDto = SessionResponseDto.builder()
                .sessionId(session.getSessionId()).sessionName(session.getSessionName())
                .sessionDate(session.getSessionStartTime()).duration(duration)
                .build();

        /** Building request URL and inserting query params */
        String url = MemberService_BaseUrl_Session + "/addSession"
                + "?memberId=" + session.getMemberId()
                + "&trainerId=" + session.getTrainerId();
        
        /** Making asynchronous POST request to Member Service */
        webClient.build().post()
                .uri(url)
                .bodyValue(responseDto)
                .retrieve()
                .toBodilessEntity()
                .subscribe(
                        success-> log.info("{}:: Successfully send to {}"
                                ,success.getStatusCode(),MemberService_BaseUrl_Session),
                error-> log.error(error.getMessage())
        );
    }

    /**
     * Sends an update request to Member Service when a session is modified in Trainer Service.
     *
     * @param session the updated {@link Session} entity
     */
    @Async
    public void updateSessionToMember(Session session) {
        log.info("Request received to webclient service for session update");
        /** Building update session response DTO */
        UpdateSessionResponseDto responseDto = UpdateSessionResponseDto.builder()
                .trainerId(session.getTrainerId())
                .sessionName(session.getSessionName())
                .sessionStartTime(session.getSessionStartTime()).sessionEndTime(session.getSessionEndTime())
                .build();

        /** Building request URL and inserting query params */
        String url = MemberService_BaseUrl_Session + "/update-session"
                + "?sessionId=" + session.getSessionId()
                + "&memberId=" + session.getMemberId();

        /** Making asynchronous PUT request to Member Service */
        webClient.build().put()
                .uri(url)
                .bodyValue(responseDto).retrieve()
                .toBodilessEntity()
                .subscribe(
                        success-> log.info("{}:: Successfully send update request to {}"
                        ,success.getStatusCode(),MemberService_BaseUrl_Session+"/update-session"),
                error-> log.error(error.getLocalizedMessage(),error.getCause())
        );
    }

    /**
     * Sends a delete request to Member Service when a session is removed from Trainer Service.
     *
     * @param sessionId unique session identifier
     * @param memberId  identifier of the member linked to this session
     */
    @Async
    public void deleteSessionForMember(String sessionId, String memberId) {
        log.info("Request received to webclient service for session delete");
        /** Building request URL and inserting query params */
        String url = MemberService_BaseUrl_Session+"/session"+
                "?sessionId=" + sessionId +
                "&memberId=" + memberId;
        /** Making asynchronous DELETE request to Member Service */
        webClient.build().delete()
                .uri(url)
                .retrieve()
                .toBodilessEntity()
                .subscribe(
           success-> log.info("{} successfully send to member-service to delete session {}",
                        success.getStatusCode(),url),
                error-> log.error(String.valueOf(error.getCause()))
                );
    }
}
