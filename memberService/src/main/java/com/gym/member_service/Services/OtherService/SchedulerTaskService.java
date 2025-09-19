package com.gym.member_service.Services.OtherService;

import com.gym.member_service.Model.*;
import com.gym.member_service.Repositories.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SchedulerTaskService {

    private final MemberRepository memberRepository;
    private final WebClientServices webClientService;
    private final WeightBmiEntryRepository weightBmiEntryRepository;
    private final BmiSummaryRepository bmiSummaryRepository;
    private final PrProgressRepository prProgressRepository;
    private final PrSummaryRepository prRepositorySummary;

    /**
     * Scheduler Task #1
     * <p>
     * Runs daily at midnight (00:00).
     * Responsibilities:
     * <ul>
     *   <li>Decrements remaining plan duration for all members</li>
     *   <li>Expires plans that reached 0 duration</li>
     *   <li>Freezes accounts whose plan expired beyond the grace period</li>
     * </ul>
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
     * Scheduler Task #2
     * <p>
     * Runs daily at 7:00 AM.
     * Responsibilities:
     * <ul>
     *   <li>Send alerts to members with only 3 days left in their plan</li>
     *   <li>Send expiration notices to members whose plans expired last night</li>
     *   <li>Send freeze notifications to members whose accounts are frozen</li>
     * </ul>
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
                webClientService.sendFrozenMessage(member);
            }
        }
    }

    /**
     * Scheduler Task #3
     * <p>
     * Runs weekly on Sunday at 01:00 AM.
     * Responsibilities:
     * <ul>
     *   <li>Compute weekly BMI summaries for each member</li>
     *   <li>Compute weekly PR (Personal Record) progress summaries</li>
     *   <li>Persist summary data for analytics and frontend visualizations</li>
     * </ul>
     */
    @Scheduled(cron = "0 0 1 * * SUN")
    @Transactional
    public void computeSummary() {
        List<Member> allMembers = memberRepository.findAll();
        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now().minusDays(6);

        for (Member member : allMembers) {
            int year = startDate.getYear();
            int month = startDate.getMonthValue();

            // Compute BMI summary
            List<WeightBmiEntry> bmiEntries = weightBmiEntryRepository
                    .findAllByMemberIdAndWeek(member.getId(), endDate, startDate);
            if (!bmiEntries.isEmpty()) {
                double avgBmi = bmiEntries.stream().mapToDouble(WeightBmiEntry::getBmi).average().orElse(0);
                double minBmi = bmiEntries.stream().mapToDouble(WeightBmiEntry::getBmi).min().orElse(0);
                double maxBmi = bmiEntries.stream().mapToDouble(WeightBmiEntry::getBmi).max().orElse(0);

                double avgWeight = bmiEntries.stream().mapToDouble(WeightBmiEntry::getWeight).average().orElse(0);
                double minWeight = bmiEntries.stream().mapToDouble(WeightBmiEntry::getWeight).min().orElse(0);
                double maxWeight = bmiEntries.stream().mapToDouble(WeightBmiEntry::getWeight).max().orElse(0);

                BmiSummary summary = BmiSummary.builder()
                        .member(member)
                        .year(year).month(month)
                        .avgBmi(avgBmi).minBmi(minBmi).maxBmi(maxBmi)
                        .avgWeight(avgWeight).minWeight(minWeight).maxWeight(maxWeight)
                        .entryCount(bmiEntries.size())
                        .build();
                bmiSummaryRepository.save(summary);
            }

            // Compute PR summary
            List<PrProgresses> progresses = prProgressRepository
                    .findAllByMemberIdAndWeek(member.getId(), startDate, endDate);
            Map<String, List<PrProgresses>> groupedWorkouts =
                    progresses.stream().collect(Collectors.groupingBy(PrProgresses::getWorkoutName));

            for (Map.Entry<String, List<PrProgresses>> entry : groupedWorkouts.entrySet()) {
                String workoutName = entry.getKey();
                List<PrProgresses> prs = entry.getValue();

                double avgWeight = prs.stream().mapToDouble(PrProgresses::getWeight).average().orElse(0);
                double maxWeight = prs.stream().mapToDouble(PrProgresses::getWeight).max().orElse(0);

                int avgReps = (int) prs.stream().mapToDouble(PrProgresses::getRepetitions).average().orElse(0);
                int maxReps = (int) prs.stream().mapToDouble(PrProgresses::getRepetitions).max().orElse(0);

                PrSummary summary = PrSummary.builder()
                        .year(year).month(month)
                        .workoutName(workoutName)
                        .avgWeight(avgWeight).avgReps(avgReps)
                        .maxWeight(maxWeight).maxReps(maxReps)
                        .entryCount(prs.size())
                        .build();
                prRepositorySummary.save(summary);
            }
        }
    }
}
