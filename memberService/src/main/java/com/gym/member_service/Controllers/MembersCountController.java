package com.gym.member_service.Controllers;

import com.gym.member_service.Services.OtherService.MembersCountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Service
@RestController
@RequestMapping("${member-service.Base_Url.count}")
/*
 * This controller opens an endpoint
 * so when a user(member) logged in the current active members
 * count increases or decreases and also returns live count to users
 * the base url of this controller is defined in the application.properties
 */
public class MembersCountController {
    // injecting dependency using constructor(@RequiredArgConstructor)
    private final MembersCountService otherService;

    /*
     * if a successful login then the auth service send
     * here a request to increase the current member count
     */
    @PostMapping("/increment")
    public ResponseEntity<?> markAsActive(@RequestParam String id) {
        otherService.markAsActive(id);
        return ResponseEntity.accepted().build();
        // returning response as ACCEPTED http status
    }

    /*
     * opens an endpoint to decrease the current member count
     * when the account is deactivated for some time
     */
    @PostMapping("/decrement")
    public ResponseEntity<?> markAsInactive(@RequestParam String id) {
        otherService.markAsInactive(id);
        return ResponseEntity.accepted().build();
        // returning response as ACCEPTED http status
    }

    /*
     * this endpoint to get current live count
     * of active members in gym
     * as of now it's returning but near future
     * it will return using websocket
     */
    @GetMapping("/active-count")
    public ResponseEntity<Long> getActiveMemberCount() {
        return ResponseEntity.status(HttpStatus.OK).body(otherService.getActiveMembersCount());
        // returning member count as OK http status
    }


}
