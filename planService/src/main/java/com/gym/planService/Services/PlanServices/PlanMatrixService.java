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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.*;
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
        String month = LocalDate.now().getMonth().toString().toUpperCase();
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
            if(month.equalsIgnoreCase(paymentMonth.trim().toUpperCase())) {
                String monthKey = buildMonthKey(month,year);
                redisTemplate.opsForValue().set(monthKey,String.valueOf(income),Duration.ofDays(15));
            }
        }
        Double sum = dtoList.stream().mapToDouble(MonthlyReviewResponseDto::getRevenue).sum();
        redisTemplate.opsForValue().set("YEAR::"+year,String.valueOf(sum),Duration.ofDays(30));
        log.info("📦 Successfully built AllMonthlyRevenueWrapper with {} records.", dtoList.size());
        return AllMonthlyRevenueWrapperResponseDto.builder()
                .reviewResponseDtoList(dtoList)
                .build();
    }

    private String buildMonthKey(String month, int year) {
        return "MONTH::"+month+"::"+year;
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

    @Cacheable(value = "revenuePerPlan", key = "'perPlan'")
    public RevenueGeneratedPerPlanResponseDto getRevenuePerPlan() {
        log.info("®️®️ request received to get revenue per plans");
        Set<String> planIds = redisTemplate.opsForSet().members("REVENUE:PLANS");

        if (planIds == null || planIds.isEmpty()) {
            log.info("line -> 256 No value Stored in redis Calling Db method");
            return fallBackLoadFromDb();
        }
        Map<String, PlanLifeTimeIncome> incomeMap = new HashMap<>();
        log.info("line -> 251 key found in redis fetching from redis");

        int count = 1;
        for (String planId : planIds) {

            log.debug("Processing request no -> {}",count);
            String revenue = redisTemplate.opsForValue().get("REVENUE:PLAN:" + planId);
            String usage = redisTemplate.opsForValue().get("USAGE:PLAN:" + planId);
            String planName = redisTemplate.opsForValue().get("PLAN:NAME:"+planId);
            if (revenue == null || usage == null) {
               log.info("Null Value is Captured");
               asyncRepairPlan(planId);
               continue;
            }

            incomeMap.put(planId, PlanLifeTimeIncome.builder()
                            .revenue(Double.parseDouble(revenue))
                            .usage(Long.parseLong(usage))
                            .planName(planName)
                            .build()
            );
            count ++;
        }
        if (incomeMap.isEmpty()) {
            log.debug("Value in the map is empty calling fallback db load method");
            return fallBackLoadFromDb();
        }
        return new RevenueGeneratedPerPlanResponseDto(incomeMap);
    }

    @Async("defaultTasks")
    public void asyncRepairPlan(String planId) {
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent("LOCK:REPAIR:" + planId, "1", Duration.ofSeconds(30));
        if (!Boolean.TRUE.equals(acquired)) {
            log.warn("A thread is Already busy in repair");
            return;
        }
        try {
            Object[] data = paymentRepository.findLifeTimeIncomeByPlanId(planId);

            if (data == null || data.length < 4
                    || data[2] == null || data[3] == null) {
                return;
            }
            String fetchedPlanId = (String) data[0];
            String planName = (String) data[1];
            Double revenue = (Double) data[2];
            Long usage = (Long) data[3];

            redisTemplate.opsForValue()
                    .set("REVENUE:PLAN:" + fetchedPlanId, revenue.toString(), Duration.ofHours(18));
            redisTemplate.opsForValue()
                    .set("USAGE:PLAN:" + fetchedPlanId, usage.toString(), Duration.ofHours(18));
            redisTemplate.opsForValue()
                    .set("PLAN:NAME:" + fetchedPlanId, planName, Duration.ofHours(18));

            log.debug("Set income={} usage={} plan={} for planId={}",
                    revenue, usage, planName, fetchedPlanId);
        } finally {
            boolean value = redisTemplate.delete("LOCK:REPAIR:" + planId);
            if (value) {
                log.debug("lock released");
            } else {
                log.debug("no lock found");
            }
        }
    }
    private RevenueGeneratedPerPlanResponseDto fallBackLoadFromDb() {
        log.info("calling Db to fetch refreshed data and put in the redis");
        List<Object[]> data = paymentRepository.findLifetimeIncomePerPlan();
        log.info("Fetched {} no of data from db ",data.size());
        Map<String, PlanLifeTimeIncome> map = new HashMap<>();
        int count = 1;
        for (Object[] row : data) {
            log.debug("line-> 269 Processing row no of {} for planId[{}]",count,row[0].toString());
            String planId = (String) row[0];
            String planName = (String) row[1];
            Double revenue = (Double) row[2];
            Long usage = (Long) row[3];
            map.put(planId,
                    PlanLifeTimeIncome.builder()
                            .revenue(revenue)
                            .usage(usage)
                            .planName(planName)
                            .build()
            );
            log.info("line->280 planId->[{}] stored in the map",planId);

            redisTemplate.opsForSet().add("REVENUE:PLANS", planId);
            redisTemplate.opsForValue().set("REVENUE:PLAN:" + planId, revenue.toString(),Duration.ofHours(18));
            redisTemplate.opsForValue().set("USAGE:PLAN:" + planId, usage.toString(),Duration.ofHours(18));
            redisTemplate.opsForValue().set("PLAN:NAME:"+planId,planName,Duration.ofHours(18));

            count++;
        }
        redisTemplate.expire("REVENUE:PLANS", Duration.ofHours(18));
        return new RevenueGeneratedPerPlanResponseDto(map);
    }


    @CacheEvict(value = "revenuePerPlan", key = "'perPlan'")
    public void autoIncrement(String planId, Double paidAmount) {
        log.info("®️®️ received to autoupdate cache for revenue/plans");
        String revenueKey = "REVENUE:PLAN:" + planId;
        String usageKey = "USAGE:PLAN:" + planId;
        boolean exist = redisTemplate.hasKey(revenueKey) && redisTemplate.hasKey(usageKey);
        if(!exist){
            asyncRepairPlan(planId);
            return;
        }
        redisTemplate.opsForSet().add("REVENUE:PLANS", planId);
        redisTemplate.opsForValue().increment("REVENUE:PLAN:" + planId, paidAmount);
        redisTemplate.opsForValue().increment("USAGE:PLAN:" + planId, 1);

    }

    public GenericResponse getLifeTimeIncome() {
        String value = redisTemplate.opsForValue().get("LIFETIME:");
        if(value!= null) {
            return new GenericResponse(value);
        }
        synchronized (this) {
            String data = redisTemplate.opsForValue().get("LIFETIME:");
            if(data!=null) return new GenericResponse(data);
            Double paidPrice = paymentRepository.getLifeTimeIncome();
            double safeTotal = paidPrice == null ? 0.0 : paidPrice;
            redisTemplate.opsForValue().set("LIFETIME:",String.valueOf(paidPrice),Duration.ofDays(30));
            return new GenericResponse(String.valueOf(safeTotal));
        }
    }

    public void autoIncrementLifeTimeIncome(Double paidPrice){
        redisTemplate.opsForValue().setIfAbsent("LIFETIME:", "0");
        redisTemplate.opsForValue().increment("LIFETIME:",paidPrice);
    }

    public QuickStatsResponseDto getSummaryStatsOfIncome(){
        log.debug("®️®️ request received to get quick stats about income");
        LocalDateTime now = LocalDateTime.now();
        int year = now.getYear();
        String month = now.getMonth().toString().toUpperCase();

        String yearKey = "YEAR::" + year;
        String monthKey = buildMonthKey(month, year);
        String dayKey = "DAY::" + now.toLocalDate();

        String yearly = redisTemplate.opsForValue().get(yearKey);
        String monthly = redisTemplate.opsForValue().get(monthKey);

        if(yearly== null || monthly== null){
            log.warn("⚠️ Cache MISS for YEAR or MONTH | year={} | month={}", year, month);
            Boolean acquired = redisTemplate.opsForValue()
                    .setIfAbsent("LOCK:QUICK_STATS","1",Duration.ofSeconds(30));
            if(Boolean.TRUE.equals(acquired)) {
                log.warn("A thread is busy to update the work");

                try {
                    log.info("🛢️ Fetching YEAR & MONTH revenue from DB");

                    Object[] data = paymentRepository.findIncomeByYearAndMonth(year, month);
                    Object[] row = (Object[]) data[0];

                    double safeYearly = row[0] == null ? 0.0 : ((Number) row[0]).doubleValue();
                    double safeMonthly = row[1] == null ? 0.0 : ((Number) row[1]).doubleValue();

                    yearly = String.valueOf(safeYearly);
                    monthly = String.valueOf(safeMonthly);

                    redisTemplate.opsForValue().set(yearKey, yearly);
                    redisTemplate.opsForValue().set(monthKey, monthly);
                    log.info("Quick stats restored from DB | year={} | month={}", safeYearly, safeMonthly);
                }finally {
                    redisTemplate.delete("LOCK:QUICK_STATS");
                }
            }
        } else {
            log.debug("✅ Cache HIT for YEAR & MONTH");
        }
        Double dailyIncome = getTodayIncome(dayKey,now);

        log.info("✅ QUICK STATS READY | Today={} | Month={} | Year={}", dailyIncome, monthly, yearly);

        return QuickStatsResponseDto.builder()
                .yearlyIncome(Double.valueOf(Objects.requireNonNull(yearly)))
                .monthlyIncome(Double.valueOf(Objects.requireNonNull(monthly)))
                .todayIncome(dailyIncome)
                .build();
    }

    private Double getTodayIncome(String dayKey, LocalDateTime now) {
        String todayIncome = redisTemplate.opsForValue().get(dayKey);

        if (todayIncome == null) {
            log.warn("Cache miss for today income | key={}", dayKey);

            Boolean acquired = redisTemplate.opsForValue()
                    .setIfAbsent("LOCK:TODAY_INCOME", "1", Duration.ofSeconds(20));

            if (Boolean.TRUE.equals(acquired)) {
                try {
                    LocalDateTime start = now.toLocalDate().atStartOfDay();
                    LocalDateTime end = now.toLocalDate().atTime(LocalTime.MAX);

                    Double dbValue = paymentRepository.findTodayIncome(start, end);
                    double safeToday = dbValue == null ? 0.0 : dbValue;

                    todayIncome = String.valueOf(safeToday);
                    redisTemplate.opsForValue().set(dayKey, todayIncome, Duration.ofHours(4));

                    log.info("Today income restored from DB | value={}", safeToday);
                } finally {
                    redisTemplate.delete("LOCK:TODAY_INCOME");
                }
            } else {
                log.warn("Another instance rebuilding today income, retrying");
                todayIncome = redisTemplate.opsForValue().get(dayKey);
            }
        } else {
            log.debug("Cache hit for today income");
        }

        return Double.valueOf(Objects.requireNonNull(todayIncome));
    }


    public void autoUpdateQuickStats(LocalDateTime transactionTime,Double paidPrice){
        if (paidPrice == null || paidPrice <= 0) {
            log.warn("Invalid paidPrice received, skipping quick stats update");
            return;
        }
        String month = transactionTime.getMonth().toString().toUpperCase();

        String yearKey = "YEAR::" + transactionTime.getYear();
        String monthKey = buildMonthKey(month, transactionTime.getYear());
        String dayKey = "DAY::" + transactionTime.toLocalDate();

        ensureExistence(monthKey,yearKey,dayKey,transactionTime);

        redisTemplate.opsForValue().increment(yearKey,paidPrice);
        redisTemplate.opsForValue().increment(monthKey, paidPrice);
        redisTemplate.opsForValue().increment(dayKey,paidPrice);

        log.debug("Quick stats incremented | yearKey={} | monthKey={} | dayKey={} | amount={}",
                yearKey, monthKey, dayKey, paidPrice);
    }

    private void ensureExistence(String monthKey, String yearKey, String dayKey,LocalDateTime now) {
        if(!redisTemplate.hasKey(monthKey) || !redisTemplate.hasKey(yearKey)) {
            restoreFromDbSafely(now);
        }
        if(!redisTemplate.hasKey(dayKey)) {
           getTodayIncome(dayKey,now);
        }
    }

    private void restoreFromDbSafely(LocalDateTime now) {

        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent("LOCK:QUICK_STATS", "1", Duration.ofSeconds(30));

        if (!Boolean.TRUE.equals(acquired)) {
            log.warn("⏳ QuickStats baseline restore already in progress");
            return;
        }

        try {
            int year = now.getYear();
            String month = now.getMonth().toString().toUpperCase();

            Object[] data = paymentRepository.findIncomeByYearAndMonth(year, month);
            Object[] row = (Object[]) data[0];

            double yearly = row[0] == null ? 0.0 : ((Number) row[0]).doubleValue();
            double monthly = row[1] == null ? 0.0 : ((Number) row[1]).doubleValue();

            redisTemplate.opsForValue().set("YEAR::" + year, String.valueOf(yearly), Duration.ofDays(30));

            redisTemplate.opsForValue().
                    set(buildMonthKey(month, year),
                    String.valueOf(monthly),
                    Duration.ofDays(15));

            log.info("📦 Restored QuickStats baseline from DB | year={} | month={}", yearly, monthly);

        } finally {
            redisTemplate.delete("LOCK:QUICK_STATS");
        }
    }

}
