package com.gym.adminservice.Services.PlanServices;

import com.gym.adminservice.Dto.PlanDtos.Responses.CreationResponseDto;
import com.gym.adminservice.Dto.PlanDtos.Responses.UpdateResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class KafkaPlanProducerService {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC_CREATE = "plan-create";
    private static final String TOPIC_UPDATE = "plan-update";
    private static final String TOPIC_DELETE = "plan-delete";

    public void sendCreatedToPlanService(CreationResponseDto responseDto){
        kafkaTemplate.send(TOPIC_CREATE,responseDto.getId(),responseDto);
    }
    /*
    @KafkaListener(topics = "plan-create", groupId = "plan-service-group")
    public void consumePlanCreate(CreationResponseDto dto) {
        System.out.println("Plan created event received: " + dto);
         save into DB here
    }
     */

    public void sendUpdatePlanService(UpdateResponseDto responseDto){
        kafkaTemplate.send(TOPIC_UPDATE,responseDto.getId(),responseDto);
    }
    /*
     *  @KafkaListener(topics = "plan-update", groupId = "plan-service-group")
     *     public void consumePlanUpdate(UpdateResponseDto dto) {
     *         System.out.println("Plan updated event received: " + dto);
     *          update in DB here
     *     }
     * */

    public void sendDeletePlanService(String id){
        kafkaTemplate.send(TOPIC_DELETE,id,id);
    }
    /*
    @KafkaListener(topics = "plan-delete", groupId = "plan-service-group")
    public void consumePlanDelete(String id) {
        System.out.println("Plan delete event received: " + id);
        // delete from DB here
    }
     */
}
