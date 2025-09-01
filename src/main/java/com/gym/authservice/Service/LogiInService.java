package com.gym.authservice.Service;

import com.gym.authservice.Dto.Request.LogiInRequestDto;
import com.gym.authservice.Dto.Response.LoginResponseDto;
import com.gym.authservice.Entity.SignedUps;
import com.gym.authservice.Exceptions.Custom.UnauthorizedAccessException;
import com.gym.authservice.Exceptions.Custom.UserNotFoundException;
import com.gym.authservice.Repository.SignedUpsRepository;
import com.gym.authservice.Config.Jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogiInService {

    private final SignedUpsRepository signedUpsRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginResponseDto login(LogiInRequestDto requestDto){

        SignedUps user;
        if(requestDto.getIdentifier().contains("@")){
             user = signedUpsRepository.findByEmail(requestDto.getIdentifier())
                    .orElseThrow(() -> new UserNotFoundException("User with this email not found"));
        } else {
            user = signedUpsRepository.findById(requestDto.getIdentifier())
                    .orElseThrow(() -> new UserNotFoundException("User with this id not found"));
        }
        if(!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())){
            throw new UnauthorizedAccessException("Invalid password");
        }
        String token = jwtUtil.generateToken(user.getEmail(),user.getRole().name(), user.getId());
        return new LoginResponseDto(token, user.getRole().name());
    }

    public Boolean verifyBeforeDelete(LogiInRequestDto requestDto){
        SignedUps user;
        if(requestDto.getIdentifier().contains("@")){
            user = signedUpsRepository.findByEmail(requestDto.getIdentifier())
                    .orElseThrow(() -> new UserNotFoundException("User with this email not found"));
        } else {
            user = signedUpsRepository.findById(requestDto.getIdentifier())
                    .orElseThrow(() -> new UserNotFoundException("User with this id not found"));
        }
        if(!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())){
            throw new UnauthorizedAccessException("Invalid password");
        }
        return true;
    }
}
