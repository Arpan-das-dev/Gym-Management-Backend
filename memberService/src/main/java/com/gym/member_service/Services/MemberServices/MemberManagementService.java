package com.gym.member_service.Services.MemberServices;

import com.gym.member_service.Dto.MemberManagementDto.Requests.FreezeRequestDto;
import com.gym.member_service.Dto.MemberManagementDto.Requests.MemberCreationRequestDto;
import com.gym.member_service.Dto.MemberManagementDto.Responses.AllMemberListResponseDto;
import com.gym.member_service.Dto.MemberManagementDto.Responses.LoginStreakResponseDto;
import com.gym.member_service.Dto.MemberManagementDto.Wrappers.AllMembersInfoWrapperResponseDtoList;
import com.gym.member_service.Exception.Exceptions.DuplicateUserFoundException;
import com.gym.member_service.Exception.Exceptions.UserNotFoundException;
import com.gym.member_service.Dto.MemberManagementDto.Responses.AllMemberResponseDto;
import com.gym.member_service.Model.*;
import com.gym.member_service.Repositories.MemberRepository;
import com.gym.member_service.Services.OtherService.MembersCountService;
import com.gym.member_service.Services.OtherService.WebClientServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor

/*
 * this service class is responsible for member management
 * here this service will create member get all member details and so on...
 */
public class MemberManagementService {

    private final MemberRepository memberRepository;
    private final WebClientServices webClientService;
    private final MembersCountService countService;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    /*
     *  This method creates a new member and save
     * there data in the database
     * this happens after a successful login and when admin creates
     * a new member
     * Caching by redis(using docker) and when a new member stored in the db
     * the cache will update data in both cache(memberListCache::All,
     * memberCache:id(dynamic id))
     */

