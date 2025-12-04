package com.gym.trainerService.Services.MemberServices;
import com.gym.trainerService.Dto.MemberDtos.Requests.AssignMemberRequestDto;
import com.gym.trainerService.Dto.MemberDtos.Responses.MemberResponseDto;
import com.gym.trainerService.Dto.MemberDtos.Wrappers.AllMemberResponseWrapperDto;
import com.gym.trainerService.Exception.Custom.InvalidMemberException;
import com.gym.trainerService.Exception.Custom.NoTrainerFoundException;
import com.gym.trainerService.Models.Member;
import com.gym.trainerService.Models.Trainer;
import com.gym.trainerService.Repositories.MemberRepository;
import com.gym.trainerService.Repositories.TrainerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service class handling business logic for managing members assigned to trainers.
 * <p>
 * This class interacts with {@link MemberRepository} and {@link TrainerRepository}
 * and leverages Spring Cache abstraction to optimize member list retrievals.
 * </p>
 *
 * <p><b>Caching Strategy:</b></p>
 * <ul>
 *   <li>{@code @Cacheable} – Used for reading all members by trainer ID.</li>
 *   <li>{@code @CacheEvict} – Used to clear cache entries upon add/delete to maintain consistency.</li>
 * </ul>
 *
 * @author Arpan Das
 * @since 1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MemberManagementService {

    // injecting MemberRepository by constructor injection using @RequiredArgsConstructor
    private final MemberRepository memberRepository;
    // injecting TrainerRepository by constructor injection using @RequiredArgsConstructor
    private final TrainerRepository trainerRepository;

    /**
     * Assigns a member to a trainer. Handles three cases:
     * <ul>
     *   <li>Extends eligibility if already assigned and expired.</li>
     *   <li>Throws exception if already active under another trainer.</li>
     *   <li>Reassigns if previously expired with another trainer.</li>
     * </ul>
     *
     * <p>Evicts trainer cache to ensure fresh data on next retrieval.</p>
     *
     * @param trainerId  unique identifier of the trainer
     * @param requestDto contains details for member assignment
     * @return {@link MemberResponseDto} of the assigned member
     */
    @CacheEvict(value = "AllMemberListCache", key = "#trainerId")
    @Transactional
    public MemberResponseDto addMember(String trainerId, AssignMemberRequestDto requestDto) {
        log.info("Request reached service to assign member {} to trainer {}",
                requestDto.getMemberId(), trainerId);

        Trainer trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> new NoTrainerFoundException("No trainer found with the id: " + trainerId));

        log.info("Fetched trainer {} {} from DB", trainer.getFirstName(), trainer.getLastName());

        // Check if the member already exists
        Optional<Member> existingMemberOpt = memberRepository.findById(requestDto.getMemberId());

        if (existingMemberOpt.isPresent()) {
            Member member = existingMemberOpt.get();
            boolean sameTrainer = member.getTrainerId().equals(trainer.getTrainerId());
            boolean expired = member.getEligibilityEnd().isBefore(LocalDate.now());

            if (sameTrainer && expired) {
                log.info("Extending eligibility for existing member {}", member.getMemberId());
                return incrementEligibility(requestDto, member);
            } else if (!sameTrainer && !expired) {
                log.warn("Member {} is already assigned to another trainer", member.getMemberId());
                throw new InvalidMemberException("Cannot assign new trainer: member is still active with another trainer");
            } else if (!sameTrainer) {
                log.info("Reassigning member {} to new trainer {}", member.getMemberId(), trainerId);
                return updateMember(trainerId, member, requestDto);
            }
        }

        // Create new member
        Member newMember = Member.builder()
                .memberId(requestDto.getMemberId())
                .trainerId(trainer.getTrainerId())
                .memberName(requestDto.getMemberName())
                .memberProfileImageUrl(requestDto.getMemberProfileImageUrl())
                .eligibilityEnd(requestDto.getEligibilityEnd())
                .build();

        memberRepository.save(newMember);
        log.info("New member {} assigned to trainer {}", newMember.getMemberId(), trainerId);

        return memberResponseDtoBuilder(newMember);
    }

    /**
     * Retrieves all members associated with a given trainer.
     * <p>
     * Results are cached in Redis for 6 hours via "AllMemberListCache".
     * </p>
     *
     * @param trainerId unique identifier of the trainer
     * @return {@link AllMemberResponseWrapperDto} containing member list
     */
    @Cacheable(value = "AllMemberListCache",key ="#trainerId")
    public AllMemberResponseWrapperDto getAllMembersByTrainerId(String trainerId) {
        List<Member> members = memberRepository.findByTrainerId(trainerId);
        List<MemberResponseDto> responseDtoList = members.stream()
                .map(this::memberResponseDtoBuilder).toList();
        return AllMemberResponseWrapperDto.builder()
                .memberResponseDtoList(responseDtoList)
                .build();
    }

    /**
     * Deletes a member record for the given trainer and member IDs.
     * <p>
     * Cache is evicted to ensure subsequent reads fetch updated data.
     * </p>
     *
     * @param trainerId ID of the trainer
     * @param memberId  ID of the member
     * @return result message indicating success or failure
     */
    @Transactional
    @CacheEvict(value = "AllMemberListCache",key ="#trainerId")
    public String deleteMemberByIds(String trainerId, String memberId) {
        int effectedRows = memberRepository.deleteByTrainerAndMember(trainerId,memberId);
       return effectedRows > 0 ? "Successfully deleted" : "No memberFound with the ids";
    }

    /**
     * Updates an existing member’s trainer assignment and eligibility.
     *
     * @param trainerId  new trainer ID
     * @param member     existing member entity
     * @param requestDto request containing updated eligibility date
     * @return updated {@link MemberResponseDto}
     */
    private MemberResponseDto updateMember(String trainerId, Member member, AssignMemberRequestDto requestDto) {
        member.setTrainerId(trainerId);
        member.setEligibilityEnd(requestDto.getEligibilityEnd());
        memberRepository.save(member);
        return memberResponseDtoBuilder(member);
    }

    /**
     * Extends a member’s eligibility period under the same trainer.
     *
     * @param requestDto request containing new eligibility end date
     * @param member     existing member entity
     * @return updated {@link MemberResponseDto}
     */
    private MemberResponseDto incrementEligibility(AssignMemberRequestDto requestDto, Member member) {
        long additionalDays = requestDto.getEligibilityEnd().toEpochDay() - LocalDate.now().toEpochDay();
        member.setEligibilityEnd(member.getEligibilityEnd().plusDays(additionalDays));
        memberRepository.save(member);
        return memberResponseDtoBuilder(member);
    }

    /**
     * Helper method to convert {@link Member} entity to {@link MemberResponseDto}.
     *
     * @param member entity to convert
     * @return corresponding response DTO
     */
    private MemberResponseDto memberResponseDtoBuilder(Member member) {
        return MemberResponseDto.builder()
                .memberId(member.getMemberId())
                .trainerId(member.getTrainerId())
                .memberName(member.getMemberName())
                .memberProfileImageUrl(member.getMemberProfileImageUrl())
                .eligibilityEnd(member.getEligibilityEnd())
                .build();
    }
}
