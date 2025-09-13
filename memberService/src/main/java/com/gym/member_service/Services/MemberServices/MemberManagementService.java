package com.gym.member_service.Services;

import com.gym.member_service.Dto.MemberManagementDto.Requests.FreezeRequestDto;
import com.gym.member_service.Dto.MemberManagementDto.Requests.MemberCreationRequestDto;
import com.gym.member_service.Exception.Exceptions.DuplicateUserFoundException;
import com.gym.member_service.Exception.Exceptions.UserNotFoundException;
import com.gym.member_service.Dto.MemberManagementDto.Responses.AllMemberResponseDto;
import com.gym.member_service.Model.*;
import com.gym.member_service.Repositories.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor

/*
 * this service class is responsible for member management
 * here this service will create member get all member details and so on...
 */
public class MemberManagementService {

    private final MemberRepository memberRepository;
    private final WebClientServices webClientService;

    // ===========================> CREATE MEMBER <========================================

    /*
     * Caching by redis(using docker) and when a new member stored in the db
     * the cache will update data in both cache(memberListCache::All,
     * memberCache:id(dynamic id))
     */

    @Caching(evict = {
            @CacheEvict(value = "memberListCache", key = "'All'"),
            @CacheEvict(value = "memberCache", key = "#requestDto.id")
    })
    @Transactional
    public String createMember(MemberCreationRequestDto requestDto) {
        if (memberRepository.existsById(requestDto.getId())) {
            throw new DuplicateUserFoundException("User with this id already exists"); // if the member already present
                                                                                       // in the db then throw an
                                                                                       // exception
        }
        Member member = Member.builder()
                .id(requestDto.getId())
                .firstName(requestDto.getFirstName())
                .lastName(requestDto.getLastName())
                .email(requestDto.getEmail())
                .phone(requestDto.getPhone())
                .gender(requestDto.getGender())
                .joinDate(requestDto.getJoinDate())
                .build();

        memberRepository.save(member); // saves the member in the db
        return "A new member created successfully";
    }

    /*
     * ===============================> GET MEMBER BY ID <=====================================
     * 
     * 
     * When the method calls it fetches the data from db, but then it stores the data
     * in Cache
     * and also get updated time to time when other methods changes the data.
     * THIS method return required member details and take id as query parameter
     */
    @Cacheable(value = "memberCache", key = "#id")
    public AllMemberResponseDto getMemberById(String id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Member with this id does not exist"));
        return responseDto(member); // a helper method defined below to return the required data
    }

    // this helper method is only used in the above method to make it more readable
    private AllMemberResponseDto responseDto(Member member) {
        return AllMemberResponseDto.builder()
                .id(member.getId()).imageUrl(member.getProfileImageUrl())
                .firstName(member.getFirstName()).lastName(member.getLastName())
                .email(member.getEmail()).phone(member.getPhone())
                .gender(member.getGender())
                .planExpiration(member.getPlanExpiration())
                .loginStreak(member.getLoginStreak()).currentBmi(member.getCurrentBmi())
                .frozen(member.isFrozen())
                .build();
    }

    /*
     * ==================================> GET ALL MEMBERS <=====================================
     * 
     * 
     * this method returns a list of members details which will be cached in the
     * redis data
     * so next time it does not require to fetch DB
     * here using stream and .toList() to return a list of data
     */

    @Cacheable(value = "memberListCache", key = "'All'")
    public List<AllMemberResponseDto> getAllMember() {
        List<Member> members = memberRepository.findAll();
        return members.stream().map(member -> AllMemberResponseDto.builder()
                .id(member.getId()).imageUrl(member.getProfileImageUrl())
                .firstName(member.getFirstName()).lastName(member.getLastName())
                .email(member.getEmail()).phone(member.getPhone())
                .gender(member.getGender())
                .planExpiration(member.getPlanExpiration())
                .frozen(member.isFrozen())
                .build()).toList();
    }

    /*
     * ================================> DELETE MEMBER BY ID <================================
     * 
     * 
     * this method take String id as query param and return results accordingly
     * and also update the cache data from the Cache
     */

    @Caching(evict = {
            @CacheEvict(value = "memberListCache", key = "'All'"),
            @CacheEvict(value = "memberCache", key = "#id")
    })
    @Transactional
    public String deleteMemberById(String id) {
        // first try to check if the member already exists
        if (!memberRepository.existsById(id)) {
            throw new UserNotFoundException("Member with this id not found");
        }
        // deleting member from db
        memberRepository.deleteById(id);
        return "Member with this id: " + id + " deleted successfully";
    }

    /*
     * ===================================> SET LOGIN STREAK <=======================================
     * 
     * 
     * when the member logsIn then a webclient will send the id, and it will change
     * the login streak
     * and then also update the cache
     */
    @Caching(evict = {
            @CacheEvict(value = "memberListCache", key = "'All'"),
            @CacheEvict(value = "memberCache", key = "#id")
    })
    @Transactional
    public Integer setAndGetLoginStreak(String id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Member with this id does not exist")); // finding member
                                                                                                     // if it doesn't
                                                                                                     // exist throws
                                                                                                     // exception
        LocalDateTime today = LocalDateTime.now();
        if (member.getLastLogin() == null || member.getLastLogin().isBefore(today.minusDays(1))) // if last login or
                                                                                                 // last login date is
                                                                                                 // more than current
                                                                                                 // date then reset the
                                                                                                 // streak to 1
        {
            member.setLoginStreak(1);
        } else if (member.getLastLogin().equals(today.minusDays(1))) { // if current date time and the last login time
                                                                       // has difference has one day increase the login
                                                                       // streak
            member.setLoginStreak(member.getLoginStreak() + 1);
        } else {
            member.setLastLogin(LocalDateTime.now()); // set the last login time as of now
        }
        memberRepository.save(member);
        return member.getLoginStreak();
    }

    /*
     * ========================================> FREEZE OR UNFREEZE <=======================================
     * 
     * 
     * this method will set that if the account is frozen or not
     * if any account is frozen then he might not be able to access dashboard but
     * can log in
     * after all changes this updates the data stored in the Cache
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "memberListCache", key = "'All'"),
            @CacheEvict(value = "memberCache", key = "#requestDto.id")
    })
    public String freezeOrUnFreezed(FreezeRequestDto requestDto) {
        String id = requestDto.getId();
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Member with this id does not exist"));

        member.setFrozen(requestDto.isFreeze()); // set the freeze status as per request
        memberRepository.save(member);
        if(requestDto.isFreeze()) webClientService.sendFrozenMessage(member);
        return requestDto.isFreeze() ? // check if true returns first response otherwise second response
                "Account frozen successfully" : "Account unfrozen successfully";
    }

}
