package com.gym.adminservice.Services.AuthService;

import com.gym.adminservice.Dto.Requests.ReportOrMessageCreationRequestDto;
import com.gym.adminservice.Dto.Requests.ResolveMessageRequestDto;
import com.gym.adminservice.Dto.Responses.AllReportsList;
import com.gym.adminservice.Dto.Responses.GenericResponseDto;
import com.gym.adminservice.Dto.Wrappers.AllMessageWrapperResponseDto;
import com.gym.adminservice.Enums.Status;
import com.gym.adminservice.Exceptions.Custom.MessageNotFoundException;
import com.gym.adminservice.Models.Messages;
import com.gym.adminservice.Repository.MessageRepository;
import com.gym.adminservice.Services.WebClientServices.WebClientMessageOrReportService;
import com.gym.adminservice.Utils.ReportIdGenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportAndMessageService {
    private final MessageRepository messageRepository;
    private final ReportIdGenUtil reportIdGenUtil;
    private final WebClientMessageOrReportService messageOrReportService;

    @Caching(evict = {
            @CacheEvict(value = "messagesCache", key = "'userId:'#requestDto.userId")
    })
    public GenericResponseDto makeReportOrMessage(ReportOrMessageCreationRequestDto requestDto){
        Messages messages = Messages.builder()
                .messageId(reportIdGenUtil.createMessageId(requestDto.getUserId(), requestDto.getMessageTime()))
                .userId(requestDto.getUserId())
                .userName(requestDto.getUserName())
                .userRole(requestDto.getUserRole())
                .emailId(requestDto.getUserId())
                .subject(requestDto.getSubject())
                .message(requestDto.getMessage())
                .status(Status.Pending.name())
                .messageTime(requestDto.getMessageTime())
                .build();
        messageRepository.save(messages);
        String response =  messages.getUserName()+"Successfully registered your report wait for admin to response";
        return new GenericResponseDto(response);
    }

    @Cacheable(value = "messagesCache", key = "'userId:'#userId")
    public AllMessageWrapperResponseDto viewAllReportsById(String userId){
        List<Messages> messages = messageRepository.findAllByUserId(userId);
        return AllMessageWrapperResponseDto.builder()
                .reportsLists(messages.stream()
                        .map(m-> AllReportsList.builder()
                                .userName(m.getUserName())
                                .subject(m.getSubject())
                                .message(m.getMessage())
                                .messageTime(m.getMessageTime())
                                .messageStatus(m.getStatus())
                                .build()).toList())
                .lastPage(false).pageNo(0).pageSize(1).totalPages(1)
                .build();
    }

    @Caching(evict = {
            @CacheEvict(value = "messagesCache", key = "'userId:'#requestDto.userId"),
    })
    public GenericResponseDto deleteReportByUser(String userId,String requestId) {
        log.info("Request received to delete request for user {}",userId);
        Messages message = messageRepository.findById(requestId)
                .orElseThrow(() -> new MessageNotFoundException("No Reports or Message find please try again later"));
        messageRepository.delete(message);
        String response = message.getUserName()+"we have successfully deleted your message or report";
        return new GenericResponseDto(response);
    }

    public AllMessageWrapperResponseDto getAllReportsForAdmin(){
        return null;
    }

    @Caching(evict = {
            @CacheEvict(value = "messagesCache", key = "'userId:'#userId"),
    })
    public GenericResponseDto resolveMessageOrReport(String userId, ResolveMessageRequestDto requestDto){
        log.info("Request received to resolve message for user  {}",userId);
        Messages messages = messageRepository.findById(requestDto.getRequestId())
                .orElseThrow(() -> new MessageNotFoundException("No Reports or Message find please try again later"));
        if(requestDto.isDelete()) {
            messageRepository.delete(messages);
        }
        messages.setStatus(Status.Resolved.name());
        messageRepository.save(messages);
        String response = "Successfully marked resolved request for "+ messages.getUserName();
        String subject = "Request Resolved";
        Mono<String> notificationResponse =  messageOrReportService
                .sendMessageOrReportResolverMessage(messages.getEmailId(),subject, requestDto.getMailMessage());
        log.info("request received by notification service and get response as ::{}",notificationResponse);
        return new GenericResponseDto(response);
    }


}
