package com.gym.member_service.Services.MemberServices;

import com.gym.member_service.Dto.MemberManagementDto.Requests.FreezeRequestDto;
import com.gym.member_service.Dto.MemberManagementDto.Requests.MemberCreationRequestDto;
import com.gym.member_service.Dto.MemberManagementDto.Responses.AllMemberListResponseDto;
import com.gym.member_service.Dto.MemberManagementDto.Responses.LoginStreakResponseDto;
import com.gym.member_service.Dto.MemberManagementDto.Responses.MemberInfoResponseDto;
import com.gym.member_service.Dto.MemberManagementDto.Wrappers.AllMembersInfoWrapperResponseDtoList;
import com.gym.member_service.Exception.Exceptions.DuplicateUserFoundException;
import com.gym.member_service.Exception.Exceptions.UserNotFoundException;
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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
/**

 Service class responsible for comprehensive member management operations.

 <p>This service handles:
 <ul>
 <li>Creating new members with consistent initialization and cache eviction</li>
 <li>Retrieving individual member details with caching</li>
 <li>Fetching paginated, filtered, and sorted lists of members with caching</li>
 <li>Deleting members and invalidating caches</li>
 <li>Managing login streak logic with daily validation and cache updates</li>
 <li>Freezing or unfreezing member accounts with notification integration and cache management</li>
 </ul>
 <p>Uses Redis for caching frequently accessed data and ensures cache coherence by evicting or updating on writes.
 Incorporates detailed logging at info and debug levels to trace request flows, performance timings, and important decision points.

 <p>Transactional annotations guarantee data integrity during critical modifications.
 @author Arpan Das

 @version 1.0

 @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor

public class MemberManagementService {

    private final MemberRepository memberRepository;
    private final WebClientServices webClientService;
    private final MembersCountService countService;
    private final StringRedisTemplate redisTemplate;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    /**

     Creates a new member in the system.

     <p>Throws an exception if a member with the same ID already exists.
     Evicts relevant caches to keep system state consistent.

     @param requestDto details of the member to create

     @return confirmation message upon successful creation

     @throws DuplicateUserFoundException if ID already exists
     */

    @CacheEvict(value = "memberListCache", key = "'All'")
    @Transactional
    public String createMember(MemberCreationRequestDto requestDto) {
        if (memberRepository.existsById(requestDto.getId())) {
            throw new DuplicateUserFoundException("User with this id already exists");
        }
        Member member = Member.builder()
                .id(requestDto.getId())
                .firstName(requestDto.getFirstName())
                .lastName(requestDto.getLastName())
                .email(requestDto.getEmail())
                .phone(requestDto.getPhone())
                .gender(requestDto.getGender())
                .joinDate(requestDto.getJoinDate())
                .activeInGym(false)
                .activePlan(false)
                .loginStreak(0)
                .planDurationLeft(0)
                .planID("")
                .planName("")
                .MaxLoginStreak(0)
                .profileImageUrl("")
                .build();

        memberRepository.save(member);
        log.info("Created new member with id: {}", requestDto.getId());
        return "A new member created successfully";
    }

    /**

     Retrieves member details by ID with caching.

     @param id member ID

     @return DTO containing member information

     @throws UserNotFoundException if member does not exist
     */
    @Cacheable(value = "memberInfo", key = "#id")
    public MemberInfoResponseDto getMemberById(String id) {
        log.info("Requested member details for id: {}", id);
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Member with this id does not exist"));
        log.info("Found member: {} with id: {}", member.getFirstName() + " " + member.getLastName(), member.getId());
        return MemberInfoResponseDto.builder()
                .memberId(member.getId())
                .firstName(member.getFirstName())
                .lastName(member.getLastName())
                .email(member.getEmail())
                .phone(member.getPhone())
                .gender(member.getGender())
                .frozen(member.isFrozen())
                .build();
    }

    /**

     Retrieves a filtered and paginated list of member details with caching.

     @param searchBy optional search string

     @param gender optional gender filter

     @param status optional status filter

     @param sortBy field to sort by

     @param sortDirection sort direction asc or desc

     @param pageNo page number (zero-based)

     @param pageSize size of each page

     @return wrapper DTO containing list of member info and pagination metadata
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
    /**

     Deletes a member by ID and evicts relevant caches.

     @param id member ID to delete

     @return confirmation message on successful deletion

     @throws UserNotFoundException if member not found by given ID
     */

    @CacheEvict(value = "memberListCache", allEntries = true)
    @Transactional
    public String deleteMemberById(String id) {
        if (!memberRepository.existsById(id)) {
            throw new UserNotFoundException("Member with this id not found");
        }
        memberRepository.deleteById(id);
        log.info("Deleted member with id: {}", id);
        return "Member with this id: " + id + " deleted successfully";
    }

    /**

     Sets or updates a member's login streak with daily evaluation logic.

     <p>Prevents multiple updates for same day. Handles streak continuation including Sunday skip rule,
     resetting streak if login missed, and updating max streak count.

     Cache entries related to member lists and login streaks are evicted/updated appropriately.

     @param id member ID whose login streak is to be updated

     @return DTO containing current and maximum login streak values

     @throws UserNotFoundException if member does not exist
     */
    @CachePut(value = "loginStreak", key = "#id")
    @Transactional
    @CacheEvict(value = "loginStreak", key = "#id")
    public LoginStreakResponseDto setLoginStreak(String id) {
        log.info("Attempting to set login streak for member id :: {}", id);
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        boolean loggedInAgain = Boolean.TRUE
                .equals(redisTemplate.opsForSet().isMember("memberCountCache", id));
        if(loggedInAgain) {
            log.warn("Member {} has already logged in today ({}). No change to streak.", id, today);
            return this.getLoginStreak(id);
        }

        // 1. Find the member
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Member with this id does not exist"));

        LocalDate lastLoginDate = (member.getLastLogin() != null) ? member.getLastLogin().toLocalDate() : null;

        log.debug("Current date: {} | Member's last login date: {}", today, lastLoginDate);
        // fallback if somehow redis value and key is not present
        if (lastLoginDate != null && lastLoginDate.isEqual(today)) {
            log.warn("Member {} has already logged in today ({}). No change to streak.", id, today);

            // Update the timestamp just in case, and save the member before returning.
            return LoginStreakResponseDto.builder()
                    .logInStreak(member.getLoginStreak())
                    .maxLogInStreak(member.getMaxLoginStreak())
                    .build();
        }

        //  Determine if the streak continues
        boolean continuesStreak = false;
        String streakStatus = "RESET";

        if (lastLoginDate != null) {
            // Check 3a: Normal consecutive day (last login was yesterday)
            boolean loggedInYesterday = lastLoginDate.isEqual(today.minusDays(1));

            // Check 3b: Sunday closure rule: Today is Monday and last login was Saturday (2 days ago).
            boolean skippedSunday = (today.getDayOfWeek() == DayOfWeek.MONDAY) && lastLoginDate.isEqual(today.minusDays(2));

            continuesStreak = loggedInYesterday || skippedSunday;

            if (continuesStreak) {
                streakStatus = skippedSunday ? "CONTINUE_SUNDAY_SKIP" : "CONTINUE_NORMAL";
            }
        } else {
            // This is the first login.
            streakStatus = "FIRST_LOGIN";
        }

        // --- APPLY STREAK LOGIC ---
        if (continuesStreak) {
            // CASE 2: Streak Increment
            int newStreak = member.getLoginStreak() + 1;
            member.setLoginStreak(newStreak);

            // Safely update MaxLoginStreak
            int maxLoginStreak = member.getMaxLoginStreak() != null ? member.getMaxLoginStreak() : 0;
            member.setMaxLoginStreak(Math.max(maxLoginStreak, newStreak));

            log.info("Streak continued for Member {} ({}). New streak: {}", id, streakStatus, newStreak);
        } else {
            // CASE 3: Streak Reset or First Login
            int newStreak = 1;
            member.setLoginStreak(newStreak);

            // Safely initialize or update MaxLoginStreak
            int maxLoginStreak = member.getMaxLoginStreak() != null ? member.getMaxLoginStreak() : 0;
            member.setMaxLoginStreak(Math.max(maxLoginStreak, newStreak));

            if ("FIRST_LOGIN".equals(streakStatus)) {
                log.info("Member {} is logging in for the first time. Starting streak: 1.", id);
            } else {
                log.warn("Member {} missed a login day. Previous login: {}. Streak reset to 1.", id, lastLoginDate);
            }
        }

        memberRepository.save(member);

        log.info("Login streak update complete for member {}. Streak: {}, Max Streak: {}",
                id, member.getLoginStreak(), member.getMaxLoginStreak());

        return LoginStreakResponseDto.builder()
                .logInStreak(member.getLoginStreak())
                .maxLogInStreak(member.getMaxLoginStreak())
                .build();
    }

    /**

     Retrieves the current and maximum login streak for a member.

     @param id member ID

     @return DTO with login streak details

     @throws UserNotFoundException if member not found
     */
    @Cacheable(value = "loginStreak", key = "#id")
    public LoginStreakResponseDto getLoginStreak(String id) {
        log.info("Request received to get login streak for member id: {}", id);
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Member with this id does not exist"));
        log.info("Returning login streak: {} and max streak: {} for member id: {}", member.getLoginStreak(), member.getMaxLoginStreak(), id);
        return LoginStreakResponseDto.builder()
                .logInStreak(member.getLoginStreak())
                .maxLogInStreak(member.getMaxLoginStreak())
                .build();
    }

    /**

     Freezes or unfreezes a member account.

     <p>Updates freeze status and sends notification via WebClientService.
     Evicts affected caches to reflect status changes immediately.

     Logs all key steps and performance timing.

     @param requestDto DTO containing member ID and freeze/unfreeze flag

     @return confirmation string message indicating result

     @throws UserNotFoundException if member does not exist
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "memberListCache", allEntries = true),
            @CacheEvict(value = "memberCache", key = "#requestDto.id")
    })
    public String freezeOrUnFrozen(FreezeRequestDto requestDto) {
        String time = LocalDateTime.now().format(formatter);
        log.info("Request received to change freeze status for member id: {} at {}", requestDto.getId(), time);
        long start = System.currentTimeMillis();

        String freeze = requestDto.isFreeze() ? "freeze" : "unfreeze";
        Member member = memberRepository.findById(requestDto.getId())
                .orElseThrow(() -> new UserNotFoundException("Member with this id does not exist"));
        member.setFrozen(requestDto.isFreeze());
        log.info("set {} for member {}",freeze,member.getFirstName()+" "+member.getLastName());
        memberRepository.save(member);

        if (requestDto.isFreeze()) webClientService.sendFrozenMessage(member);

        long end = System.currentTimeMillis();
        log.info("Processed freeze/unfreeze request for member id: {} in {} ms", requestDto.getId(), end - start);

        Mono<String> res = webClientService.sendFrozenMessageByAdmin(member, requestDto.isFreeze());
        log.info("Received notification service response: {}", res);

        return requestDto.isFreeze() ? "Account frozen successfully" : "Account unfrozen successfully";
    }

    private String profileImageMapper(String imageUrl) {
        if(imageUrl == null || imageUrl.isBlank()) return "";
        return imageUrl;
    }
}
