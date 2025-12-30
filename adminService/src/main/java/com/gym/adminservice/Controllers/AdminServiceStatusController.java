package com.gym.adminservice.Controllers;

import com.gym.adminservice.Services.AuthService.AdminStatusService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("${admin.active.count_URL}")
@RequiredArgsConstructor
public class AdminServiceStatusController {
    
    private final AdminStatusService adminStatusService;
    
    @PostMapping("/administrator/increment")
    public ResponseEntity<?> markAsActive(
            @RequestParam @NotBlank(message = "Unable To Proceed with Empty AdminId") String id) {
        log.info("©️©️ request received to mark {} as active and increment count",id);
        adminStatusService.markAsActive(id);
        return ResponseEntity.accepted().build();
        // returning response as ACCEPTED http status
    }

    /*
     * opens an endpoint to decrease the current member count
     * when the account is deactivated for some time
     */
    @PostMapping("/administrator/decrement")
    public ResponseEntity<?> markAsInactive(
            @RequestParam @NotBlank(message = "Unable To Proceed with Empty AdminId") String id) {
        log.info("©️©️ request received to mark {} as inactive and decrement count",id);
        adminStatusService.markAsInactive(id);
        return ResponseEntity.accepted().build();
        // returning response as ACCEPTED http status
    }

    /*
     * this endpoint to get current live count
     * of active members in gym
     * as of now it's returning but near future
     * it will return using websocket
     */
    @GetMapping("/all/active-count")
    public ResponseEntity<Long> getActiveMemberCount() {
        return ResponseEntity.status(HttpStatus.OK).body(adminStatusService.getActiveAdminsCount());
        // returning member count as OK http status
    }

}
