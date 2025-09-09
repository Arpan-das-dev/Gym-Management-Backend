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

    /**
     * this service will create a user in the auth service via admin and sent
     * requests via webclient
     * also it will create the user in their respective service like member in
     * member service and trainer
     */
    public String createMember(CreateMemberRequestDto memberRequestDto) {
        // create the signup dto to send to the auth service
        SignupResponseDto responseDto = SignupResponseDto.builder()
                .email(memberRequestDto.getEmail()).phone(memberRequestDto.getPhone())
                .role(memberRequestDto.getRole())
                .firstName(memberRequestDto.getFirstName()).lastName(memberRequestDto.getLastName())
                .gender(memberRequestDto.getGender())
                .joinDate(memberRequestDto.getJoinDate())
                .password(memberRequestDto.getPassword())
                .build();
        // create the member dto to send to the member service but
        // as we don't have member service yet so we will comment it out
        MemberResponseDto memberResponseDto = MemberResponseDto.builder()
                .firstName(memberRequestDto.getFirstName()).lastName(memberRequestDto.getLastName())
                .age(memberRequestDto.getAge()).gender(memberRequestDto.getGender())
                .joinDate(memberRequestDto.getJoinDate()).address(memberRequestDto.getAddress())
                .email(memberRequestDto.getEmail()).phone(memberRequestDto.getPhone())
                .password(memberRequestDto.getPassword())
                .build();
        // send the request to the auth service via webclient
        webClientService.sendSignupDetailsMember(responseDto);
        // as we don't have member service yet so we will comment it out
        // webClientMemberService.sendMemberDetails(memberResponseDto)

        return "Member created successfully";
    }

    // this service will create a trainer in the auth service via admin and sent
    // requests via webclient
    // also it will create the trainer in their respective service like trainer in
    // trainer service
    public String createTrainer(CreateTrainerRequestDto trainerRequestDto) {
        // create the signup dto to send to the auth service
        SignupResponseDto responseDto = SignupResponseDto.builder()
                .email(trainerRequestDto.getEmail()).phone(trainerRequestDto.getPhone())
                .role(RoleType.TRAINER_ADMIN)
                .firstName(trainerRequestDto.getFirstName()).lastName(trainerRequestDto.getLastName())
                .gender(trainerRequestDto.getGender())
                .joinDate(trainerRequestDto.getJoinDate())
                .password(trainerRequestDto.getPassword())
                .build();
        // create the member dto to send to the member service but
        // as we dont have member service yet so we will comment it out
        TrainerResponseDto trainerResponseDto = TrainerResponseDto.builder()
                .firstName(trainerRequestDto.getFirstName()).lastName(trainerRequestDto.getLastName())
                .age(trainerRequestDto.getAge()).gender(trainerRequestDto.getGender())
                .joinDate(trainerRequestDto.getJoinDate()).address(trainerRequestDto.getAddress())
                .email(trainerRequestDto.getEmail()).phone(trainerRequestDto.getPhone())
                .password(trainerRequestDto.getPassword())
                .experience(trainerRequestDto.getExperience())
                .specialties(trainerRequestDto.getSpecialties())
                .build();
        // send the request to the auth service via webclient
        webClientService.sendSignupDetailsTrainer(responseDto);
        // as we don't have member service yet so we will comment it out
        // webClientTrainerService.sendTrainerDetails(trainerResponseDto)

        return "Trainer created successfully";
    }

    /*
     * this service will create an admin in the auth service via admin and sent
     * requests via webclient
     * also it will create the admin in their respective service like admin in admin
     * service
     * and only super admin can create another admin any other request will be
     * denied and throw an exception
     */
    @Transactional
    public String createAdmin(CreateAdminRequestDto requestDto) {
        // create the signup dto to send to the auth service
        SignupResponseDto responseDto = SignupResponseDto.builder()
                .email(requestDto.getEmail())
                .role(RoleType.ADMIN_ADMIN)
                .firstName(requestDto.getFirstName()).lastName(requestDto.getLastName())
                .gender(requestDto.getGender())
                .joinDate(requestDto.getJoinDate())
                .password(requestDto.getPassword())
                .build();
        // create the admin entity to save in the admin service database
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
        // save the admin entity in the admin service database`
        adminRepository.save(model);
        // send the request to the auth service via webclient
        webClientService.sendSignupDetailsAdmin(responseDto);

        return "Admin created successfully";
    }

    /*
     * this service will set a custom id to the admin
     * this id will be used to identify the admin in the auth service
     * only super admin can set the custom id to another admin and in other roles no
     * one can set the id
     */
    public String setCustomIdToAdmin(String id, String role, String email) {

        AdminEntity entity = adminRepository.findByEmail(email).orElseThrow(); // userNotFoundException
        entity.setId(id);
        return "Id set successfully";
    }

    /*
     * this service will delete a user from the auth service via admin and sent
     * requests via webclient
     * also it will delete the user from their respective service like member from
     * member service and trainer from
     * and as this service is called by admin so only admin can delete a user
     * without any authentication
     */
    public String deleteUser(String identifier, RoleType role) {
        /*
         * this service will delete a user from the auth service via admin and sent
         * requests via webclient
         * also it will delete the user from their respective service like member from
         * member service and trainer from
         * and as this service is called by admin so only admin can delete a user
         * without any authentication
         */
        if (role.isTrainerRole()) {
            webClientService.deleteUser(identifier);
            // webClientTrainerService.deleteTrainer(identifier);
            return "trainer request deleted successfully";
        } else if (role.equals(RoleType.ADMIN)) {
            adminRepository.deleteByEmail(identifier);
            return "admin deleted successfully";
        } else {
            webClientService.deleteUser(identifier);
            // webClientMemberService.deleteMember(identifier);
            return "member request deleted successfully";
        }
    }

    public String freezeAccount(String email, RoleType role) {
        if (role.isTrainerRole()) {
            // webClientTrainerService.freezeAccount(email,role)
            return "Trainer Account frozen Successfully";
        } else if (role.equals(RoleType.MEMBER)) {
            // webClientMemberService.freezeAccount(identifier);
            return "Member Account frozen Successfully";
        } else {
            return "account frozen successfully";
        }
    }

}
