package com.gym.adminservice.Services.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminStatusService {

    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    
    private static final String ACTIVE_SET_KEY = "ADMIN:COUNT";
    
    public void markAsActive(String id) {
        if (!isActive(id)) { // check if the Admin is already active if not then add the Admin to the set
            // to prevent duplicate entries
            redisTemplate.opsForSet().add(ACTIVE_SET_KEY, id);
            System.out.println(" 🤸🏻🤸🏻 Marking active: " + id + ", broadcasting count...");// add the Admin to the set
            broadCastLiveCount();
        }
    }

    public void markAsInactive(String id) {
        if(isActive(id)) {
            redisTemplate.opsForSet().remove(ACTIVE_SET_KEY, id);// remove the Admin from the set if he is
            System.out.println(" 🀄🀄 Marking inactive: " + id + ", broadcasting count...");
            broadCastLiveCount();
        }                                 // inactive(response form frontend)
    }

    public Long getActiveAdminsCount() {
        return redisTemplate.opsForSet().size(ACTIVE_SET_KEY); // return the size of the set later on we will use
        // this to show the active members count in the gym
    }

    public boolean isActive(String id) {
        return Boolean.TRUE
                .equals(redisTemplate.opsForSet().isMember(ACTIVE_SET_KEY, id)); // check if the Admin is in the set
        // or not
    }

    public void broadCastLiveCount(){
        Long currentActive = getActiveAdminsCount();
        messagingTemplate.convertAndSend("/topic/activeAdmins", currentActive);
        log.info("👥👥 current Admin's active count is ==> {}",currentActive);
    }

}
