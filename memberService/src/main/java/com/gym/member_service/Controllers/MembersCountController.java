package com.gym.member_service.Controllers;

import com.gym.member_service.Services.MembersCountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Service
@RestController
@RequestMapping("${member-service.Base_Url.count}")
public class MembersCountController {
    private final MembersCountService otherService;

    @PostMapping("/increment")
    public ResponseEntity<?> markAsActive(@RequestParam String id){
        otherService.markAsActive(id);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/decrement")
    public ResponseEntity<?> markAsInactive(@RequestParam String id){
        otherService.markAsInactive(id);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/active-count")
    public ResponseEntity<Long> getActiveMemberCount(){
        return ResponseEntity.status(HttpStatus.OK).body(
                otherService.getActiveMembersCount()
        );
    }
}
