package com.gym.member_service.Services;

import com.gym.member_service.Dto.MemberPlanDto.Requests.MembersPlanMeticsRequestDto;
import com.gym.member_service.Dto.MemberPlanDto.Requests.PlanRequestDto;
import com.gym.member_service.Dto.MemberPlanDto.Responses.MemberPlansMeticsResponseDto;
import com.gym.member_service.Exception.Exceptions.UserNotFoundException;
import com.gym.member_service.Model.Member;
import com.gym.member_service.Repositories.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberPlanSerVice {
    private final MemberRepository memberRepository;


    /*
     * ========================================> UPDATE PLAN <============================================
     *
     *
     * updates the current plan duration time and plan id
     * if still time left it will just add on rather than resting previous left
     * days
     * then update the data stored in the cache
     */
    @Caching(evict = {
            @CacheEvict(value = "memberListCache", key = "'All'"),
            @CacheEvict(value = "memberCache", key = "#requestDto.id")
    })
    @Transactional
    public String updatePlan(String id, PlanRequestDto requestDto) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Member with this id does not exist")); // throws exception
        // if no user is
        // not found the id
        member.setPlanID(requestDto.getPlanId()); // set the new plan id
        LocalDateTime current = member.getPlanExpiration(); // get the current expiration date
        member.setPlanExpiration(current.plusDays(requestDto.getDuration())); // add on the duration on previous days
        member.setPlanName(requestDto.getPlanName()); // setting the new plan
        member.setPlanDurationLeft(requestDto.getDuration()); // increased the duration in days left ;
        if (!member.getActivePlan())
            member.setActivePlan(true);
        memberRepository.save(member);
        return "plan updated successfully";
    }

    /*
     *  ================================ GET MEMBER PLAN MATRICES ========================================
     *
     *  This method return a response dto includes the member and plan matrices
     *
     */

    public List<MemberPlansMeticsResponseDto> getAllMatrices(MembersPlanMeticsRequestDto requestDto){
        List<MemberPlansMeticsResponseDto> responseDtoList = new LinkedList<>();
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
