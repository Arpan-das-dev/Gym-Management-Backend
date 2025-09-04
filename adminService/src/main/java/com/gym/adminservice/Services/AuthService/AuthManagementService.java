package com.gym.adminservice.Services.AuthService;



import com.gym.adminservice.Dto.Requests.CreateAdminRequestDto;
import com.gym.adminservice.Dto.Requests.CreateMemberRequestDto;
import com.gym.adminservice.Dto.Requests.CreateTrainerRequestDto;
import com.gym.adminservice.Dto.Responses.MemberResponseDto;
import com.gym.adminservice.Dto.Responses.SignupResponseDto;
import com.gym.adminservice.Dto.Responses.TrainerResponseDto;
import com.gym.adminservice.Enums.RoleType;
import com.gym.adminservice.Models.AdminEntity;
import com.gym.adminservice.Repository.AdminRepository;
import com.gym.adminservice.Services.WebClientServices.WebClientAuthService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@AllArgsConstructor
public class AuthManagementService {
    private final WebClientAuthService webClientService;
    private final AdminRepository adminRepository;

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
        // webClientMemberService.sendMemberDetails(memberResponseDto)

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
        //webClientTrainerService.sendTrainerDetails(trainerResponseDto)

        return "Trainer created successfully";
    }

    @Transactional
    public String createAdmin(CreateAdminRequestDto requestDto){
        SignupResponseDto responseDto = SignupResponseDto.builder()
                .email(requestDto.getEmail())
                .role(RoleType.ADMIN_ADMIN)
                .firstName(requestDto.getFirstName()).lastName(requestDto.getLastName())
                .gender(requestDto.getGender())
                .joinDate(requestDto.getJoinDate())
                .password(requestDto.getPassword())
                .build();
        AdminEntity model = AdminEntity.builder()
                .id(String.valueOf(UUID.randomUUID()))
                .firstName(requestDto.getFirstName())
                .lastName(requestDto.getLastName())
                .email(requestDto.getEmail())
                .phone(requestDto.getPhone())
                .joinDate(requestDto.getJoinDate())
                .gender(requestDto.getGender())
                .role(requestDto.getRole())
                .build();

        adminRepository.save(model);
        webClientService.sendSignupDetailsAdmin(responseDto);

        return "Admin created successfully";
    }

    public String setCustomIdToAdmin(String id, String role, String email) {
        AdminEntity entity = adminRepository.findByEmail(email).
                orElseThrow(); //userNotFoundException
        entity.setId(id);
        return "Id set successfully";
    }

    public String deleteUser(String identifier, RoleType role){
        if(role.isTrainerRole()){
            webClientService.deleteUser(identifier);
            // webClientTrainerService.deleteTrainer(identifier);
            return "trainer request deleted successfully";
        } else {
            webClientService.deleteUser(identifier);
            // webClientMemberService.deleteMember(identifier);
            return "member request deleted successfully";
        }
    }

    public String freezeAccount(String email,RoleType role){
        if(role.isTrainerRole()){
            //webClientTrainerService.freezeAccount(email,role)
            return "Trainer Account frozen Successfully";
        } else if (role.equals(RoleType.MEMBER)) {
            //webClientMemberService.freezeAccount(identifier);
            return "Member Account frozen Successfully";
        } else{
            return "account frozen successfully";
        }
    }



}
