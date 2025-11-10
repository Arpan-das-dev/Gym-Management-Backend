package com.gym.planService.Services.PlanServices;

import com.gym.planService.Dtos.OrderDtos.Responses.MonthlyRevenueResponseDto;
import com.gym.planService.Dtos.PlanDtos.Responses.MonthlyReviewResponseDto;
import com.gym.planService.Dtos.PlanDtos.Wrappers.AllMonthlyRevenueWrapperResponseDto;
import com.gym.planService.Exception.Custom.PlanNotFoundException;
import com.gym.planService.Models.MonthlyRevenue;
import com.gym.planService.Models.Plan;
import com.gym.planService.Repositories.PlanPaymentRepository;
import com.gym.planService.Repositories.PlanRepository;
import com.gym.planService.Repositories.RevenueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanMatrixService {

    private final PlanRepository planRepository;
    private final PlanPaymentRepository paymentRepository;
    private final RevenueRepository revenueRepository;

    public String getActiveUsersCount(String planId){
        Plan plan = planRepository.findById(planId)
                .orElseThrow(()-> new PlanNotFoundException("No plan Found with the id::"+planId));
        log.warn("No plan found with this id::{}",planId);
        return plan.getMembersCount().toString();
    }

    public List<String> getMostPopularPlan() {
        List<Plan> plans = planRepository.findMostPopularPlans();
        log.info("fetched {} plans from db",plans.size());
        return plans.stream().map(Plan::getPlanId).toList();
    }

    public Integer getTotalPlanUsers(){
        log.info("SERVICE :: Fetching total plan users across all plans");

        Integer totalUsers = planRepository.findTotalUsers();

        if (totalUsers == null) {
            log.warn("No plans found or no users enrolled yet");
            return 0;
        }

        log.info("Total users enrolled in all plans: {}", totalUsers);
        return totalUsers;
    }

    public MonthlyRevenueResponseDto getRevenue(){
        log.info("SERVICE :: Calculating monthly revenue and growth percentage");

        // Get current and previous months
        YearMonth currentMonth = YearMonth.now();
        YearMonth previousMonth = currentMonth.minusMonths(1);

        // Fetch total successful revenue for each
        Double currentRevenue = paymentRepository.sumRevenueByMonthAndYear(
                currentMonth.getMonth().toString(),
                currentMonth.getYear()
        ).orElse(0.0);

        Double previousRevenue = paymentRepository.sumRevenueByMonthAndYear(
                previousMonth.getMonth().toString(),
                previousMonth.getYear()
        ).orElse(0.0);

        // Calculate % change
        double changePercentage = 0.0;
        if (previousRevenue > 0) {
            changePercentage = ((currentRevenue - previousRevenue) / previousRevenue) * 100;
        }

        log.info("Revenue Summary => Current: {} | Previous: {} | Change: {}%",
                currentRevenue, previousRevenue, changePercentage);

        return MonthlyRevenueResponseDto.builder()
                .currentMonthReview(currentRevenue.intValue())
                .changeInPercentage(Math.round(changePercentage * 100.0) / 100.0)
                .build();
    }

    @Cacheable(value = "allRevenue",key = "revenue':'#pageSize':'#pageNo")
    public AllMonthlyRevenueWrapperResponseDto getAllReviewPerPerMonth(int pageSize, int pageNo){
        log.info("📊 Fetching paginated monthly revenue data | pageNo={} | pageSize={}", pageNo, pageSize);

        Pageable pageable = PageRequest.of(pageNo, pageSize);
        List<MonthlyRevenue> revenuesList = revenueRepository.findPaginatedData(pageable);

        if (revenuesList.isEmpty()) {
            log.warn("⚠️ No monthly revenue records found for given pagination params.");
            return new AllMonthlyRevenueWrapperResponseDto(Collections.emptyList());
        }

        log.info("✅ Retrieved {} monthly revenue records from DB.", revenuesList.size());

        List<MonthlyReviewResponseDto> dtoList = new ArrayList<>();
        double previousRevenue = 0.0;

        for (MonthlyRevenue revenue : revenuesList) {
            double change = 0.0;
            if (previousRevenue != 0.0) {
                change = ((revenue.getMonthlyRevenue() - previousRevenue) / previousRevenue) * 100;
            }

            dtoList.add(new MonthlyReviewResponseDto(
                    revenue.getCurrentMonth(),
                    revenue.getMonthlyRevenue(),
                    Math.round(change * 100.0) / 100.0 // round to 2 decimals
            ));

            previousRevenue = revenue.getMonthlyRevenue();
            log.debug("🧾 Month: {} | Revenue: {} | Change: {}%",
                    revenue.getCurrentMonth(), revenue.getMonthlyRevenue(), change);
        }

        AllMonthlyRevenueWrapperResponseDto response = new AllMonthlyRevenueWrapperResponseDto(dtoList);
        log.info("📦 Successfully built AllMonthlyRevenueWrapper with {} records.", dtoList.size());

        return response;
    }
}
