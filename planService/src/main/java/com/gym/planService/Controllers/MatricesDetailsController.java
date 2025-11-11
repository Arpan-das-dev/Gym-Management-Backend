package com.gym.planService.Controllers;

import com.gym.planService.Dtos.OrderDtos.Responses.MonthlyRevenueResponseDto;
import com.gym.planService.Dtos.PlanDtos.Responses.GenericResponse;
import com.gym.planService.Dtos.PlanDtos.Responses.TotalUserResponseDto;
import com.gym.planService.Dtos.PlanDtos.Wrappers.AllMonthlyRevenueWrapperResponseDto;
import com.gym.planService.Services.PlanServices.PlanMatrixService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<GenericResponse> getMostPopularPlan() {
        log.info("API :: [GET] /all/mostPopular | Request received to fetch most popular plan(s)");

        List<String> message = matrixService.getMostPopularPlan();
        String info = message.toString();

        log.info("SERVICE :: Most popular plan(s) retrieved successfully: {}", info);
        return ResponseEntity.status(HttpStatus.OK).body(new GenericResponse(info));
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
                response.getCurrentMonthReview(), response.getChangeInPercentage());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Fetch paginated revenue per month details.
     */
    @GetMapping("/admin/revenuePerMonth")
    public ResponseEntity<AllMonthlyRevenueWrapperResponseDto> getRevenuePerMonth(
            @Positive @Valid @RequestParam int pageSize,
            @Positive @Valid @RequestParam int pageNo) {

        log.info("API :: [GET] /admin/revenuePerMonth | Request received with pagination params | pageNo={} | pageSize={}", pageNo, pageSize);

        AllMonthlyRevenueWrapperResponseDto response = matrixService.getAllReviewPerPerMonth(pageSize, pageNo);

        log.info("SERVICE :: Paginated monthly revenue report generated successfully with {} entries",
                response.getReviewResponseDtoList().size());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
