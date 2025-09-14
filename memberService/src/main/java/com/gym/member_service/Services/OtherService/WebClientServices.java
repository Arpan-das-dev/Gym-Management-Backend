package com.gym.member_service.Services.OtherService;

import com.gym.member_service.Dto.MemberPlanDto.Requests.PlanRequestDto;
import com.gym.member_service.Dto.NotificationDto.MailNotificationDto;
import com.gym.member_service.Dto.NotificationDto.PlanActivationNotificationDto;
import com.gym.member_service.Model.Member;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.LocalDateTime;

/*
 * This service layer is responsible to interact with notification service
 * to send email or sms on different occasion
 * using webclient, Asynchronously
 * @Async is used to make sure each request runs on
 * a separate thread
 */
@Service
public class WebClientServices {

    @Value("${app.notificationService.plan_Url}")
    private final String Notification_MemberService_URL;
    private final WebClient.Builder webClient;
    // using constructor to inject dependencies;
    public WebClientServices( @Value("${app.notificationService.plan_Url}") String Notification_MemberService_URL,
                              WebClient.Builder webClient)
    {
        this.Notification_MemberService_URL = Notification_MemberService_URL;
        this.webClient = webClient;
    }
    // send mail Asynchronously when plan's duration left for 3 days
    @Async
    public void sendAlertMessage(Member member) {
        // endpoint for post the dto
        String endpoint = "/alert";
        // subject for mail
        String subject = "Plan will be expired in 3 days";
        // building final dto to post the body in web client
        MailNotificationDto notificationDto = notificationDtoBuilder(member,subject,member.getPlanExpiration());
       sendMailToNotificationService(endpoint,notificationDto);
    }
    // send mail Asynchronously when plan's duration ends
    @Async
    public void sendExpiredMessage(Member member) {
        // endpoint for post the dto
        String endpoint = "/expired";
        // building final dto to post the body in web client
        String subject = "Plan Expired";
        // building final dto to post the body in web client
        MailNotificationDto notificationDto = notificationDtoBuilder(member,subject,LocalDateTime.now());
        sendMailToNotificationService(endpoint,notificationDto);
    }
    // send mail Asynchronously when member's account is frozen
    @Async
    public void sendFrozenMessage(Member member) {
        // endpoint for post the dto
        String endpoint = "/frozen";
        // building final dto to post the body in web client
        String subject = "Your plan has expired";
        // building final dto to post the body in web client
        MailNotificationDto notificationDto = notificationDtoBuilder(member,subject,LocalDateTime.now());
        sendMailToNotificationService(endpoint,notificationDto);
    }
    /*
    * This is a helper method to build a notification dto
    * it takes the member, subject, and date to
    * build the notification dto
    * */
    private MailNotificationDto notificationDtoBuilder(Member member, String subject,LocalDateTime time){
        // using builder pattern to build the notifcation dto
        return MailNotificationDto.builder()
                .name(member.getFirstName()+" "+ member.getLastName())
                .subject(subject)
                .memberId(member.getId())
                .mailId(member.getEmail())
                .phone(member.getPhone())
                .time(time)
                .build();
    }
    /*
    * A private helper method to send dto to notification service
    * to send mail asynchronously
    * for both success and error it's print the result
    * */
    private void sendMailToNotificationService(String endpoint, MailNotificationDto notificationDto) {
        // set the final url of notification service
        String notificationDtoUrl= Notification_MemberService_URL+endpoint;
        webClient.build().post()    // using post() as it will be a post method in the controller
                .uri(notificationDtoUrl) // passing the final url
                .bodyValue(notificationDto) // add the payload
                .retrieve().toBodilessEntity().subscribe(
             success-> System.out.println("Mail send to notification service"),
                error-> System.out.println("Unable to send mail to notification service ")
                );
        // print the result as per result
    }

    /*
    * This method will send notification service as soon as
    * member bought any plan
    * */
    public void sendUpdatePlanNotification(PlanRequestDto requestDto,String name) {
        PlanActivationNotificationDto notificationDto = PlanActivationNotificationDto.builder()
                .planName(requestDto.getPlanName())
                .subject("Plan Renewed")
                .activationDate(LocalDate.now())
                .planExpiration(LocalDate.now().plusDays(requestDto.getDuration()))
                .duration(requestDto.getDuration())
                .build();
        String endPoint = "/activePlan";
        webClient.build().post()
                .uri(Notification_MemberService_URL+endPoint)
                .bodyValue(notificationDto)
                .retrieve().toBodilessEntity().subscribe(
                        success-> System.out.println("Mail send to "+name),
                        error-> System.out.println("Unable to send mail to "+name)
                );
    }
}
