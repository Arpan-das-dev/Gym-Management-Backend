package com.gym.member_service.Controllers;

import com.gym.member_service.Services.MembersOtherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Service
@RestController
@RequestMapping("${member-service.Base_Url.count}")
public class MembersOtherController {
    private final MembersOtherService otherService;

    @PostMapping("/increment")
    public ResponseEntity<?> markAsActive(@RequestParam String id){
        otherService.markAsActive(id);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/decrement")
    public ResponseEntity<?> markAsInactive(@RequestParam String id){
        otherService.marAsInactive(id);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/ActiveCount")
    public ResponseEntity<Long> getActiveMemberCount(){
        return ResponseEntity.status(HttpStatus.OK).body(
                otherService.getActiveMembersCount()
        );
    }
}
