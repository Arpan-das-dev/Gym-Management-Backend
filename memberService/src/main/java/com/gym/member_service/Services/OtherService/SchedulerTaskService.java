package com.gym.member_service.Services.OtherService;

import com.gym.member_service.Model.*;
import com.gym.member_service.Repositories.*;
import com.gym.member_service.Utils.LogExecutionTime;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Scheduled task service for automated system maintenance and member notifications.
 *
 * <p>This service provides automated background processing capabilities for:
 * <ul>
 *   <li>Daily plan duration management and account status updates</li>
 *   <li>Automated member notification system for plan alerts and expirations</li>
 *   <li>Weekly analytics and summary computation for BMI and PR (Personal Record) tracking</li>
 *   <li>Real-time session reminders and upcoming training notifications</li>
 * </ul>
 *
 * <p>All scheduled tasks are configured using Spring's {@code @Scheduled} annotation
 * with cron expressions for precise timing control. The service ensures system
 * reliability through automated maintenance operations and enhances member
 * experience through timely notifications and progress tracking.
 *
 * <p>Transactional boundaries are applied where necessary to maintain data
 * consistency across multiple repository operations, particularly during
 * bulk updates and summary computations.
 *
 * <p>The service integrates with external notification services through
 * {@link WebClientServices} to provide asynchronous email and SMS communications.
 *
 * @author Arpan Das
 * @version 1.0
 * @since 1.0
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerTaskService {

    private final MemberRepository memberRepository;
    private final WebClientServices webClientService;
    private final WeightBmiEntryRepository weightBmiEntryRepository;
    private final BmiSummaryRepository bmiSummaryRepository;
    private final PrProgressRepository prProgressRepository;
    private final PrSummaryRepository prRepositorySummary;
    private final SessionRepository sessionRepository;

    /**
     * Daily plan management and account status maintenance task.
     *
     * <p>This scheduled task runs every day at midnight (00:00) and performs
     * critical plan management operations:
     * <ul>
     *   <li>Decrements the remaining plan duration for all active members</li>
     *   <li>Expires plans that have reached zero duration</li>
     *   <li>Freezes member accounts that have exceeded the grace period</li>
     * </ul>
     *
     * <p>The operation is transactional to ensure all plan status changes
     * are committed atomically, preventing inconsistent member account states.
     *
     * <p>Execution statistics are logged to monitor system performance and
     * track the volume of plan management activities.
     *
     * <p><strong>Schedule:</strong> Daily at 00:00 (midnight)
     * <p><strong>Cron Expression:</strong> {@code 0 0 0 * * *}
     *
     * @throws DataAccessException if database operations fail during plan updates
     * @throws TransactionSystemException if the transaction cannot be completed
     *
     * @see MemberRepository#decrementDurationForAllMembers()
     * @see MemberRepository#expirePlan(LocalDateTime)
     * @see MemberRepository#freezeExpiredAccounts()
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void checkAndDecrementPlans() {
        int decrementedMembersPlansCount = memberRepository.decrementDurationForAllMembers();
        int expiredPlanMembersCount = memberRepository.expirePlan(LocalDateTime.now());
        int frozenMembersCount = memberRepository.freezeExpiredAccounts();

        System.out.println("Plans decremented: " + decrementedMembersPlansCount);
        System.out.println("Plans expired: " + expiredPlanMembersCount);
        System.out.println("Accounts frozen: " + frozenMembersCount);
    }
    /**
     * Daily member notification task for plan-related alerts.
     *
     * <p>This scheduled task runs every day at 7:00 AM and sends various
     * notification types to members based on their plan status:
     * <ul>
     *   <li>Plan expiration alerts for members with 3 days remaining</li>
     *   <li>Plan expiration notices for members whose plans expired overnight</li>
     *   <li>Account freeze notifications for members exceeding grace periods</li>
     * </ul>
     *
     * <p>All notifications are sent asynchronously through the WebClientServices
     * to prevent blocking the scheduler thread and ensure reliable delivery.
     *
     * <p>Freeze notifications include additional logic to prevent duplicate
     * notifications and ensure they are only sent at appropriate intervals
     * during the grace period.
     *
     * <p><strong>Schedule:</strong> Daily at 07:00 (7:00 AM)
     * <p><strong>Cron Expression:</strong> {@code 0 0 7 * * *}
     *
     * @throws DataAccessException if member retrieval operations fail
     * @throws WebClientRequestException if notification service calls fail
     *
     * @see WebClientServices#sendAlertMessage(Member)
     * @see WebClientServices#sendExpiredMessage(Member)
     * @see WebClientServices#sendFrozenMessage(Member)
     */
    @Scheduled(cron = "0 0 7 * * *")
    public void sendAlertMails() {
        List<Member> membersListByDurationLeft3 = memberRepository.getMemberListByDuration(3);
        membersListByDurationLeft3.forEach(webClientService::sendAlertMessage);

        List<Member> membersListByDurationLeftZero = memberRepository.getMemberListByDuration(0);
        membersListByDurationLeftZero.forEach(webClientService::sendExpiredMessage);

        List<Member> membersByFrozenAccount = memberRepository.getFrozenMemberList();
        for (Member member : membersByFrozenAccount) {
            if (member.getPlanDurationLeft() == -10 &&
                    member.getPlanExpiration().isAfter(LocalDateTime.now().plusDays(10))) {
                if(!member.getEmail().contains("@example.com")){
                    webClientService.sendFrozenMessage(member);
                }
            }
        }
    }

    /**
     * Weekly analytics computation task for member progress tracking.
     *
     * <p>This scheduled task runs every Sunday at 1:00 AM and computes
     * comprehensive weekly summaries for member analytics:
     * <ul>
     *   <li>BMI summaries including average, minimum, and maximum values</li>
     *   <li>Weight tracking summaries with statistical analysis</li>
     *   <li>Personal Record (PR) progress summaries grouped by workout type</li>
     *   <li>Performance metrics aggregation for dashboard visualizations</li>
     * </ul>
     *
     * <p>The computation covers the previous week's data (7 days) and creates
     * persistent summary records for efficient frontend data retrieval and
     * long-term trend analysis.
     *
     * <p>BMI summaries include comprehensive weight and BMI statistics with
     * entry counts for data quality assessment. PR summaries are grouped by
     * workout name to provide exercise-specific progress tracking.
     *
     * <p>The operation is transactional to ensure all summary data is
     * persisted consistently, preventing partial computation results.
     *
     * <p><strong>Schedule:</strong> Weekly on Sunday at 01:00 AM
     * <p><strong>Cron Expression:</strong> {@code 0 0 1 * * SUN}
     *
     * @throws DataAccessException if summary data persistence fails
     * @throws TransactionSystemException if the transaction cannot be completed
     * @throws IllegalStateException if date calculations result in invalid ranges
     *
     * @see BmiSummary
     * @see PrSummary
     * @see WeightBmiEntry
     * @see PrProgresses
     */
//    @Scheduled(cron = "32 30 * * * *")
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void computeSummary() {
        log.info("schedular task triggered to compute summaries");

        int pageNo = 0;
        int pageSize = 40;
        Pageable pageable = PageRequest.of(pageNo,pageSize);
        LocalDate end = LocalDate.now().minusDays(1);
        LocalDate start = end.withDayOfMonth(1);
        Page<Member> members = memberRepository.findAll(pageable);
        while (!members.isLast()) {
            members.forEach(m-> computeWeightBmi(m,start,end));
            pageNo ++;
            pageable = PageRequest.of(pageNo, pageSize);
            members = memberRepository.findAll(pageable);
        }
    }

    @LogExecutionTime
    @Async("computeSummaries")
    @Transactional
    public void computePr(Member member, LocalDate startDate, LocalDate endDate) {
        log.info("compute pr summary started for memberId={} for period {} to {}",
                member.getId(), startDate, endDate);

        List<PrProgresses> progresses = prProgressRepository
                .findAllByMemberIdAndWeek(member.getId(), startDate, endDate);

        if (progresses.isEmpty()) {
            log.info("No PR progress data found for memberId={} in the given period.", member.getId());
            return;
        }

        Map<String, List<PrProgresses>> groupedWorkouts =
                progresses.stream().collect(Collectors.groupingBy(PrProgresses::getWorkoutName));

        List<String> workoutNamesToSummarize = new ArrayList<>(groupedWorkouts.keySet());
        int summaryYear = endDate.getYear();
        int summaryMonth = endDate.getMonthValue(); // Use the end date to define the month for the summary


        log.debug("Batch lookup for existing PrSummary records for memberId={} in {}-{}", member.getId(), summaryYear, summaryMonth);
        List<PrSummary> existingSummaries = prRepositorySummary
                .findAllByMemberAndYearAndMonthAndWorkoutNameIn(member, summaryYear, summaryMonth, workoutNamesToSummarize);

        Map<String, PrSummary> existingSummaryMap = existingSummaries.stream()
                .collect(Collectors.toMap(PrSummary::getWorkoutName, summary -> summary));

        List<PrSummary> summariesToSave = new ArrayList<>();


        for (Map.Entry<String, List<PrProgresses>> entry : groupedWorkouts.entrySet()) {
            String workoutName = entry.getKey();
            List<PrProgresses> prs = entry.getValue();

            PrSummary summary = existingSummaryMap.getOrDefault(workoutName, new PrSummary());

            double avgWeight = prs.stream().mapToDouble(PrProgresses::getWeight).average().orElse(0);
            double maxWeight = prs.stream().mapToDouble(PrProgresses::getWeight).max().orElse(0);

            int avgReps = (int) prs.stream().mapToDouble(PrProgresses::getRepetitions).average().orElse(0);
            int maxReps = (int) prs.stream().mapToDouble(PrProgresses::getRepetitions).max().orElse(0);

            // Set identifying keys (essential if the summary is new)
            summary.setMember(member);
            summary.setYear(summaryYear);
            summary.setMonth(summaryMonth);
            summary.setWorkoutName(workoutName);

            // Set calculated stats
            summary.setAvgWeight(avgWeight);
            summary.setMaxWeight(maxWeight);
            summary.setAvgReps(avgReps);
            summary.setMaxReps(maxReps);
            summary.setEntryCount(prs.size());

            summariesToSave.add(summary);
        }

        // --- 5. Batch Save (DB-Friendly) ---
        prRepositorySummary.saveAll(summariesToSave);
        log.info("Finished computing and batch-persisting {} workout summaries for member {}.", summariesToSave.size(), member.getId());
    }

    @Transactional
    @LogExecutionTime
    @Async("computeSummaries")
    public void computeWeightBmi(Member member, LocalDate startDate, LocalDate endDate) {
        log.info("computeWeightBmi started for memberId={} for period {} to {}",
                member.getId(), startDate, endDate);

        int summaryYear = endDate.getYear();
        int summaryMonth = endDate.getMonthValue();

        // --- 1. Fetch Raw Data ---
        List<WeightBmiEntry> bmiEntries = weightBmiEntryRepository
                .findAllByMemberIdAndDateBetween(member.getId(), startDate, endDate);

        log.debug("Fetched {} BMI entries for memberId={} between {} and {}",
                bmiEntries.size(), member.getId(), startDate, endDate);

        if (bmiEntries.isEmpty()) {
            log.warn("No BMI entries found to compute summary for memberId={} between {} and {}",
                    member.getId(), startDate, endDate);
            log.info("computeWeightBmi completed for memberId={}", member.getId());
            return;
        }

        // --- 2. Calculate Aggregated Statistics ---
        double avgBmi = bmiEntries.stream().mapToDouble(WeightBmiEntry::getBmi).average().orElse(0);
        double minBmi = bmiEntries.stream().mapToDouble(WeightBmiEntry::getBmi).min().orElse(0);
        double maxBmi = bmiEntries.stream().mapToDouble(WeightBmiEntry::getBmi).max().orElse(0);

        double avgWeight = bmiEntries.stream().mapToDouble(WeightBmiEntry::getWeight).average().orElse(0);
        double minWeight = bmiEntries.stream().mapToDouble(WeightBmiEntry::getWeight).min().orElse(0);
        double maxWeight = bmiEntries.stream().mapToDouble(WeightBmiEntry::getWeight).max().orElse(0);

        log.info("Computed BMI summary for memberId={}: avgBmi={}, minBmi={}, maxBmi={}, avgWeight={}, minWeight={}, maxWeight={}, entryCount={}",
                member.getId(), avgBmi, minBmi, maxBmi, avgWeight, minWeight, maxWeight, bmiEntries.size());

        // --- 3. Single Lookup for Existing Summary ---
        // Uses the unique keys (member, year, month) to find the existing record.
        Optional<BmiSummary> existing = bmiSummaryRepository
                .findByMemberAndYearAndMonth(member, summaryYear, summaryMonth);

        // --- 4. Upsert/Merge Logic ---
        // If present, uses the existing entity (UPDATE); otherwise, creates a new one (INSERT).
        BmiSummary summary = existing.orElseGet(BmiSummary::new);

        // Set identifying keys (Crucial if the summary is new)
        summary.setMember(member);
        summary.setYear(summaryYear);
        summary.setMonth(summaryMonth);

        // Set calculated stats
        summary.setAvgBmi(avgBmi);
        summary.setMinBmi(minBmi);
        summary.setMaxBmi(maxBmi);
        summary.setAvgWeight(avgWeight);
        summary.setMinWeight(minWeight);
        summary.setMaxWeight(maxWeight);
        summary.setEntryCount(bmiEntries.size());

        // --- 5. Save (Handles both Update and Insert) ---
        bmiSummaryRepository.save(summary);

        log.info("{} BmiSummary for memberId={} month={}",
                existing.isPresent() ? "Updated existing" : "Created new", member.getId(), summaryMonth);

        log.info("computeWeightBmi completed for memberId={} done by {}", member.getId(),Thread.currentThread().getName());
    }



    /**
     * Real-time session reminder notification task.
     *
     * <p>This scheduled task runs every 30 minutes and identifies training
     * sessions that are scheduled to start within the next 30 minutes.
     * It provides advance notification to both members and trainers about
     * upcoming sessions to improve attendance and preparation.
     *
     * <p>The task uses a 30-minute lookahead window to identify upcoming
     * sessions and logs session details for monitoring and debugging purposes.
     * Future enhancements will include SMS/email/push notification delivery
     * to provide multichannel session reminders.
     *
     * <p>The frequent execution interval ensures timely notifications while
     * balancing system resource usage and notification relevance.
     *
     * <p><strong>Schedule:</strong> Every 30 minutes
     * <p><strong>Fixed Rate:</strong> {@code 30 * 60 * 1000} milliseconds (1800000ms)
     *
     * @throws DataAccessException if session retrieval operations fail
     * @throws IllegalArgumentException if time threshold calculations fail
     *
     * @see Session
     * @see SessionRepository#findSessionsStartingSoon(LocalDateTime, LocalDateTime)
     */
    @Scheduled(fixedRate = 30 * 60 * 1000) // every 30 mins
    public void sendNotificationForUpcomingSessions(){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusMinutes(30);

        List<Session> sessions = sessionRepository.findSessionsStartingSoon(now, threshold);

        for (Session session : sessions) {
            log.info("Upcoming session for member {} with trainer {} at {}",
                    session.getMemberId(),
                    session.getTrainerId(),
                    session.getSessionStartTime());
            if(session.getSessionStartTime().minusMinutes(30).equals(threshold)) {
                // TODO: send SMS/email/notification here
            }
        }
    }


    // scheduler service to expire plans
    @Scheduled(cron = "0 0 0 * * *")
    public void sendToPlanService(){
        LocalDateTime now = LocalDateTime.now();
        List<Member> memberList = memberRepository.findAllByExpiredPlans(now);
        for (Member m : memberList) {
            log.info("sending to plan service of member {}",m.getFirstName()+" "+m.getLastName());
            try {
                String  response =  webClientService.sendPlanServiceToDecrementMembersCount(m.getPlanID()).get();
                System.out.println(response);
                log.info("response sent");
            } catch (InterruptedException | ExecutionException e) {
                log.warn(e.getCause().getLocalizedMessage());
                throw new RuntimeException(e);
            }
        }
    }
}
