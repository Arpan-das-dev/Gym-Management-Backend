package com.gym.adminservice.Services.AuthService;

import com.gym.adminservice.Dto.Requests.ReportOrMessageCreationRequestDto;
import com.gym.adminservice.Dto.Requests.ResolveMessageRequestDto;
import com.gym.adminservice.Dto.Responses.AllReportsList;
import com.gym.adminservice.Dto.Responses.GenericResponseDto;
import com.gym.adminservice.Dto.Wrappers.AllMessageWrapperResponseDto;
import com.gym.adminservice.Enums.Status;
import com.gym.adminservice.Exceptions.Custom.MessageLimitReachedException;
import com.gym.adminservice.Exceptions.Custom.MessageNotFoundException;
import com.gym.adminservice.Exceptions.Custom.UnauthorizedRequestException;
import com.gym.adminservice.Models.Messages;
import com.gym.adminservice.Repository.MessageRepository;
import com.gym.adminservice.Services.WebClientServices.WebClientMessageOrReportService;
import com.gym.adminservice.Utils.CustomAnnotations.Annotations.LogRequestTime;
import com.gym.adminservice.Utils.ReportIdGenUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportAndMessageService {
    private final MessageRepository messageRepository;
    private final ReportIdGenUtil reportIdGenUtil;
    private final WebClientMessageOrReportService messageOrReportService;
    private final StringRedisTemplate redisTemplate;
    private final String  Redis_PreFix = "REPORT_COUNT::";

    @LogRequestTime
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "messagesCache",key = "'userId:' + #requestDto.userId"),
            @CacheEvict(value = "adminMessageCache", allEntries = true)
    })
    public GenericResponseDto makeReportOrMessage(ReportOrMessageCreationRequestDto requestDto){
        log.info("®️®️ reached Service class to make a new report");
        validateCount(requestDto.getUserId());
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
        clearReportCountCache(requestDto.getUserId());
        log.info("👍🏻👍🏻 saved new report by {} on {}",messages.getUserName(),messages.getMessageTime().toLocalDate());
        String response =  messages.getUserName()+"Successfully registered your report wait for admin to response";
        return new GenericResponseDto(response);
    }



    private void validateCount( String userId) {
        log.info("Request received to validate count  for {} before adding it in db",userId);
        Integer count = extractCount(userId);
        if(count>=5) {
            log.info("limit reached current count is {} throwing an exception 💀💀",count);
            throw new MessageLimitReachedException("You Already raised 5 or more Reports please Wait for admin To respond");
        }
    }
    private Integer extractCount(String userId){
        String key = Redis_PreFix+userId;
        String value = redisTemplate.opsForValue().get(key);
        Integer count ;
        log.info("retrieved the value from string redis template on line 86 --> {}",value);
        if(value==null){
            log.info("Value is null now fetching it from db ");
            count = messageRepository.countByUserId(userId);
            log.info("Count received from db is {}",count);
            redisTemplate.opsForValue().set(key, String.valueOf(count), Duration.ofHours(6));
            log.info("Stored value in the redis with key {}",key);
            return count;
        } else{
            return Integer.parseInt(value);
        }
    }
    private void clearReportCountCache(String userId) {
        redisTemplate.delete(Redis_PreFix + userId);
    }

    @LogRequestTime
    @Cacheable(value = "messagesCache", key = "'userId:' + #userId")
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

    @LogRequestTime
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "messagesCache", key = "'userId:' + #requestDto.userId"),
            @CacheEvict(value = "adminMessageCache", allEntries = true)
    })
    public GenericResponseDto deleteReportByUser(String userId,String requestId) {
        log.info("Request received to delete request for user {}",userId);
        Messages message = messageRepository.findById(requestId)
                .orElseThrow(() -> new MessageNotFoundException("No Reports or Message find please try again later"));
        if(!message.getUserId().equals(userId)) {
            throw new UnauthorizedRequestException("You Can not delete Other Reports idiot");
        }
        messageRepository.delete(message);
        String response = message.getUserName()+"we have successfully deleted your message or report";
        clearReportCountCache(userId);
        return new GenericResponseDto(response);
    }


    @LogRequestTime
    @Cacheable(
            value = "adminMessageCache",
            key = "'ADMIN:' + #pageNo + ':' + #pageSize + ':' + #sortBy + ':' + #sortDirection + ':' + #role + ':' + #status"
    )
    public AllMessageWrapperResponseDto getAllReportsForAdmin
            (int pageNo, int pageSize, String sortBy,String sortDirection, String role, String status) {
        log.info("®️®️ reached service class");
        Sort.Direction direction =
                sortDirection.equalsIgnoreCase("ASC")
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;

        Sort sort = switch (sortBy.toUpperCase()) {
            case "USERNAME" -> Sort.by(direction, "userName");
            case "SUBJECT"  -> Sort.by(direction, "subject");
            case "ROLE"     -> Sort.by(direction, "userRole");
            case "STATUS"   -> Sort.by(direction, "status");
            default         -> Sort.by(direction, "messageTime");
        };

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        log.info("Requesting db 🛄🛄 for direction of {} with sorting {}",direction,sort);
        Page<Messages> page = messageRepository.findAllWithFilters(role, status, pageable);
        log.info("Loaded {} no of reports from db ",page.getSize());
        List<AllReportsList> reports = page.getContent().stream()
                .map(m -> AllReportsList.builder()
                        .userId(m.getUserId())
                        .userName(m.getUserName())
                        .userRole(m.getUserRole())
                        .subject(m.getSubject())
                        .message(m.getMessage())
                        .messageTime(m.getMessageTime())
                        .messageStatus(m.getStatus())
                        .build()
                ).toList();

        return AllMessageWrapperResponseDto.builder()
                .reportsLists(reports)
                .pageNo(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .lastPage(page.isLast())
                .build();
    }

    @LogRequestTime
    @Caching(evict = {
            @CacheEvict(value = "messagesCache", key = "'userId:'#userId"),
            @CacheEvict(value = "adminMessageCache", allEntries = true)
    })
    public GenericResponseDto resolveMessageOrReport(String userId, ResolveMessageRequestDto requestDto){
        log.info("®️®️ Request received to resolve message for user  {}",userId);
        Messages messages = messageRepository.findById(requestDto.getRequestId())
                .orElseThrow(() -> new MessageNotFoundException("No Reports or Message find please try again later"));
        String subject = "Request Resolved";
        if(requestDto.isDecline()) {
            messages.setStatus(Status.Declined.name().toUpperCase());
            messageRepository.save(messages);
           subject = "Request Declined";
        }
        messages.setStatus(Status.Resolved.name().toUpperCase());
        messageRepository.save(messages);
        String status = requestDto.isDecline() ? Status.Declined.name() : Status.Resolved.name();
        String response = "Successfully "+ status +" request for "+ messages.getUserName();
        if(!messages.getEmailId().contains("@example.com") &&
                requestDto.isNotify() && requestDto.getMailMessage()!=null)
        {
            Mono<String> notificationResponse =  messageOrReportService
                    .sendMessageOrReportResolverMessage(messages.getEmailId(),subject, requestDto.getMailMessage());
            log.info("request received by notification service and get response as ::{}",notificationResponse);
        }
        clearReportCountCache(messages.getUserId());
        return new GenericResponseDto(response);
    }



}
