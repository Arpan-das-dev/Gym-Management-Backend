package com.gym.member_service.Controllers;

import com.gym.member_service.Dto.MemberFitDtos.Requests.MemberWeighBmiEntryRequestDto;
import com.gym.member_service.Dto.MemberFitDtos.Requests.PrProgressRequestDto;
import com.gym.member_service.Dto.MemberFitDtos.Wrappers.MemberBmiResponseWrapperDto;
import com.gym.member_service.Dto.MemberFitDtos.Responses.MemberWeighBmiEntryResponseDto;
import com.gym.member_service.Dto.MemberFitDtos.Responses.MemberPrProgressResponseDto;
import com.gym.member_service.Dto.MemberFitDtos.Wrappers.MemberPrProgressWrapperDto;
import com.gym.member_service.Services.FitnessServices.MemberFitService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.PastOrPresent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${member-service.Base_Url.Fit}")
@Validated
/*
 * This controller is responsible for interaction with database
 * using MemberFitService Class
 * @RequestMapping: map this controller on specific url
 * which is defined in application.properties
 */
public class MemberFitController {

    private final MemberFitService fitService;

    /*
     * This method is responsible to add a new weight and bmi with date
     * using logic present in MemberFitService
     * and take memberId(@RequestParam) and a request dto(@RequestBody)
     * as method parameter
     */
    @PostMapping("/weight-bmi-entry")
    ResponseEntity<MemberWeighBmiEntryResponseDto> addWeightBmi(@RequestParam String memberId,
                                                                @Valid @RequestBody MemberWeighBmiEntryRequestDto
                                                                        requestDto)
    {   // set the response using method present in MemberFitService
        MemberWeighBmiEntryResponseDto responseDto = fitService.addWeighBmiEntry(memberId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        // if successfully done then returns CREATED as http status
    }

    /*
     * This method is responsible to get all weight and bmi  date
     * using logic present in MemberFitService
     * and take memberId(@RequestParam) and number of days(@RequestParam)
     * as method parameter
     */
    @GetMapping("/weightBmi")
    ResponseEntity<MemberBmiResponseWrapperDto> getAllBmiListById(@RequestParam String memberId,
                                                                  @RequestParam int days) {
        // set the response using method present in MemberFitService
        MemberBmiResponseWrapperDto responseDtoList = fitService.getAllBmiEntry(memberId, days);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(responseDtoList);
        // if successfully retrieves data from DB then returns ACCEPTED as http status
    }

    /*
     * This method is responsible to delete weight and bmi data
     * from a certain date
     * using logic present in MemberFitService
     * and take memberId(@RequestParam) and date(@RequestParam)
     * as method parameter
     */
    @DeleteMapping("/weightBmi")
    ResponseEntity<String> deleteByIdDate(@RequestParam String memberId,
                                          @RequestParam
                                          @FutureOrPresent(message = "Can not delete a pr from future")
                                          LocalDate date)
    {
       // set the response using method present in MemberFitService
       String response = fitService.deleteByDateAndId(memberId,date);
       return ResponseEntity.status(HttpStatus.OK).body(response);
       // if successfully deletes data in DB then returns OK as http status
    }

    /*
     * This method is responsible to add a new Pr with date
     * using logic present in MemberFitService
     * and take memberId(@RequestParam) and a request dto(@RequestBody)
     * as method parameter
     */
    @PostMapping("/addPr")
    ResponseEntity<List<MemberPrProgressResponseDto>> addNewPr(@RequestParam String memberId,
                                                               @Valid @RequestBody List<PrProgressRequestDto>
                                                                       requestDto)
    {
        // set the response using method present in MemberFitService
        List<MemberPrProgressResponseDto> responseDtoList = fitService.addANewPr(memberId,requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDtoList);
        // if successfully adds data in DB then returns CREATED as http status
    }

    /*
     * This method is responsible get Pr with date etc.
     * using logic present in MemberFitService
     * and take memberId(@RequestParam) and no of days(@RequestParam)
     * as method parameter
     */
    @GetMapping("/pr")
    ResponseEntity<MemberPrProgressWrapperDto> getAllPrProgress(@RequestParam String memberId,
                                                                @RequestParam int days)
    {   // set the response using method present in MemberFitService
        MemberPrProgressWrapperDto response = fitService.getAllPrProgress(memberId,days);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        // if successfully retrieves data from DB then returns ACCEPTED as http status
    }

    /*
     * This method is responsible delete all Pr on a certain date
     * using logic present in MemberFitService
     * and take memberId(@RequestParam) and a date(@RequestParam)
     * as method parameter
     */
    @DeleteMapping("/pr")
    ResponseEntity<String> deletePrByIdDate(@RequestParam String memberId,
                                            @RequestParam
                                            @PastOrPresent(message = "Can not delete a pr from future")
                                            LocalDate date)
    {   // set the response using method present in MemberFitService
        String response = fitService.deleteByIdAndDate(memberId,date);
        return ResponseEntity.status(HttpStatus.OK).body(response);
        // if successfully deletes data in DB then returns OK as http status
    }

    /*
     * This method is responsible delete a Pr on a certain date of certain workout
     * using logic present in MemberFitService
     * and take memberId(@RequestParam) and a date(@RequestParam)
     * as method parameter
     */
    @DeleteMapping("/deletePr/{workoutName}")
    ResponseEntity<String> deleteByWorkoutName(@RequestParam String memberId,
                                               @RequestParam
                                               @PastOrPresent(message = "Can not delete a pr from future")
                                               LocalDate date,
                                               @PathVariable String workoutName)
    {   // set the response using method present in MemberFitService
        String response = fitService.deleteByWorkoutNameWIthMemberIdAndDate(memberId,date,workoutName);
        return ResponseEntity.status(HttpStatus.OK).body(response);
        // if successfully deletes data in DB then returns OK as http status
    }
}
