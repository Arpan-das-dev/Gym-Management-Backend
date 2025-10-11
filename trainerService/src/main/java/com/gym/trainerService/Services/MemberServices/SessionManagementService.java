package com.gym.trainerService.Services.MemberServices;

import com.gym.trainerService.Dto.SessionDtos.Requests.AddSessionRequestDto;
import com.gym.trainerService.Dto.SessionDtos.Requests.UpdateSessionRequestDto;
import com.gym.trainerService.Dto.SessionDtos.Responses.AllSessionResponseDto;
import com.gym.trainerService.Dto.SessionDtos.Wrappers.AllSessionsWrapperDto;

import com.gym.trainerService.Exception.*;
import com.gym.trainerService.Models.Member;
import com.gym.trainerService.Models.Session;
import com.gym.trainerService.Repositories.MemberRepository;
import com.gym.trainerService.Repositories.SessionRepository;
import com.gym.trainerService.Repositories.TrainerRepository;
import com.gym.trainerService.Services.OtherServices.WebClientService;
import com.gym.trainerService.Utils.SessionIdGenUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SessionManagementService {

    private final MemberRepository memberRepository;
    private final TrainerRepository trainerRepository;
    private final SessionRepository sessionRepository;
    private final WebClientService webClientService;
    private final SessionIdGenUtil sessionIdGenUtil;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Transactional
    @CachePut(value = "AllSessionCache", key = "#trainerId")
    public AllSessionsWrapperDto addSession(String trainerId, AddSessionRequestDto requestDto) {
        if(!trainerRepository.existsById(trainerId)) {
            log.warn("No trainer found for the trainer with the id ---> {}",trainerId);
            throw new NoTrainerFoundException("No trainer found with this id :: "+trainerId);
        }
        Member member = memberRepository.findByTrainerIdMemberId(trainerId,requestDto.getMemberId())
                .orElseThrow(()-> new MemberNotFoundException("No member found for this credentials"));
        log.info("fetched member :: {} from database ",member.getMemberName());
        if(member.getEligibilityEnd().isBefore(requestDto.getSessionDate().toLocalDate())) {
            log.warn("Unable to add new session as the plan expired for the member --> {} on :: {}",
                    member.getMemberName(), member.getEligibilityEnd());
            throw new PlanExpirationException("Unable to add sessions due to plan expired");
        }

        LocalDateTime startTime = requestDto.getSessionDate();
        LocalDateTime endTime = startTime.plusMinutes(Math.round(requestDto.getDuration() * 60));
        if(sessionRepository.sessionSlotCheck(startTime,endTime).isPresent()) {
            log.warn("Wrong slot, no empty slot found between {} and {} "
                    ,startTime.format(formatter),endTime.format(formatter));
            throw new InvalidSessionException("No empty slot is available between "+startTime.format(formatter) +
                    " & "+ endTime.format(formatter));
        }
        String sessionId = sessionIdGenUtil
                .generateSessionId(requestDto.getMemberId(), member.getTrainerId(),startTime,endTime);
        Session session = Session.builder()
                .sessionId(sessionId).sessionName(requestDto.getSessionName())
                .memberId(member.getMemberId()).trainerId(member.getTrainerId())
                .sessionStartTime(startTime).sessionEndTime(endTime)
                .build();
        sessionRepository.save(session);
        log.info("Successfully saved session on {}",session.getSessionStartTime().format(formatter));
        webClientService.sendSessionToMember(session,requestDto.getDuration());
        log.info("Model sent to webClient service class ");
        return responseDtoBuilderForAllSessionForTrainer(session.getTrainerId());
    }

    @Transactional
    @CachePut(value = "AllSessionCache", key = "#requestDto.trainerId")
    public AllSessionsWrapperDto updateSession(String sessionId, UpdateSessionRequestDto requestDto) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(()-> new NoSessionFoundException("No session found with this id: "+sessionId));
        log.info("Successfully retrieved session of id: {} from database",session.getSessionId());
        if(! session.getTrainerId().equals(requestDto.getTrainerId()) ||
        !session.getMemberId().equals(requestDto.getMemberId())) {
            log.warn("Invalid credentials for trainer or member id");
            throw new InvalidSessionException("session does not matches with trainer or member");
        }
        LocalDateTime startTime = requestDto.getSessionDate();
        LocalDateTime endTime = startTime.plusMinutes(Math.round(requestDto.getDuration() * 60));
        if(sessionRepository.sessionSlotCheck(startTime,endTime).isPresent()) {
            log.warn("Wrong slot no empty slot found between {} and {} "
                    ,startTime.format(formatter),endTime.format(formatter));
            throw new InvalidSessionException("No empty slot is available between "+startTime.format(formatter) +
                    " & "+ endTime.format(formatter));
        }
        session.setSessionName(requestDto.getSessionName());
        session.setSessionStartTime(startTime);
        session.setSessionEndTime(endTime);
        sessionRepository.save(session);
        log.info("Session updated with name {} between {} - {}", session.getSessionName(),
                session.getSessionStartTime().format(formatter),session.getSessionEndTime().format(formatter));
        webClientService.updateSessionToMember(session);
        log.info("Dto send to webClient service for further process");
        return responseDtoBuilderForAllSessionForTrainer(session.getTrainerId());
    }

    @Cacheable(value = "AllSessionCache", key = "#requestDto.trainerId")
    public AllSessionsWrapperDto getUpcomingSessions(String trainerId) {
        if(trainerRepository.existsById(trainerId)) {
            log.info("successfully retrieved sessions form database for trainer {}",trainerId);
            return responseDtoBuilderForAllSessionForTrainer(trainerId);
        }
        log.warn("No trainer found with the id: {}",trainerId);
        throw new NoTrainerFoundException("No trainer found with this id: "+trainerId);
    }

    @Cacheable(value = "AllSessionCache", key = "#trainerId + ':' + #pageNo + ':' + #pageSize")
    public AllSessionsWrapperDto getPastSessionsByPagination(String trainerId, int pageNo, int pageSize) {
        log.info("Request received in service class for past sessions for size {} and page no {}",pageSize,pageNo);
        Pageable pageRequest = PageRequest.of(pageNo,pageSize);
        List<Session> sessionList = sessionRepository
                .findPaginatedDataByTrainerId(trainerId,LocalDateTime.now(),pageRequest).stream().toList();
        log.info("Successfully retrieved {} sessions for the trainer id:: {}",sessionList.size(),trainerId);
        List<AllSessionResponseDto> responseDtoList = sessionList.stream().map(session -> AllSessionResponseDto
                .builder()
                .sessionId(session.getSessionId()).sessionName(session.getSessionName())
                .memberId(session.getMemberId())
                .sessionStartTime(session.getSessionStartTime()).sessionEndTime(session.getSessionEndTime())
                .build()).toList();
        log.info("successfully build {} sessions as response", responseDtoList.size());
        return AllSessionsWrapperDto.builder()
                .responseDtoList(responseDtoList)
                .build();
    }

    @Caching(evict = {
            @CacheEvict(value = "AllSessionCache", key = "trainerId"),
            @CacheEvict(value = "AllSessionCache", key = "trainerId + '*'")
    })
    public String deleteSession(String sessionId,String trainerId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(()-> new NoSessionFoundException("No session found with this id: "+sessionId));
        log.info("Successfully retrieved session of id: {} from database to delete",session.getSessionId());
        if(!session.getTrainerId().equals(trainerId)) {
            log.warn("Session and trainer id mismatch");
            throw new InvalidSessionException("Invalid session with trainerId");
        }
        sessionRepository.deleteById(session.getSessionId());
        log.info("Successfully deleted session on {}",session.getSessionStartTime().format(formatter));
        return "Successfully deleted session of id:: "+session.getSessionId();
    }

    private AllSessionsWrapperDto responseDtoBuilderForAllSessionForTrainer(String trainerId) {
        log.info("Method handling to building response dto");
        List<Session> sessionList = sessionRepository.findByTrainerId(trainerId,LocalDateTime.now());
        log.info("Successfully retrieved {} no of sessionList for trainer {} from database",sessionList.size(),trainerId);
        List<AllSessionResponseDto> responseDto = sessionList.stream().map(session ->
                AllSessionResponseDto.builder()
                .sessionId(session.getSessionId()).sessionName(session.getSessionName())
                .memberId(session.getMemberId())
                .sessionStartTime(session.getSessionStartTime()).sessionEndTime(session.getSessionEndTime())
                .build()).toList();
        return AllSessionsWrapperDto.builder()
                .responseDtoList(responseDto)
                .build();
    }

}
