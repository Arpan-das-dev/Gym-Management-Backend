package com.gym.planService.Services.PlanServices;

import com.gym.planService.Dtos.OrderDtos.Responses.MonthlyRevenueResponseDto;
import com.gym.planService.Dtos.OrderDtos.Responses.OldAndNewTransactionResponseDto;
import com.gym.planService.Dtos.PlanDtos.Responses.*;
import com.gym.planService.Dtos.PlanDtos.Wrappers.AllMonthlyRevenueWrapperResponseDto;
import com.gym.planService.Exception.Custom.PlanNotFoundException;
import com.gym.planService.Exception.Custom.RevenueLimitExceededException;
import com.gym.planService.Models.Plan;
import com.gym.planService.Models.PlanPayment;
import com.gym.planService.Repositories.PlanPaymentRepository;
import com.gym.planService.Repositories.PlanRepository;
import com.gym.planService.Repositories.RevenueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanMatrixService {

    private final PlanRepository planRepository;
    private final PlanPaymentRepository paymentRepository;
    private final RevenueRepository revenueRepository;
    private final StringRedisTemplate redisTemplate;
    private final CacheManager manager;

    public String getActiveUsersCount(String planId) {
        log.info("Request received to get users count for plan --> {}", planId);

        String key = "USERS::" + planId;
        String cachedCount = redisTemplate.opsForValue().get(key);
        if (cachedCount != null) {
            log.info("Cache hit for key {}: count is {}", key, cachedCount);
            return cachedCount;
        }

        log.info("Cache miss for key {}. Fetching from database...", key);

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> {
                    log.error("Plan not found in database for ID: {}", planId);
                    return new PlanNotFoundException("No plan found with the id::" + planId);
                });
        String usersCount = String.valueOf(plan.getMembersCount());
        redisTemplate.opsForValue().set(key, usersCount, Duration.ofHours(6));
        log.info("Cache updated for key {} with value {} (TTL: 6 hours)", key, usersCount);
        return usersCount;
    }

    @Cacheable(value = "mostPopular", key = "'popular'")
    public MostPopularPlanIds getMostPopularPlan() {
        List<Plan> plans = planRepository.findMostPopularPlans();
        log.info("fetched {} plans from db",plans.size());
        return MostPopularPlanIds.builder()
                .planIds(plans.stream().map(Plan::getPlanId).toList())
                .build();
    }

    @Cacheable(value = "totalUsers", key = "'totalUsersList'")
    public TotalUserResponseDto getTotalPlanUsers() {
        log.info("SERVICE :: Fetching total plan users across all plans");

        Integer totalUsers = planRepository.findTotalUsers(); 

        String currentMonth = LocalDate.now().getMonth().toString();
        String previousMonth = LocalDate.now().withDayOfMonth(1).minusDays(1).getMonth().toString();

        List<String> months = List.of(currentMonth, previousMonth);
        List<Object[]> results = revenueRepository.findUserCountsByMonths(months);

        Map<String, Integer> monthToUserCount = results.stream()
                .collect(Collectors.toMap(
                        r -> (String) r[0],
                        r -> ((Long) r[1]).intValue() // COUNT returns Long, convert to Integer
                ));

        Integer thisMonthUsers = monthToUserCount.getOrDefault(currentMonth, 0);
        Integer lastMonthUsers = monthToUserCount.getOrDefault(previousMonth, 0);

        if (totalUsers == null) {
            log.warn("No plans found or no users enrolled yet");
            return new TotalUserResponseDto(0, 0.00);
        }

        log.info("Total users enrolled in all plans: {}", totalUsers);

        return new TotalUserResponseDto(thisMonthUsers, changeDetection(thisMonthUsers, lastMonthUsers));
    }

    private Double changeDetection(Integer thisMonthUsers, Integer lastMonthUsers) {
        if (thisMonthUsers == null || thisMonthUsers == 0) {
            return 0.0;
        }
        int change = thisMonthUsers - (lastMonthUsers != null ? lastMonthUsers : 0);
        return ((double) change / thisMonthUsers) * 100;
    }

    @Cacheable(value = "monthlyRevenue", key = "'revenue'")
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
                .currentMonthRevenue(currentRevenue.intValue())
                .changeInPercentage(Math.round(changePercentage * 100.0) / 100.0)
                .build();
    }

    @Cacheable(value = "allRevenue", key = "#year")
    public AllMonthlyRevenueWrapperResponseDto getAllRevenuePerPerMonth(int year){
        log.info("📊 Fetching paginated monthly revenue for year {}", year);
        List<Integer> yearlist = getOldAndNewestTime().getYarList();
        boolean condition = yearlist.contains(year);
        if(!condition) {
          String message = yearlist.getFirst().equals(yearlist.getLast()) ?
          "No Payment Present other than Year "+yearlist.getLast() :
          "No Payment Present For "+year + "Kindly request Between "+yearlist.getFirst()+ " And" + yearlist.getLast();
          throw new RevenueLimitExceededException(message);
        }
        List<Object[]> revenuesList = paymentRepository.findMonthlyRevenueByYear(year);

        if (revenuesList.isEmpty()) {
            log.warn("⚠️ No monthly revenue records found for given pagination params.");
            return new AllMonthlyRevenueWrapperResponseDto(Collections.emptyList());
        }
        log.info("✅ Retrieved {} monthly revenue records from DB.", revenuesList.size());

        List<MonthlyReviewResponseDto> dtoList = new ArrayList<>();
        double previousRevenue = 0.0;

        for (Object[] revenue : revenuesList) {
            double change = 0.0;
            int paymentYear = (int) revenue[0];
            String  paymentMonth = (String) revenue[1];
            double income = (double) revenue[2];
            if (previousRevenue != 0.0) {
                change = ((income - previousRevenue) / previousRevenue) * 100;
            }

            dtoList.add(new MonthlyReviewResponseDto(
                    paymentYear,paymentMonth, income, Math.round(change * 100.0) / 100.0
            ));
            previousRevenue = income;
            log.debug("🧾 Month: {} | Revenue: {} | Change: {}%",
                    paymentMonth, income, change);
        }
        log.info("📦 Successfully built AllMonthlyRevenueWrapper with {} records.", dtoList.size());
        return AllMonthlyRevenueWrapperResponseDto.builder()
                .reviewResponseDtoList(dtoList)
                .build();
    }

    public OldAndNewTransactionResponseDto getOldAndNewestTime() {
        log.info("📆 Fetching available transaction year range");

        int oldest = getCachedYear("PLAN_PAYMENT:OLDEST_YEAR", true);
        int newest = getCachedYear("PLAN_PAYMENT:NEWEST_YEAR", false);

        log.info("📆 Transaction year boundaries resolved | oldest={} | newest={}", oldest, newest);

        List<Integer> yearList = new ArrayList<>();
        for (int y = oldest; y <= newest; y++) {
            yearList.add(y);
        }

        log.debug("📆 Available years list prepared -> {}", yearList);

        return OldAndNewTransactionResponseDto.builder()
                .yarList(yearList)
                .build();
    }


    private int getCachedYear(String key, boolean oldest) {
        log.debug("🧠 Resolving {} transaction year (cache key = {})",
                oldest ? "OLDEST" : "NEWEST", key);

        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            log.info("🧠 Cache HIT for key [{}] -> year={}", key, cached);
            return Integer.parseInt(cached);
        }

        log.warn("🧠 Cache MISS for key [{}]. Querying database…", key);

        PlanPayment payment = oldest
                ? paymentRepository.findOldestTransaction(PageRequest.of(0, 1)).getFirst()
                : paymentRepository.findNewestTransaction(PageRequest.of(0, 1)).getFirst();

        int resolvedYear = payment.getPaymentYear();

        redisTemplate.opsForValue().set(
                key,
                String.valueOf(resolvedYear),
                oldest ? Duration.ofHours(12) : Duration.ofHours(2)
        );

        log.info("🧠 Cached {} year={} with TTL={} hours",
                oldest ? "OLDEST" : "NEWEST",
                resolvedYear,
                oldest ? 12 : 2
        );

        return resolvedYear;
    }

    @Cacheable(value = "revenuePerPlan", key = "'revenuePlan'")
    public RevenueGeneratedPerPlanResponseDto getRevenuePerPlan() {
        Map<String ,PlanLifeTimeIncome> incomeMap = new HashMap<>();
        List<Object[]> lifeTimeRevenue = paymentRepository.findLifetimeIncomePerPlan();
        for (Object[] revenue : lifeTimeRevenue) {
            PlanLifeTimeIncome income = PlanLifeTimeIncome.builder()
                    .revenue((Double) revenue[1])
                    .usage((Integer) revenue[2])
                    .build();
            String planId = (String) revenue[0];
            if(!incomeMap.containsKey(planId)){
                incomeMap.put(planId,income);
            }
        }
        return new RevenueGeneratedPerPlanResponseDto(incomeMap);
    }

    public void updateRevenuePerPlanCache(String planId,Double paidAmount){
        RevenueGeneratedPerPlanResponseDto responseDto = (RevenueGeneratedPerPlanResponseDto) Objects
                .requireNonNull(manager.getCache("revenuePerPlan")).get("revenuePlan");
        if(responseDto!=null) {
            PlanLifeTimeIncome income = responseDto.getAllPlanIncomes().get(planId);
            income.setRevenue(income.getRevenue()+paidAmount);
            income.setUsage(income.getUsage()+1);
            Objects.requireNonNull(manager.getCache("revenuePerPlan")).put("revenuePlan",responseDto);
        } else {
            Objects.requireNonNull(manager.getCache("revenuePerPlan")).evict("revenuePlan");
            getRevenuePerPlan();
        }
    }
}
