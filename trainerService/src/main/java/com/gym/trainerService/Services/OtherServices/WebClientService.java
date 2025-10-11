package com.gym.trainerService.Services.OtherServices;

import com.gym.trainerService.Dto.SessionDtos.Responses.SessionResponseDto;
import com.gym.trainerService.Dto.SessionDtos.Responses.UpdateSessionResponseDto;
import com.gym.trainerService.Models.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
public class WebClientService {
    private final WebClient.Builder webClient;
    private final String MemberService_BaseUrl_Session;

    public WebClientService(WebClient.Builder webClient,
                            @Value("${app.memberService.Base_Url.session}") String memberService_BaseUrl_Session) {
        this.webClient = webClient;
        MemberService_BaseUrl_Session = memberService_BaseUrl_Session;
    }

    @Async
    public void sendSessionToMember(Session session, double duration) {
        SessionResponseDto responseDto = SessionResponseDto.builder()
                .sessionId(session.getSessionId()).sessionName(session.getSessionName())
                .sessionDate(session.getSessionStartTime()).duration(duration)
                .build();
        webClient.build().post().uri(URL-> URL
                .queryParam("memberId",session.getMemberId())
                .queryParam("trainerId",session.getTrainerId())
                .path(MemberService_BaseUrl_Session+"/addSession")
                .build()).bodyValue(responseDto).retrieve().toBodilessEntity().subscribe(
                        success-> log.info("{}:: Successfully send to {}"
                                ,success.getStatusCode(),MemberService_BaseUrl_Session),
                error-> log.error(error.getLocalizedMessage(),error.getMessage())
        );
    }

    @Async
    public void updateSessionToMember(Session session) {
        UpdateSessionResponseDto responseDto = UpdateSessionResponseDto.builder()
                .trainerId(session.getTrainerId())
                .sessionName(session.getSessionName())
                .sessionStartTime(session.getSessionStartTime()).sessionEndTime(session.getSessionEndTime())
                .build();
        webClient.build().put().uri(URL->URL
                .queryParam("sessionId",session.getSessionId())
                .queryParam("memberId",session.getMemberId())
                .path(MemberService_BaseUrl_Session+"/update-session")
                .build()).bodyValue(responseDto).retrieve().toBodilessEntity().subscribe(
                        success-> log.info("{}:: Successfully send update request to {}"
                        ,success.getStatusCode(),MemberService_BaseUrl_Session+"/update-session"),
                error-> log.error(error.getLocalizedMessage(),error.getMessage())
        );
    }
}
