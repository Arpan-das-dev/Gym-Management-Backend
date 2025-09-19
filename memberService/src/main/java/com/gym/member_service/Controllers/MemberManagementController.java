package com.gym.member_service.Controllers;

import com.gym.member_service.Dto.MemberManagementDto.Requests.FreezeRequestDto;
import com.gym.member_service.Dto.MemberManagementDto.Requests.MemberCreationRequestDto;
import com.gym.member_service.Dto.MemberManagementDto.Responses.AllMemberResponseDto;
import com.gym.member_service.Services.MemberServices.MemberManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<String> createMember (@Valid @RequestBody MemberCreationRequestDto requestDto){
        // set response to return in response
        String  response = memberManagementService.createMember(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
        // If member creates successfully returns CREATED as http status
    }

    /*
     * this method opens a new endpoint so the
     * admin service  can
     * request here to freeze a member account
     * it took a Valid member id(@RequestParam) parameter to freeze a member
     */
    @PostMapping("admin/freeze")
    public ResponseEntity<String> freeze(@Valid @RequestBody FreezeRequestDto requestDto){
        String response =  memberManagementService.freezeOrUnFrozen(requestDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /*
     * this method opens a new endpoint so the
     * auth service  can send a request here
     * to increase or get login Streak
     * it took a Valid member id(@RequestParam) parameter to
     * set the login Streak and returns current login streak
     */
    @GetMapping("streak")
    public ResponseEntity<Integer> getLoginStreak(@RequestParam String id){
        // set response to send as response
        Integer response = memberManagementService.setAndGetLoginStreak(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
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
    public ResponseEntity<List<AllMemberResponseDto>> getAllMembers(){
        // set response to send as response
        List<AllMemberResponseDto> response = memberManagementService.getAllMember();
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
    @DeleteMapping("delete")
    public ResponseEntity<String> deleteMemberById(@Valid @RequestParam String id){
        // set response to send as response
        String  response = memberManagementService.deleteMemberById(id);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        // if successfully operation done returns response with OK http status
    }
}
