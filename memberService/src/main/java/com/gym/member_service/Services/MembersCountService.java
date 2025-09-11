package com.gym.member_service.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

/*
 * this service is responsible for handling the active members
 * it will show how many members are active in the gym (lively)
 * we are using redis set to store the active members and the ttl is 15 minutes
 */
public class MembersCountService {
    // we will use redis set to store the active members
    private final RedisTemplate<String, String> redisTemplate;
    // the name of the set
    private final String ACTIVE_MEMBER_SET = "memberCountCache";

    /*
     * this method will mark the member as active first check if the member is
     * already active
     * if not then add the member to the set to prevent duplicate entries
     */
    public void markAsActive(String id) {
        if (!isActive(id)) { // check if the member is already active if not then add the member to the set
                             // to prevent duplicate entries
            redisTemplate.opsForSet().add(ACTIVE_MEMBER_SET, id); // add the member to the set
        }
    }

    public void markAsInactive(String id) {
        redisTemplate.opsForSet().remove(ACTIVE_MEMBER_SET, id); // remove the member from the set if he is
                                                                 // inactive(response form frontend)
    }

    public Long getActiveMembersCount() {
        return redisTemplate.opsForSet().size(ACTIVE_MEMBER_SET); // return the size of the set later on we will use
                                                                  // this to show the active members count in the gym
    }

    public boolean isActive(String id) {
        return Boolean.TRUE
                .equals(redisTemplate.opsForSet().isMember(ACTIVE_MEMBER_SET, id)); // check if the member is in the set
                                                                                    // or not
    }

    /*
     * later on we will add a scheduler task which will sent to the frontend
     * if the member is active or not via websocket
     * and also we will remove the member from the active set if he didn't respond
     * and the above methods will also be sent to the frontend via websocket
     * so that the frontend can show the active members in real time
     */
}
