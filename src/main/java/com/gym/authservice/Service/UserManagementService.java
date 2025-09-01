package com.gym.authservice.Service;

import com.gym.authservice.Dto.Request.SignupRequestDto;
import com.gym.authservice.Dto.Response.CredentialNotificationDto;
import com.gym.authservice.Dto.Response.SignUpResponseDto;
import com.gym.authservice.Dto.Response.SignupDetailsInfoDto;
import com.gym.authservice.Entity.SignedUps;
import com.gym.authservice.Exceptions.Custom.UserNotFoundException;
import com.gym.authservice.Repository.SignedUpsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final SignUpService signUpService;
    private final NotificationService notificationService;
    private final SignedUpsRepository signedUpsRepository;

/*
                        CREATE
    this method is for only admin so the admin can manually create user through his/her dashboard
*/
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

    /*
                        CREATE
    this method is for only admin to create trainer manually and the trainer need no approval form the admin
     */
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
    /*
                        READ
    this method is for admin so he can see new logins via id
     */
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
    /*
                        READ
    this method is for admin so he can see new logins via email
     */
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
    /*
    this method to send credentials details so that the user can log in first time,and he may change his password
     */
    public void sendCredentialsNotification(CredentialNotificationDto notificationDto){
        notificationService.sendCredentials(notificationDto);
    }

}
