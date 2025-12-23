package com.gym.planService.Controllers;

import com.gym.planService.Dtos.OrderDtos.Responses.MonthlyRevenueResponseDto;
import com.gym.planService.Dtos.PlanDtos.Responses.GenericResponse;
import com.gym.planService.Dtos.PlanDtos.Responses.MostPopularPlanIds;
import com.gym.planService.Dtos.PlanDtos.Responses.TotalUserResponseDto;
import com.gym.planService.Dtos.PlanDtos.Wrappers.AllMonthlyRevenueWrapperResponseDto;
import com.gym.planService.Services.PlanServices.PlanMatrixService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("${payment-service.Base_Url.Matrices}")
@RequiredArgsConstructor
public class MatricesDetailsController {

    private final PlanMatrixService matrixService;

    /**
     * Fetch active user count for a specific plan.
     */
    @GetMapping("/admin/activeUser")
    public ResponseEntity<GenericResponse> getActivePlanUserCount(@Valid @RequestParam String planId) {
        log.info("API :: [GET] /admin/activeUser | Request received to fetch active user count for Plan ID: {}", planId);

        String message = matrixService.getActiveUsersCount(planId);

        log.info("SERVICE :: Successfully fetched active user count for Plan ID: {}", planId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new GenericResponse(message));
    }

    /**
     * Fetch most popular plan(s) based on member count.
     */
    @GetMapping("/all/mostPopular")
    public ResponseEntity<MostPopularPlanIds> getMostPopularPlan() {
        log.info("API :: [GET] /all/mostPopular | Request received to fetch most popular plan(s)");

        MostPopularPlanIds popularPlanIds = matrixService.getMostPopularPlan();

        log.info("SERVICE :: Most popular plan(s) retrieved successfully: {}", popularPlanIds.getPlanIds().size());
        return ResponseEntity.status(HttpStatus.OK).body(popularPlanIds);
    }

    /**
     * Fetch total number of users across all plans.
     */
    @GetMapping("/admin/totalUsers")
    public ResponseEntity<TotalUserResponseDto> getTotalUsersForAllPlans() {
        log.info("API :: [GET] /admin/totalUsers | Request received to fetch total plan user count");

        TotalUserResponseDto response = matrixService.getTotalPlanUsers();

        log.info("SERVICE :: Total active users across all plans: {}", response);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Fetch revenue for the current month with percentage change from last month.
     */
    @GetMapping("/admin/monthlyRevenue")
    public ResponseEntity<MonthlyRevenueResponseDto> getMonthlyRevenue() {
        log.info("API :: [GET] /admin/monthlyRevenue | Request received to fetch monthly revenue summary");

        MonthlyRevenueResponseDto response = matrixService.getRevenue();

        log.info("SERVICE :: Monthly revenue retrieved successfully | Current Month Revenue: {} | Change: {}%",
                response.getCurrentMonthRevenue(), response.getChangeInPercentage());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Fetch paginated revenue per month details.
     */
    @GetMapping("/admin/revenuePerMonth/{year}")
    public ResponseEntity<AllMonthlyRevenueWrapperResponseDto> getRevenuePerMonth(
            @PathVariable @Positive (message = "Year Can not be Negative or Zero")  int year
    ) {
        log.info("API :: [GET] /admin/revenuePerMonth | Request received for year {}",year);
        AllMonthlyRevenueWrapperResponseDto response = matrixService.getAllRevenuePerPerMonth(year);

        log.info("SERVICE :: Paginated monthly revenue report generated successfully with {} entries",
                response.getReviewResponseDtoList().size());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
