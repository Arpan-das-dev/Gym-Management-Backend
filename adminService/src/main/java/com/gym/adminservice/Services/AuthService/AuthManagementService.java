package com.gym.adminservice.Services.AuthService;

import com.gym.adminservice.Dto.Requests.CreateAdminRequestDto;
import com.gym.adminservice.Dto.Requests.CreateMemberRequestDto;
import com.gym.adminservice.Dto.Requests.CreateTrainerRequestDto;
import com.gym.adminservice.Dto.Responses.*;
import com.gym.adminservice.Enums.RoleType;
import com.gym.adminservice.Models.AdminEntity;
import com.gym.adminservice.Repository.AdminRepository;
import com.gym.adminservice.Services.WebClientServices.WebClientAuthService;
import com.gym.adminservice.Utils.IdGenUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class AuthManagementService {
    private final WebClientAuthService webClientService;
    private final AdminRepository adminRepository;
    private final IdGenUtil idGenUtil;
    /**
     * this service will create a user in the auth service via admin and sent
     * requests via webclient
     * also it will create the user in their respective service like member in
     * member service and trainer
     */
    public UserCreationResponseDto createMember(CreateMemberRequestDto memberRequestDto) {
        // create the signup dto to send to the auth service
        SignupRequestDto responseDto = SignupRequestDto.builder()
                .email(memberRequestDto.getEmail()).phone(memberRequestDto.getPhone())
                .role(RoleType.MEMBER_ADMIN)
                .firstName(memberRequestDto.getFirstName()).lastName(memberRequestDto.getLastName())
                .gender(memberRequestDto.getGender())
                .joinDate(memberRequestDto.getJoinDate())
                .password(memberRequestDto.getPassword())
                .build();
        // create the member dto to send to the member service but
        // as we don't have member service yet so we will comment it out
        MemberResponseDto memberResponseDto = MemberResponseDto.builder()
                .firstName(memberRequestDto.getFirstName()).lastName(memberRequestDto.getLastName())
                .gender(memberRequestDto.getGender())
                .joinDate(memberRequestDto.getJoinDate())
                .email(memberRequestDto.getEmail()).phone(memberRequestDto.getPhone())
                .build();
        // send the request to the auth service via webclient
        webClientService.sendSignupDetailsMember(responseDto);
        // as we don't have member service yet so we will comment it out
        //webClientMemberService.sendMemberDetails(memberResponseDto)

        return new UserCreationResponseDto("Member created successfully");
    }

    // this service will create a trainer in the auth service via admin and sent
    // requests via webclient
    // also it will create the trainer in their respective service like trainer in
    // trainer service
    public UserCreationResponseDto createTrainer(CreateTrainerRequestDto trainerRequestDto) {
        // create the signup dto to send to the auth service
        SignupRequestDto responseDto = SignupRequestDto.builder()
                .email(trainerRequestDto.getEmail()).phone(trainerRequestDto.getPhone())
                .role(RoleType.TRAINER_ADMIN)
                .firstName(trainerRequestDto.getFirstName()).lastName(trainerRequestDto.getLastName())
                .gender(trainerRequestDto.getGender())
                .joinDate(trainerRequestDto.getJoinDate())
                .password(trainerRequestDto.getPassword())
                .build();
        // create the member dto to send to the member service but
        // as we don't have member service yet so we will comment it out
        TrainerResponseDto trainerResponseDto = TrainerResponseDto.builder()
                .firstName(trainerRequestDto.getFirstName()).lastName(trainerRequestDto.getLastName())
                .gender(trainerRequestDto.getGender())
                .joinDate(trainerRequestDto.getJoinDate())
                .email(trainerRequestDto.getEmail()).phone(trainerRequestDto.getPhone())
                .password(trainerRequestDto.getPassword())
                .build();
        // send the request to the auth service via webclient
        webClientService.sendSignupDetailsTrainer(responseDto);
        // as we don't have member service yet so we will comment it out
        // webClientTrainerService.sendTrainerDetails(trainerResponseDto)

        return new UserCreationResponseDto("Trainer created successfully");
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
    public UserCreationResponseDto createAdmin(CreateAdminRequestDto requestDto) {
        // create the admin entity to save in the admin service database
        AdminEntity model = AdminEntity.builder()
                .id(idGenUtil.idGeneration(requestDto.getRole().name(),
                        requestDto.getGender(),
                        requestDto.getJoinDate()))
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
        // create the signup dto to send to the auth service
        AdminCreationRequestDto responseDto = AdminCreationRequestDto.builder()
                .id(model.getId())
                .firstName(model.getFirstName()).lastName(model.getLastName())
                .email(model.getEmail()).phone(model.getPhone())
                .password(requestDto.getPassword())
                .isEmailVerified(false).isPhoneVerified(false)
                .isApproved(true)
                .joinDate(model.getJoinDate())
                .gender(model.getGender())
                .role(model.getRole())
                .build();
        // send the request to the auth service via webclient
        webClientService.sendSignupDetailsAdmin(responseDto);

        return new UserCreationResponseDto("Admin created successfully");
    }

    /*
     * this service will set a custom id to the admin
     * this id will be used to identify the admin in the auth service
     * only super admin can set the custom id to another admin and in other roles no
     * one can set the id
     */
    public UserCreationResponseDto setCustomIdToAdmin(String id, String role, String email) {

        AdminEntity entity = adminRepository.findByEmail(email).orElseThrow(); // userNotFoundException
        entity.setId(id);
        return new UserCreationResponseDto("Id set successfully");
    }

    /*
     * this service will delete a user from the auth service via admin and sent
     * requests via webclient
     * also it will delete the user from their respective service like member from
     * member service and trainer from
     * and as this service is called by admin so only admin can delete a user
     * without any authentication
     */
    public UserCreationResponseDto deleteUser(String identifier, RoleType role) {
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
            return new UserCreationResponseDto("trainer request deleted successfully");
        } else if (role.equals(RoleType.ADMIN)) {
            adminRepository.deleteByEmail(identifier);
            return new UserCreationResponseDto("admin deleted successfully");
        } else {
            webClientService.deleteUser(identifier);
            // webClientMemberService.deleteMember(identifier);
            return new UserCreationResponseDto("member request deleted successfully");
        }
    }

    public UserCreationResponseDto freezeAccount(String email, RoleType role) {
        if (role.isTrainerRole()) {
            //webClientTrainerService.freezeAccount(email,role)
            return new UserCreationResponseDto("Trainer Account frozen Successfully");
        } else if (role.equals(RoleType.MEMBER)) {
            // webClientMemberService.freezeAccount(identifier);
            return new UserCreationResponseDto("Member Account frozen Successfully");
        } else {
            return new UserCreationResponseDto("account frozen successfully");
        }
    }

}
