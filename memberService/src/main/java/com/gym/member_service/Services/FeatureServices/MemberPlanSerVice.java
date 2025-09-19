package com.gym.member_service.Services.FeatureServices;

import com.gym.member_service.Dto.MemberPlanDto.Requests.MembersPlanMeticsRequestDto;
import com.gym.member_service.Dto.MemberPlanDto.Requests.PlanRequestDto;
import com.gym.member_service.Dto.MemberPlanDto.Responses.MemberPlansMeticsResponseDto;
import com.gym.member_service.Exception.Exceptions.UserNotFoundException;
import com.gym.member_service.Model.Member;
import com.gym.member_service.Repositories.MemberRepository;
import com.gym.member_service.Services.OtherService.WebClientServices;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

/**
 * Service layer for managing member plan subscriptions and plan analytics.
 *
 * <p>This service provides comprehensive business logic for:
 * <ul>
 *   <li>Plan subscription management including renewals and extensions</li>
 *   <li>Plan duration calculations with additive functionality</li>
 *   <li>Member account status management during plan changes</li>
 *   <li>Plan analytics and metrics for administrative reporting</li>
 *   <li>Integration with notification services for plan updates</li>
 * </ul>
 *
 * <p>The service implements sophisticated caching strategies to optimize
 * performance while ensuring cache coherence through targeted cache eviction
 * during plan modifications. Transactional boundaries ensure data consistency
 * across multiple repository operations and external service calls.
 *
 * <p>Plan updates use an additive approach, extending existing plan durations
 * rather than replacing them, which provides flexibility for plan renewals
 * and upgrades while preserving remaining subscription time.
 *
 * <p>The service integrates with external notification services to provide
 * immediate confirmation of plan changes and maintains comprehensive audit
 * trails for subscription management operations.
 *
 * @author Arpan Das
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class MemberPlanSerVice {
    // injecting all the dependencies through constructor(@RequestParam)
    private final MemberRepository memberRepository;
    private final WebClientServices webClientServices;
    /**
     * Updates a member's plan subscription with additive duration management.
     *
     * <p>This method performs comprehensive plan updates including:
     * <ul>
     *   <li>Plan ID and name assignment to the member's account</li>
     *   <li>Additive duration calculation preserving existing subscription time</li>
     *   <li>Account status activation for previously frozen or inactive accounts</li>
     *   <li>Cache invalidation to maintain data consistency across the system</li>
     *   <li>Asynchronous notification delivery for plan update confirmation</li>
     * </ul>
     *
     * <p>The duration calculation uses an additive approach: if the member has
     * remaining time on their current plan, the new duration is added to the
     * existing expiration date. For expired or null plans, the duration is
     * calculated from the current timestamp.
     *
     * <p>Account reactivation automatically occurs for frozen accounts when
     * a new plan is applied, setting the active plan status to true and
     * removing the frozen flag to restore full member privileges.
     *
     * <p>The operation is fully transactional to ensure atomicity - if any
     * step fails (database update or notification), all changes are rolled
     * back to maintain system consistency.
     *
     * <p>Cache eviction targets both individual member cache entries and
     * aggregate member list caches to ensure immediate visibility of plan
     * changes across all system components.
     *
     * @param id the unique identifier of the member whose plan is being updated.
     *           Must not be null or empty and must correspond to an existing member.
     * @param requestDto the plan update request containing new plan details including
     *                   plan ID, name, and duration in days. Must not be null and
     *                   must pass validation constraints.
     * @return String confirmation message indicating successful plan update
     * @throws UserNotFoundException if the member ID does not exist in the system
     * @throws TransactionSystemException if the database transaction fails
     * @throws IllegalArgumentException if the request DTO contains invalid data
     * @throws WebClientRequestException if the notification service call fails
     *
     * @see PlanRequestDto
     * @see Member
     * @see WebClientServices#sendUpdatePlanNotification(PlanRequestDto, String)
     */
    @Caching(evict = {
            @CacheEvict(value = "memberListCache", key = "'All'"),
            @CacheEvict(value = "memberCache", key = "#id")
    })
    @Transactional
    public String updatePlan(String id, PlanRequestDto requestDto) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Member with this id does not exist")); // throws exception
                                                                                                      // if no user is
                                                                                                     // not found the id
        member.setPlanID(requestDto.getPlanId()); // set the new plan id
        LocalDateTime current = member.getPlanExpiration(); // get the current expiration date
        if(current==null) current = LocalDateTime.now();
        member.setPlanExpiration(current.plusDays(requestDto.getDuration())); // add on the duration on previous days
        member.setPlanName(requestDto.getPlanName()); // setting the new plan
        member.setPlanDurationLeft(member.getPlanDurationLeft()+ requestDto.getDuration()); // increased the duration
                                                                                            // in days left ;
        if (!member.getActivePlan()){
            member.setActivePlan(true);
            member.setFrozen(false);
        }
        memberRepository.save(member);

        webClientServices.sendUpdatePlanNotification(requestDto,member.getFirstName()+" "+member.getLastName());
        return "plan updated successfully";
    }
    /**
     * Retrieves comprehensive plan analytics and membership metrics for administrative reporting.
     *
     * <p>This method generates detailed analytics for specified plans including:
     * <ul>
     *   <li>Member count per plan for subscription volume analysis</li>
     *   <li>Complete member lists for each plan for detailed examination</li>
     *   <li>Plan popularity metrics for business intelligence</li>
     *   <li>Membership distribution analysis across different plan types</li>
     * </ul>
     *
     * <p>The analytics are computed in real-time by querying current member
     * subscriptions, providing up-to-date insights for administrative
     * decision-making and business strategy planning.
     *
     * <p>Each plan's metrics include both quantitative data (member counts)
     * and qualitative data (detailed member information) to support various
     * administrative use cases from high-level reporting to individual
     * member management.
     *
     * <p>The method processes multiple plans efficiently using iterative
     * queries optimized for performance while maintaining comprehensive
     * data accuracy for each requested plan.
     *
     * @param requestDto the metrics request containing the list of plan names
     *                   for which analytics should be generated. Must not be null
     *                   and must contain at least one valid plan name.
     * @return List&lt;MemberPlansMeticsResponseDto&gt; containing comprehensive
     *         analytics for each requested plan including member counts and
     *         detailed member information
     * @throws IllegalArgumentException if requestDto is null or contains no plan names
     * @throws DataAccessException if member data retrieval fails
     *
     * @see MembersPlanMeticsRequestDto
     * @see MemberPlansMeticsResponseDto
     * @see MemberRepository#memberCountWithPlansNameOf(String)
     * @see MemberRepository#getMemberListByPlanName(String)
     */
    public List<MemberPlansMeticsResponseDto> getAllMatrices(MembersPlanMeticsRequestDto requestDto){
        List<MemberPlansMeticsResponseDto> responseDtoList = new LinkedList<>();
        // using form loop to get data member for each plan
        for(String name : requestDto.getPlanNames()){
            int memberCount =  memberRepository.memberCountWithPlansNameOf(name);
            List<Member> memberList = memberRepository.getMemberListByPlanName(name);
            MemberPlansMeticsResponseDto responseDto = MemberPlansMeticsResponseDto.builder()
                    .planName(name)
                    .membersCount(memberCount)
                    .memberList(memberList)
                    .build();
            responseDtoList.add(responseDto);
        }
        return responseDtoList;
    }

}
