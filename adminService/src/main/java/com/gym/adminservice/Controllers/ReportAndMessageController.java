package com.gym.adminservice.Controllers;

import com.gym.adminservice.Dto.Requests.ReportOrMessageCreationRequestDto;
import com.gym.adminservice.Dto.Requests.ResolveMessageRequestDto;
import com.gym.adminservice.Dto.Responses.GenericResponseDto;
import com.gym.adminservice.Dto.Wrappers.AllMessageWrapperResponseDto;
import com.gym.adminservice.Services.AuthService.ReportAndMessageService;
import com.gym.adminservice.Utils.CustomAnnotations.Annotations.LogExecutionTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("${admin.REPORT_MESSAGE_URL}")
public class ReportAndMessageController {

    private final ReportAndMessageService reportAndMessageService;

    @LogExecutionTime
    @PostMapping("/users/launchReport")
    ResponseEntity<GenericResponseDto> makeNewMessageOrReport(
            @Valid @RequestBody ReportOrMessageCreationRequestDto requestDto){
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
    @GetMapping("/administrator/getReports/{pageNo}/{pageSize}")
    public ResponseEntity<AllMessageWrapperResponseDto> getAllReportsForAdmin(
            @PathVariable @PositiveOrZero(message = "Page No can not be Negative") int pageNo,
            @PathVariable @Positive(message = "Page Size must be greater than Zero") int pageSize,
            @RequestParam(defaultValue = "messageTime") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(defaultValue = "ALL") String role,
            @RequestParam(defaultValue = "ALL") String status
    ) {
        log.info("©️©️ Admin request → page={}, size={}, sortBy={}, direction={}, role={}, status={}",
                pageNo, pageSize, sortBy, sortDirection, role, status);
        AllMessageWrapperResponseDto response = reportAndMessageService
                .getAllReportsForAdmin(pageNo, pageSize, sortBy, sortDirection, role.toUpperCase(), status.toUpperCase());
        log.info("Serving response of {} elements",response.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @LogExecutionTime
    @PostMapping("/administrator/{userId}")
    ResponseEntity<GenericResponseDto> markAsResolveOrDelete(
            @PathVariable @NotBlank(message = "Unable To Process request without Having valid id") String userId,
            @Valid @RequestBody ResolveMessageRequestDto requestDto) {
        log.info("©️©️ request received to update status request of user --> {} ",userId);
        GenericResponseDto response = reportAndMessageService.resolveMessageOrReport(userId,requestDto);
        log.info("Serving response as [ {} ]",response.getMessage());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
