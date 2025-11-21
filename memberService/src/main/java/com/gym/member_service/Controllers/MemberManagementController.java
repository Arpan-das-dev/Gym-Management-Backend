package com.gym.member_service.Controllers;

import com.gym.member_service.Dto.MemberManagementDto.Requests.FreezeRequestDto;
import com.gym.member_service.Dto.MemberManagementDto.Requests.MemberCreationRequestDto;
import com.gym.member_service.Dto.MemberManagementDto.Responses.AllMemberResponseDto;
import com.gym.member_service.Dto.MemberManagementDto.Responses.LoginStreakResponseDto;
import com.gym.member_service.Dto.MemberManagementDto.Wrappers.AllMembersInfoWrapperResponseDtoList;
import com.gym.member_service.Dto.NotificationDto.GenericResponse;
import com.gym.member_service.Services.MemberServices.MemberManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("${member-service.BASE_URL}")
@RequiredArgsConstructor
@Validated
/*
 * This controller allows system to manage members
 * like - create, get member details, delete or freeze account
 * the url for this controller is defined in application.properties
 * if the url's endpoint starts with 'admin' means only admin is allowed to
 * do this performance
 */
public class MemberManagementController {
    // injecting MemberManagementService by using constructor(@RequiredArgConstructor)
    private final MemberManagementService memberManagementService;

    /*
     * this method opens a new endpoint so the auth service
     * or admin service  can
     * request here to creates a new member
     * it took a Valid body as a parameter to creates a member
     */
    @PostMapping("create")
    public ResponseEntity<GenericResponse> createMember (@Valid @RequestBody MemberCreationRequestDto requestDto){
        // set response to return in response
        String  response = memberManagementService.createMember(requestDto);
        GenericResponse res = new GenericResponse(response);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
        // If member creates successfully returns CREATED as http status
    }

    /*
     * this method opens a new endpoint so the
     * admin service  can
     * request here to freeze a member account
     * it took a Valid member id(@RequestParam) parameter to freeze a member
     */
    @PostMapping("admin/freeze")
    public ResponseEntity<GenericResponse> freeze(@Valid @RequestBody FreezeRequestDto requestDto){
        String response =  memberManagementService.freezeOrUnFrozen(requestDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new GenericResponse(response));
    }

    /*
     * this method opens a new endpoint so the
     * auth service  can send a request here
     * to increase or get login Streak
     * it took a Valid member id(@RequestParam) parameter to
     * set the login Streak and returns current login streak
     */
    @PostMapping("setStreak")
    public ResponseEntity<LoginStreakResponseDto> setLoginStreak(@RequestParam String id) {
        LoginStreakResponseDto response = memberManagementService.setLoginStreak(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @GetMapping("getStreak")
    public ResponseEntity<LoginStreakResponseDto> getLoginStreak(@RequestParam String id){
        // set response to send as response
        LoginStreakResponseDto res = memberManagementService.getLoginStreak(id);
        return ResponseEntity.status(HttpStatus.OK).body(res);
        // if successfully operation done returns response with OK http status
    }

    /*
     * this method opens a new endpoint so the
     * frontend  can send a request here
     * to  get member basic details
     * it took a Valid member id(@RequestParam) parameter to
     * returns a members basic details
     */
    @GetMapping("getBy")
    public ResponseEntity<AllMemberResponseDto> getMemberById(@RequestParam String id){
        // set response to send as response
        AllMemberResponseDto response = memberManagementService.getMemberById(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
        // if successfully operation done returns response with OK http status
    }

    /*
     * this method opens a new endpoint so the
     * frontend  can send a request here
     * to  get All member basic details
     * so that reflect real time data in admin's dashboard
     * returns a members basic details
     */
    @GetMapping("admin/getAll")
    public ResponseEntity<AllMembersInfoWrapperResponseDtoList> getAllMembers(
            @RequestParam(required = false, defaultValue = "") String searchBy,
            @RequestParam(required = false, defaultValue = "") String gender,
            @RequestParam(required = false, defaultValue = "") String status,
            @RequestParam(required = false, defaultValue = "planExpiration") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection,
            @RequestParam(required = false, defaultValue = "0") Integer pageNo,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize
    ){
        pageNo = Math.max(pageNo,0);
        pageSize = pageSize < 0 ?  20  : pageSize;
        log.info(
 "Admin Transaction Request | page={} | size={} | searchBy='{}' | sortBy='{}' | direction='{}' | gender='{}' | status = '{}'",
pageNo, pageSize, searchBy, sortBy, sortDirection,gender,status
        );
        // set response to send as response
        AllMembersInfoWrapperResponseDtoList response = memberManagementService
                .getAllMember(searchBy,gender,status,sortBy, sortDirection,pageNo,pageSize);
        return ResponseEntity.status(HttpStatus.OK).body(response);
        // if successfully operation done returns response with OK http status
    }

    /*
     * this method opens a new endpoint so the
     * auth service  can send a request here
     * to delete a specific member
     * it took a Valid member id(@RequestParam) parameter to
     * delete member from the database
     */
    @DeleteMapping("admin/delete")
    public ResponseEntity<GenericResponse> deleteMemberById(@RequestParam String id){
        // set response to send as response
        String  response = memberManagementService.deleteMemberById(id);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new GenericResponse(response));
        // if successfully operation done returns response with OK http status
    }
}
