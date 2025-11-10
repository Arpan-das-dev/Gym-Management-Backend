package com.gym.planService.Controllers;

import com.gym.planService.Dtos.CuponDtos.Requests.CreateCuponCodeRequestDto;
import com.gym.planService.Dtos.CuponDtos.Requests.UpdateCuponRequestDto;
import com.gym.planService.Dtos.CuponDtos.Responses.CuponCodeResponseDto;
import com.gym.planService.Dtos.CuponDtos.Responses.CuponValidationResponseDto;
import com.gym.planService.Dtos.CuponDtos.Wrappers.AllCuponCodeWrapperResponseDto;
import com.gym.planService.Services.PlanServices.CuponCodeManagementService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing coupon codes linked with plans.
 * <p>
 * This REST controller provides administrative endpoints to create, update, fetch, delete,
 * and validate coupon codes available for various plans.
 * <p>
 * Each API logs important request details for audit and debugging purposes.
 * <p>
 * Usage requires a valid plan ID or coupon code as request parameters alongside necessary request bodies where applicable.
 * <p>
 * Example usage includes launching new coupons, updating existing coupon details,
 * fetching all coupons tied to a specific plan, deleting coupons, and validating coupon codes.
 * </p>
 *
 * @author Arpan Das
 * @version 1.0
 * @since 2025-10-19
 */
@Slf4j
@RestController
@RequestMapping("${plan-service.Base_Url.cuponManagement}")
@RequiredArgsConstructor
public class CuponCodeManagementController {

    private final CuponCodeManagementService cuponService;

    /**
     * Creates and launches a new coupon code for a specified plan.
     *
     * @param planId     Plan identifier to associate the new coupon code with; must be non-blank.
     * @param requestDto Coupon creation details including coupon code string, validity date, and discount percentage; validated.
     * @return ResponseEntity containing all coupon codes for the plan after addition, status 201 Created.
     */
    @PostMapping("/admin/addCupon")
    public ResponseEntity<AllCuponCodeWrapperResponseDto> launchCuponCode(
            @RequestParam @Valid @NotBlank(message = "Plan ID must be provided.") String planId,
            @Valid @RequestBody CreateCuponCodeRequestDto requestDto) {

        log.info("API :: [POST] /admin/addCupon | Launching coupon '{}' for plan '{}' valid till {}",
                requestDto.getCuponCode(), planId, requestDto.getValidity());

        AllCuponCodeWrapperResponseDto response = cuponService.createCuponCode(planId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Updates an existing coupon code's details.
     *
     * @param cuponCode  Coupon code string to update; must be non-blank.
     * @param requestDto Coupon update details including validity and discount percentage; validated.
     * @return ResponseEntity containing the updated coupon code details, status 202 Accepted.
     */
    @PutMapping("/admin/updateCupon")
    public ResponseEntity<CuponCodeResponseDto> updateCupon(
            @RequestParam @Valid @NotBlank(message = "Coupon code must be provided.") String cuponCode,
            @Valid @RequestBody UpdateCuponRequestDto requestDto) {

        log.info("API :: [PUT] /admin/updateCupon | Updating coupon '{}'", cuponCode);
        CuponCodeResponseDto response = cuponService.updateCupon(cuponCode, requestDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * Retrieves all coupon codes associated with a specific plan.
     *
     * @param planId Plan identifier whose coupons are to be fetched; must be non-blank.
     * @return ResponseEntity containing all coupons for the plan, status 200 OK.
     */
    @GetMapping("/admin/getCuponCodes")
    public ResponseEntity<AllCuponCodeWrapperResponseDto> getAllCuponCodes(
            @RequestParam @Valid @NotBlank(message = "Plan ID must be provided.") String planId) {

        log.info("API :: [GET] /admin/getCuponCodes | Fetching all coupons for plan '{}'", planId);
        AllCuponCodeWrapperResponseDto response = cuponService.getCuponCodesByPlanId(planId);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a specified coupon code from a plan.
     *
     * @param cuponCode Coupon code to delete; must be non-blank.
     * @param planId    Plan identifier from which the coupon will be removed; must be non-blank.
     * @return ResponseEntity containing confirmation message, status 202 Accepted.
     */
    @DeleteMapping("/admin/deleteCuponCode")
    public ResponseEntity<String> deleteCuponCode(
            @RequestParam @Valid @NotBlank(message = "Coupon code must be provided.") String cuponCode,
            @RequestParam @Valid @NotBlank(message = "Plan ID must be provided.") String planId) {

        log.info("API :: [DELETE] /admin/deleteCuponCode | Deleting coupon '{}' for plan '{}'", cuponCode, planId);
        String result = cuponService.deleteCuponByCuponCode(cuponCode, planId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(result);
    }

    /**
     * Validates whether a given coupon code is currently active and valid.
     *
     * @param cuponCode Coupon code to validate; must be non-blank.
     * @return ResponseEntity containing true if valid, false otherwise;
     *         returns HTTP 302 Found if valid, 404 Not Found if invalid.
     */
    @PostMapping("/all/validateCuponCode")
    public ResponseEntity<CuponValidationResponseDto> validateCuponCode(
            @RequestParam @Valid @NotBlank(message = "Coupon code must be provided.") String cuponCode) {

        log.info("API :: [POST] /all/validateCuponCode | Validating coupon '{}'", cuponCode);
        CuponValidationResponseDto response = cuponService.validateCupon(cuponCode);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
