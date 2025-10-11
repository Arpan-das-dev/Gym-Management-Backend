package com.gym.trainerService.Controllers;

import com.gym.trainerService.Dto.SessionDtos.Requests.AddSessionRequestDto;
import com.gym.trainerService.Dto.SessionDtos.Requests.UpdateSessionRequestDto;
import com.gym.trainerService.Dto.SessionDtos.Wrappers.AllSessionsWrapperDto;
import com.gym.trainerService.Services.MemberServices.SessionManagementService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${trainer-service.Base_Url.sessionManagement}")
@Slf4j
@RequiredArgsConstructor
@Validated
public class SessionManagementController {

    private final SessionManagementService sessionManagementService;

    @PostMapping("/trainer/addSessions")
    public ResponseEntity<AllSessionsWrapperDto> addSession(@RequestParam String trainerId,
                                                            @Valid AddSessionRequestDto requestDto){
        log.info("Request received to add session for member {} with trainer {}"
                ,requestDto.getMemberId(),trainerId);
        AllSessionsWrapperDto response = sessionManagementService.addSession(trainerId,requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/trainer/updateSession")
    public ResponseEntity<AllSessionsWrapperDto> updateSession(@RequestParam String sessionId,
                                                               @Valid UpdateSessionRequestDto requestDto) {
        log.info("Request received for update session of id: {}",sessionId);
        AllSessionsWrapperDto response = sessionManagementService.updateSession(sessionId,requestDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/trainer/getSessions")
    public ResponseEntity<AllSessionsWrapperDto> getUpcomingSessions (@RequestParam String trainerId) {
        log.info("Request received to get upcoming sessions for trainer: {}",trainerId);
        AllSessionsWrapperDto response = sessionManagementService.getUpcomingSessions(trainerId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/trainer/getSession/${pageSize}")
    public ResponseEntity<AllSessionsWrapperDto> getPastSessions ( @PathVariable @Positive int pageSize,
                                                                   @RequestParam String trainerId,
                                                                   @RequestParam @Positive int pageNo) {
        log.info("Request received to get past sessions for pageNo: {}, of size: {}",pageNo,pageSize);
        AllSessionsWrapperDto response = sessionManagementService.getPastSessionsByPagination(trainerId,pageNo,pageSize);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/trainer/deleteSession")
    public ResponseEntity<String> deleteSessionBySessionId(@RequestParam String sessionId,
                                                           @RequestParam String trainerId) {
        String response = sessionManagementService.deleteSession(sessionId,trainerId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
