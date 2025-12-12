package com.gym.adminservice.Controllers;

import com.gym.adminservice.Dto.Requests.ReportOrMessageCreationRequestDto;
import com.gym.adminservice.Dto.Responses.GenericResponseDto;
import com.gym.adminservice.Dto.Wrappers.AllMessageWrapperResponseDto;
import com.gym.adminservice.Services.AuthService.ReportAndMessageService;
import com.gym.adminservice.Utils.CustomAnnotations.Annotations.LogExecutionTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("${admin.REPORT_MESSAGE_URL}")
public class ReportAndMessageController {

    private final ReportAndMessageService reportAndMessageService;

    @LogExecutionTime
    @PostMapping("/users/launchReport")
    ResponseEntity<GenericResponseDto> makeNewMessageOrReport(ReportOrMessageCreationRequestDto requestDto){
        log.info("©️©️ request received to launch a report for {} by {} on {}",
                requestDto.getSubject(),requestDto.getUserName(),requestDto.getMessageTime());
        GenericResponseDto response = reportAndMessageService.makeReportOrMessage(requestDto);
        log.info("New Report Saved sending response as [ {} ]",response.getMessage());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @LogExecutionTime
    @GetMapping("/users/status")
    ResponseEntity<AllMessageWrapperResponseDto> getStatusByUserId(
            @RequestParam @NotBlank(message = "Unable To Process request without Having valid id") String userId
    ) {
        log.info("©️©️ request received to get report status for {}",userId);
        AllMessageWrapperResponseDto response = reportAndMessageService.viewAllReportsById(userId);
        log.info("serving response for all reports of count --> {} for user {}",
                response.getReportsLists().size(),userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @LogExecutionTime
    @DeleteMapping("/users/deleteReport")
    ResponseEntity<GenericResponseDto> deleteMessageOrReportByUser(
            @RequestParam @NotBlank (message = "Unable To Process request without Having valid id") String userId,
            @RequestParam @NotBlank (message = "Unable to Process Without Having any Request Id") String requestId
    ){
        log.info("©️©️ request received to delete report--> {} by {} ",requestId,userId);
        GenericResponseDto response = reportAndMessageService.deleteReportByUser(userId, requestId);
        log.info("deleted successfully serving response as --> [ {} ]",response.getMessage());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @LogExecutionTime
    @GetMapping("/admin/getAll/{pageNo}")
    ResponseEntity<AllMessageWrapperResponseDto> getAllReportsForAdmin(
            @PathVariable @PositiveOrZero(message = "Page No can not be Negative") int pageNo,
            @RequestParam @Positive(message = "Page Size must be greater than Zero") int pageSize,
            @RequestParam(required = false, defaultValue = "messageTime") String sortBy,
            @RequestParam(required = false, defaultValue = "DESC") String sortDirection
    ) {
        log.info("©️©️ request get -> {} no of reports of page[{}] witch direction {} by {}",
                pageSize, pageNo, sortDirection, sortBy);
        AllMessageWrapperResponseDto response = reportAndMessageService
                .getAllReportsForAdmin(pageNo, pageSize, sortBy, sortDirection);
        log.info("serving response of {} elements and the page is {}", response.getTotalElements(),
                response.isLastPage() ? "Last" : "Not Last");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @LogExecutionTime
    @PostMapping("/admin/")
}
