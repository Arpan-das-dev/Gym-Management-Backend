package com.gym.trainerService.Services.OtherServices;

import com.gym.trainerService.Dto.TrainerMangementDto.Requests.SpecialityResponseDto;
import com.gym.trainerService.Enums.TrainerSpeciality;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpecialityService {

    private static  final Set<String>VALID_SPECIALITIES =
            Arrays.stream(TrainerSpeciality.values())
                    .map(Enum::name)
                    .collect(Collectors.toSet());

    /**
     * @param speciality which is all enums of speciality
     * @return boolean value
     */
    public boolean isValidSpeciality(String speciality) {
        return VALID_SPECIALITIES.contains(speciality);
    }

    public String normalize(String speciality) {
        return speciality.trim().toUpperCase().replace(" ", "_");
    }

    @Cacheable(value = "allSpeciality", key = "'all'")
    public SpecialityResponseDto getAllSpecialites() {
        return SpecialityResponseDto.builder()
                .specialityList(VALID_SPECIALITIES.stream().map(this::normalize).toList())
                .build();
    }
}
