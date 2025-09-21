package com.gym.authservice.Service;

import com.gym.authservice.Dto.Request.SignupRequestDto;
import com.gym.authservice.Dto.Response.CredentialNotificationDto;
import com.gym.authservice.Dto.Response.SignUpResponseDto;
import com.gym.authservice.Dto.Response.SignupDetailsInfoDto;
import com.gym.authservice.Entity.SignedUps;
import com.gym.authservice.Exceptions.Custom.UserNotFoundException;
import com.gym.authservice.Repository.SignedUpsRepository;
import com.gym.authservice.Roles.RoleType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final SignUpService signUpService;
    private final WebClientService webClientService;
    private final SignedUpsRepository signedUpsRepository;


@PreAuthorize("hasRole('ADMIN')")
    public SignUpResponseDto createMemberByAdmin(SignupRequestDto requestDto){
        SignUpResponseDto responseDto = signUpService.signUp(requestDto);
        CredentialNotificationDto notificationDto = CredentialNotificationDto.builder()
                .email(requestDto.getEmail())
                .name(requestDto.getFirstName()+" "+requestDto.getLastName())
                .password(requestDto.getPassword())
                .build();
        sendCredentialsNotification(notificationDto);
        return responseDto;
    }


    @PreAuthorize("hasRole('ADMIN')")
    public SignUpResponseDto createTrainerByAdmin(SignupRequestDto requestDto){
        SignUpResponseDto responseDto = signUpService.signUp(requestDto);
        CredentialNotificationDto notificationDto = CredentialNotificationDto.builder()
                .email(requestDto.getEmail())
                .name(requestDto.getFirstName()+" "+requestDto.getLastName())
                .password(requestDto.getPassword())
                .build();
        sendCredentialsNotification(notificationDto);
        return responseDto;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public SignUpResponseDto createAdminByAdmin(@Valid SignupRequestDto requestDto) {
    SignUpResponseDto responseDto = signUpService.signUp(requestDto);
    CredentialNotificationDto notificationDto = CredentialNotificationDto.builder()
            .email(requestDto.getEmail())
            .name(requestDto.getFirstName()+ " "+ requestDto.getLastName())
            .password(requestDto.getPassword())
            .build();
    sendCredentialsNotification(notificationDto);
    return responseDto;
    }
    @PreAuthorize("hasRole('ADMIN')")
    public SignupDetailsInfoDto getUserById(String id){
        SignedUps user = signedUpsRepository.findById(id)
                .orElseThrow(()->new UserNotFoundException("User with this id does not exists"));
        return SignupDetailsInfoDto.builder()
                .id(user.getId())
                .email(user.getEmail()).emailVerified(user.isEmailVerified())
                .firstName(user.getFirstName()).lastName(user.getLastName())
                .phone(user.getPhone()).phoneVerified(user.isPhoneVerified())
                .role(user.getRole())
                .gender(user.getGender())
                .joinDate(user.getJoinDate())
                .isApproved(user.isApproved())
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public SignupDetailsInfoDto getUserByEmail(String email){
        SignedUps user = signedUpsRepository.findByEmail(email)
                .orElseThrow(()->new UserNotFoundException("User with this email does not exists"));
        return SignupDetailsInfoDto.builder()
                .id(user.getId())
                .email(user.getEmail()).emailVerified(user.isEmailVerified())
                .firstName(user.getFirstName()).lastName(user.getLastName())
                .phone(user.getPhone()).phoneVerified(user.isPhoneVerified())
                .role(user.getRole())
                .gender(user.getGender())
                .joinDate(user.getJoinDate())
                .isApproved(user.isApproved())
                .build();
    }
    public String deleteByIdentifier(String identifier){
        String message = "user deleted successfully";
        if(identifier.contains("@")){
            try {
                signedUpsRepository.deleteByEmail(identifier);
            } catch (UserNotFoundException e) {
                throw new UserNotFoundException("user with this email does not exists");
            }
        } else {
            try {
                signedUpsRepository.deleteById(identifier);
            } catch (UserNotFoundException e) {
                throw new UserNotFoundException("user with this id does not exists");
            }
        }
        return message;
    }

    public void sendCredentialsNotification(CredentialNotificationDto notificationDto){
        webClientService.sendCredentials(notificationDto);
    }

    @Transactional
    public String approve(String email, boolean approve) {
    SignedUps user = signedUpsRepository.findByEmail(email)
            .orElseThrow(()-> new UserNotFoundException("No user found with this email id: "+email));
    if(approve){
        if (user.getRole().name().startsWith("TRAINER")){
            user.setRole(RoleType.TRAINER);
            user.setApproved(true);
            signedUpsRepository.save(user);
            webClientService.sendTrainerServiceToCreateNewTrainer(user);
        } else {
           user.setRole(RoleType.MEMBER);
           user.setApproved(true);
           signedUpsRepository.save(user);
           webClientService.sendMemberServiceToCreateNewMember(user);
        }
        return "Successfully approved "+user.getFirstName()+" "+user.getLastName();
    }
    signedUpsRepository.deleteByEmail(user.getEmail());
    return "User with this email: "+user.getEmail()+" is unApproved";
    }
}
