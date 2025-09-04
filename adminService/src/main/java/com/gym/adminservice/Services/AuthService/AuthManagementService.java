package com.gym.adminservice.Services;



import com.gym.adminservice.Dto.Requests.CreateMemberRequestDto;
import com.gym.adminservice.Dto.Requests.CreateTrainerRequestDto;
import com.gym.adminservice.Dto.Responses.MemberResponseDto;
import com.gym.adminservice.Dto.Responses.SignupResponseDto;
import com.gym.adminservice.Dto.Responses.TrainerResponseDto;
import com.gym.adminservice.Enums.RoleType;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.management.relation.RoleInfo;

@Service
@AllArgsConstructor
public class AuthManagementService {
    private final WebClientAuthService webClientService;

    public String createMember(CreateMemberRequestDto memberRequestDto) {
        SignupResponseDto responseDto = SignupResponseDto.builder()
                .email(memberRequestDto.getEmail()).phone(memberRequestDto.getPhone())
                .role(memberRequestDto.getRole())
                .firstName(memberRequestDto.getFirstName()).lastName(memberRequestDto.getLastName())
                .gender(memberRequestDto.getGender())
                .joinDate(memberRequestDto.getJoinDate())
                .password(memberRequestDto.getPassword())
                .build();
        MemberResponseDto memberResponseDto = MemberResponseDto.builder()
                .firstName(memberRequestDto.getFirstName()).lastName(memberRequestDto.getLastName())
                .age(memberRequestDto.getAge()).gender(memberRequestDto.getGender())
                .joinDate(memberRequestDto.getJoinDate()).address(memberRequestDto.getAddress())
                .email(memberRequestDto.getEmail()).phone(memberRequestDto.getPhone())
                .password(memberRequestDto.getPassword())
                .build();

        webClientService.sendSignupDetailsMember(responseDto);
        webClientService.sendMemberDetails(memberResponseDto);

        return "Member created successfully";
    }

    public String createTrainer(CreateTrainerRequestDto trainerRequestDto){
        SignupResponseDto responseDto = SignupResponseDto.builder()
                .email(trainerRequestDto.getEmail()).phone(trainerRequestDto.getPhone())
                .role(RoleType.TRAINER_ADMIN)
                .firstName(trainerRequestDto.getFirstName()).lastName(trainerRequestDto.getLastName())
                .gender(trainerRequestDto.getGender())
                .joinDate(trainerRequestDto.getJoinDate())
                .password(trainerRequestDto.getPassword())
                .build();
        TrainerResponseDto trainerResponseDto = TrainerResponseDto.builder()
                .firstName(trainerRequestDto.getFirstName()).lastName(trainerRequestDto.getLastName())
                .age(trainerRequestDto.getAge()).gender(trainerRequestDto.getGender())
                .joinDate(trainerRequestDto.getJoinDate()).address(trainerRequestDto.getAddress())
                .email(trainerRequestDto.getEmail()).phone(trainerRequestDto.getPhone())
                .password(trainerRequestDto.getPassword())
                .experience(trainerRequestDto.getExperience())
                .specialties(trainerRequestDto.getSpecialties())
                .build();

        webClientService.sendSignupDetailsTrainer(responseDto);
        webClientService.sendTrainerDetails(trainerResponseDto);

        return "Trainer created successfully";
    }

    public String deleteUser(String identifier, RoleInfo role){
        if(role.getName().contains("TRAINER")){
            webClientService.deleteUser(identifier);
            // webClientTrainerService.deleteTrainer(identifier)
            return "trainer request deleted successfully";
        } else {
            // webClientMemberService.deleteMember(identifier)
            return "member request deleted successfully";
        }
    }


}
