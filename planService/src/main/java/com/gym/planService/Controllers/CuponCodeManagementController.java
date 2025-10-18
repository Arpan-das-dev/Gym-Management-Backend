package com.gym.planService.Controllers;

import com.gym.planService.Dtos.CuponDtos.Requests.CreateCuponCodeRequestDto;
import com.gym.planService.Dtos.CuponDtos.Requests.UpdateCuponRequestDto;
import com.gym.planService.Dtos.CuponDtos.Responses.CuponCodeResponseDto;
import com.gym.planService.Dtos.CuponDtos.Wrappers.AllCuponCodeWrapperResponseDto;
import com.gym.planService.Services.CuponCodeManagementService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("${plan-service.Base_Url.cuponManagement}")
@RequiredArgsConstructor
public class CuponCodeManagementController {

    private final CuponCodeManagementService cuponService;

    @PutMapping("/admin/addCupon")
    ResponseEntity<AllCuponCodeWrapperResponseDto> launchCuponCode(
            @RequestParam  @Valid @NotBlank String planId,
            @Valid @RequestBody CreateCuponCodeRequestDto requestDto) {
        log.info("Request received to launch cupon code :: {} valid till {}"
                , requestDto.getCuponCode(), requestDto.getValidity());
        AllCuponCodeWrapperResponseDto response = cuponService.createCuponCode(planId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/admin/updateCupon")
    ResponseEntity<CuponCodeResponseDto> updateCupon (@RequestParam @Valid @NotBlank String cuponCode,
                                                      @Valid @RequestBody UpdateCuponRequestDto requestDto) {
        log.info("Request received to update cupon feature for cupon {} ",cuponCode);
        CuponCodeResponseDto response = cuponService.updateCupon(cuponCode,requestDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/admin/getCuponCodes")
    ResponseEntity<AllCuponCodeWrapperResponseDto> getAllCuponCode(@RequestParam @Valid @NotBlank String planId) {
        log.info("Request received to get cupon codes for plan:: {}",planId);
        AllCuponCodeWrapperResponseDto response = cuponService.getCuponCodesByPlanId(planId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/admin/deleteCuponCode")
    ResponseEntity<String> deleteCuponCode(
            @RequestParam @Valid @NotBlank String cuponCode,
            @RequestParam @Valid @NotBlank String planId) {
        log.info("Request received to delete cupon code:: {}",cuponCode);
        String response = cuponService.deleteCuponByCuponCode(cuponCode,planId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @PostMapping("/all/validateCuponCode")
    ResponseEntity<Boolean> validateCuponCode (@RequestParam @Valid @NotBlank String cuponCode) {
        log.info("Request received to check validity for cupon :: {}",cuponCode);
        Boolean response = cuponService.validateCupon(cuponCode);
        if(response) return ResponseEntity.status(HttpStatus.FOUND).body(true);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
    }
}