    @Caching(evict = {
            @CacheEvict(value = "memberListCache", key = "'All'"),
            @CacheEvict(value = "memberCache", allEntries = true)
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
     * This method returns user data and took member id to perform service logic
     * When the method calls it fetches the data from db, but then it stores the data
     * in Cache
     * and also get updated time to time when other methods changes the data.
     * THIS method return required member details and take id as query parameter
     */
    @Cacheable(value = "memberCache", key = "#id")
    public AllMemberResponseDto getMemberById(String id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Member with this id does not exist")); // if no user found
                                                                                                   // throws an exception
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
     * this method returns a list of members details which will be cached in the
     * redis data
     * so next time it does not require to fetch DB
     * here using stream and .toList() to return a list of data
     */


    @Cacheable(
            value = "memberListCache",
            key = "'search=' + (#searchBy ?: '') +':gender=' + (#gender ?: '') +':status=' + (#status ?: '') +':sort=' + #sortBy +':dir=' + #sortDirection +':p=' + #pageNo +':s=' + #pageSize"
            )
    public AllMembersInfoWrapperResponseDtoList getAllMember(String searchBy,String gender,String status,
                                          String sortBy, String sortDirection, int pageNo, int pageSize)
    {
        long start = System.currentTimeMillis();
        log.info("Fetching members | search={} | gender={} | status={} | sort={} | dir={} at:: {}",
                searchBy, gender, status, sortBy, sortDirection,LocalDateTime.now().format(formatter));

        String search = (searchBy == null || searchBy.isBlank()) ? "" : searchBy.trim();
        String gFilter = (gender == null || gender.isBlank() || gender.equalsIgnoreCase("all")) ? "" : gender.trim();
        String sFilter = (status == null || status.isBlank() || status.equalsIgnoreCase("all")) ? "" : status.trim();

        // sorting
        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        Sort sort = switch (sortBy.toLowerCase()) {
            case "planexpiration" -> Sort.by(direction, "planExpiration");
            case "durationleft" -> Sort.by(direction, "planDurationLeft");
            case "name" -> Sort.by(direction, "firstName", "lastName");
            case "date" -> Sort.by(direction, "lastLogin");
            case "gender" -> Sort.by(direction, "gender");
            default -> Sort.by(Sort.Direction.DESC, "planExpiration");
        };

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        // 🔥 Main query: search + filters in one repo method
        Page<Member> members = memberRepository.findAllWithFilters(search, gFilter, sFilter, pageable);

        // convert entity → DTO
        List<AllMemberListResponseDto> list =
                members.stream().map(m -> AllMemberListResponseDto.builder()
                        .id(m.getId())
                        .firstName(m.getFirstName())
                        .lastName(m.getLastName())
                        .email(m.getEmail())
                        .phone(m.getPhone())
                        .gender(m.getGender())
                        .planDurationLeft(m.getPlanDurationLeft() == null ? 0: m.getPlanDurationLeft() )
                        .planExpiration(m.getPlanExpiration())
                        .planId(m.getPlanID())
                        .planName(m.getPlanName())
                        .frozen(m.isFrozen())
                        .profileImageUrl(profileImageMapper(m.getProfileImageUrl()))
                        .active(countService.isActive(m.getId()))
                        .build()
                ).toList();

        long end = System.currentTimeMillis();
        log.info("Processed in {} ms", end - start);
        log.info("sending {} no of members and {} have last page and remaining elements are {}",
                list.size(),members.isLast() ? "doesn't": "does", members.getTotalElements()-list.size());
        return AllMembersInfoWrapperResponseDtoList.builder()
                .responseDtoList(list)
                .pageNo(members.getNumber())
                .lastPage(members.isLast())
                .pageSize(members.getSize())
                .totalElements(members.getTotalElements())
                .build();
    }
    /*
     * Generally in Auth service user decided to delete account this
     * method runs and delete a specific member and all records from database
     * this method take String id as query param and return results accordingly
     * and also update the cache data from the Cache
     * so cache remains fresh
     */

    @Caching(evict = {
            @CacheEvict(value = "memberListCache", allEntries = true),
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
     *
     * If any user is not Logged in on any day
     * when next day the member will log in the streak resets to 1
     * when the member logsIn then a webclient will send the id, and it will change
     * the login streak
     * and then also update the cache
     */
    @Caching(
            evict = {
                    @CacheEvict(value = "memberListCache", allEntries = true),
                    @CacheEvict(value = "memberCache", key = "#id"),
            },
            put = {
                    @CachePut(value = "loginStreak", key = "'userId::'#id")
            }
    )
    @Transactional
    public LoginStreakResponseDto setLoginStreak(String id) {
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
            Integer maxLoginStreak = member.getMaxLoginStreak();
            Integer loginSteak = member.getLoginStreak();
            member.setLoginStreak(loginSteak + 1);
            member.setMaxLoginStreak(Math.max(maxLoginStreak,loginSteak+1));

        } else {
            member.setLastLogin(LocalDateTime.now()); // set the last login time as of now
        }
        memberRepository.save(member);
        return LoginStreakResponseDto.builder()
                .logInStreak(member.getLoginStreak())
                .maxLogInStreak(member.getMaxLoginStreak())
                .build();
    }
    @Cacheable(value = "loginStreak", key = "'userId::'#id")
    public LoginStreakResponseDto getLoginStreak(String id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Member with this id does not exist")); // finding member
                                                                                                     // if it doesn't
                                                                                                     // exist throws
                                                                                                     // exception

        return LoginStreakResponseDto.builder()
                .logInStreak(member.getLoginStreak())
                .maxLogInStreak(member.getMaxLoginStreak())
                .build();
    }

    /*
     * This method here is to allow admin to freeze any
     *  member account forcefully so that they can not access the
     *  Dashboard or get details in the frontend
     * this method will set that if the account is frozen or not
     * if any account is frozen then he might not be able to access dashboard but
     * can log in
     * after all changes this updates the data stored in the Cache
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "memberListCache", allEntries = true),
            @CacheEvict(value = "memberCache", key = "#requestDto.id")
    })
    public String freezeOrUnFrozen(FreezeRequestDto requestDto) {
        String time = LocalDateTime.now().format(formatter);
        System.out.println("Request received to change status for member's account on::"+ time);
        long start = System.currentTimeMillis();
        String freeze = requestDto.isFreeze()? "freeze" : "unfreeze";
        log.info("Request received to {} for id {}",freeze,requestDto.getId());
        String id = requestDto.getId();
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Member with this id does not exist")); // if no user found
                                                                                                    // Throws exception
        member.setFrozen(requestDto.isFreeze()); // set the freeze status as per request
        memberRepository.save(member);
        if(requestDto.isFreeze()) webClientService.sendFrozenMessage(member);
        long end = System.currentTimeMillis();
        log.info("sending request on {} ,request completed in {}",time,end-start);
        Mono<String> res =  webClientService.sendFrozenMessageByAdmin(member,requestDto.isFreeze());
        log.info("got response from notification service {}",res);
        return requestDto.isFreeze() ? // check if true returns first response otherwise second response
                "Account frozen successfully" : "Account unfrozen successfully";
    }


    private String profileImageMapper(String imageUrl) {
        if(imageUrl == null || imageUrl.isBlank()) return "";
        return imageUrl;
    }
}
