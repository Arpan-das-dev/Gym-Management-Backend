package com.gym.member_service.Services;

import com.gym.member_service.Model.Member;
import com.gym.member_service.Repositories.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SchedulerTaskService {

    private final MemberRepository memberRepository;
    private final WebClientServices webClientService;


    /*
     * ==========================> SCHEDULER TASKS #01  <=================================
     *
     *      =================> RUNS EVERY NIGHT AT 12:00 AM <===================
     *
     *       ===============> UPDATE THE DB OF MEMBER DETAILS <================
     */

    @Scheduled(cron = "0 0 0 * * *")
    public void checkAndDecrementPlans(){
        int decrementedMembersPlansCount = memberRepository.decrementDurationForAllMembers();
        int expiredPlanMembersCount = memberRepository.expirePlan(LocalDateTime.now());
        int frozenMembersCount = memberRepository.freezeExpiredAccounts();

        System.out.println("The no of members's plan's duration have be decreased are ==> "
                + decrementedMembersPlansCount);
        System.out.println("The no of members's plan is expired is ==> "+expiredPlanMembersCount);
        System.out.println("The no of members's account have been frozen are ==> "+ frozenMembersCount);
    }

    /*
     * ==========================> SCHEDULER TASKS #02  <=================================
     *
     *      =================> RUNS EVERY DAY AT 7:00 AM <===================
     *
     *               ========> SEND EMAILS TO MEMBERS EMAIL <=========
     */

    @Scheduled(cron = "0 0 7 * * *")
    public void sendAlertMails() {
        List<Member> membersListByDurationLeft3 = memberRepository.getMemberListByDuration(3);
        for (Member member : membersListByDurationLeft3) {
            webClientService.sendAlertMessage(member);
        }

        List<Member> membersListByDurationLeftZero = memberRepository.getMemberListByDuration(0);
        for (Member member : membersListByDurationLeftZero) {
            webClientService.sendExpiredMessage(member);
        }

        List<Member> membersByFrozenAccount = memberRepository.getFrozenMemberList();
        for (Member member : membersByFrozenAccount) {
            if (member.getPlanDurationLeft() == -10 &&
                    member.getPlanExpiration().isAfter(LocalDateTime.now().plusDays(10))) {
                webClientService.sendFrozenMessage(member);
            }
        }
    }

}
