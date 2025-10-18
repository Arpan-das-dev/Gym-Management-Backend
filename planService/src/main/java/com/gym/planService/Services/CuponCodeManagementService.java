package com.gym.planService.Services;

import com.gym.planService.Dtos.CuponDtos.Requests.CreateCuponCodeRequestDto;
import com.gym.planService.Dtos.CuponDtos.Requests.UpdateCuponRequestDto;
import com.gym.planService.Dtos.CuponDtos.Responses.CuponCodeResponseDto;
import com.gym.planService.Dtos.CuponDtos.Wrappers.AllCuponCodeWrapperResponseDto;
import com.gym.planService.Exception.Custom.CuponCodeNotFoundException;
import com.gym.planService.Exception.Custom.DuplicateCuponCodeFoundException;
import com.gym.planService.Exception.Custom.PlanNotFoundException;
import com.gym.planService.Models.PlanCuponCode;
import com.gym.planService.Repositories.PlanCuponCodeRepository;
import com.gym.planService.Repositories.PlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class CuponCodeManagementService {

    private final PlanRepository planRepository;
    private final PlanCuponCodeRepository cuponCodeRepository;
    private final String Redis_Cache_CUPONCODE_PREFIX = "CUPON_CODE::";
    private final StringRedisTemplate redisTemplate;

    public AllCuponCodeWrapperResponseDto createCuponCode(String planId, CreateCuponCodeRequestDto requestDto) {
        log.info("Request reached service class for plan id::{}",planId);
        if (!planRepository.existsById(planId)) {
            log.warn("no plan found with id :: {}",planId);
            throw new PlanNotFoundException("Plan does not exist with the id :: {}"+planId);
        } else if (cuponCodeRepository.existsById(requestDto.getCuponCode())) {
            log.warn("A cupon already exists with the name :: {}",requestDto.getCuponCode());
            throw new DuplicateCuponCodeFoundException("Cupon code :: "+requestDto.getCuponCode()+" already exists");
        }
        PlanCuponCode cuponCode = PlanCuponCode.builder()
                .cuponCode(requestDto.getCuponCode())
                .validity(requestDto.getValidity())
                .percentage(requestDto.getOffPercentage())
                .planId(planId)
                .build();
        log.info("New cupon code Created with id:: {}",cuponCode.getCuponCode());
        long duration = requestDto.getValidity().toEpochDay() - LocalDate.now().toEpochDay();
        String key = Redis_Cache_CUPONCODE_PREFIX+cuponCode.getCuponCode();
        redisTemplate
                .opsForValue().set(key,cuponCode.getCuponCode(),Duration.ofDays(duration));
        return responseDtoBuilder(cuponCode.getPlanId());
    }

    public CuponCodeResponseDto updateCupon(String cuponCode, UpdateCuponRequestDto requestDto) {
        log.info("Request reached service class for cuponCode::{}",cuponCode);
        String key = Redis_Cache_CUPONCODE_PREFIX+cuponCode;
        boolean check = Objects.equals(redisTemplate.opsForValue().get(key), cuponCode);
        if (!check) {
            log.warn("No cupon code found with name :: {}",cuponCode);
            throw new CuponCodeNotFoundException("No cupon code exists with name:: "+cuponCode);
        } else if (!planRepository.existsById(requestDto.getPlanId())) {
            log.warn("plan or cupon code does not matches");
            throw new PlanNotFoundException("No plan found for the cupon code:: "+requestDto.getPlanId());
        }
        PlanCuponCode planCuponCode = cuponCodeRepository.findById(cuponCode)
                .orElseThrow(()-> new CuponCodeNotFoundException("No cupon code found with the id::"+cuponCode));

        planCuponCode.setValidity(requestDto.getValidity());
        planCuponCode.setPercentage(requestDto.getOffPercentage());
        cuponCodeRepository.save(planCuponCode);
        log.info("Successfully updated for cupon code :: {}",planCuponCode.getCuponCode());
        redisTemplate.delete(Redis_Cache_CUPONCODE_PREFIX);
        String updatedKey = Redis_Cache_CUPONCODE_PREFIX+planCuponCode.getCuponCode();
        long duration = planCuponCode.getValidity().toEpochDay() - LocalDate.now().toEpochDay();
        redisTemplate.opsForValue().set(updatedKey,planCuponCode.getCuponCode(),Duration.ofDays(duration));
        log.info("Cupon saved in the redis with key :: {}",updatedKey);
        return CuponCodeResponseDto.builder()
                .cuponCode(planCuponCode.getCuponCode())
                .offPercentage(planCuponCode.getPercentage())
                .validityDate(planCuponCode.getValidity())
                .build();
    }

    public AllCuponCodeWrapperResponseDto getCuponCodesByPlanId(String planId) {
       return responseDtoBuilder(planId);
    }


    public String deleteCuponByCuponCode(String cuponCode,String planId) {
        String key = Redis_Cache_CUPONCODE_PREFIX+cuponCode;
        boolean check = Objects.equals(redisTemplate.opsForValue().get(key), cuponCode);
        if (!check) {
            log.warn("No cupon code found with the name :: {}",cuponCode);
            throw new CuponCodeNotFoundException("No cupon code exists with name:: "+cuponCode);
        }
        cuponCodeRepository.deleteById(cuponCode);
        log.info("successfully deleted cupon for plan ::--->{}",planId);
        redisTemplate.delete(key);
        log.info("Cupon code removed from cache");
        return "Successfully deleted cuponCode";
    }



    private AllCuponCodeWrapperResponseDto responseDtoBuilder(String planId) {
        List<PlanCuponCode> cuponCodes = cuponCodeRepository.findAllByPlanId(planId);
        List<CuponCodeResponseDto> responseDtoList = cuponCodes.stream()
                .map(cupons-> CuponCodeResponseDto.builder()
                        .cuponCode(cupons.getCuponCode())
                        .validityDate(cupons.getValidity())
                        .offPercentage(cupons.getPercentage())
                        .build()).toList();
        return AllCuponCodeWrapperResponseDto.builder()
                .responseDtoList(responseDtoList)
                .build();
    }

    public Boolean validateCupon( String cuponCode) {
        return Objects
                .equals(redisTemplate
                        .opsForValue()
                        .get(Redis_Cache_CUPONCODE_PREFIX + cuponCode), cuponCode);
    }
}
