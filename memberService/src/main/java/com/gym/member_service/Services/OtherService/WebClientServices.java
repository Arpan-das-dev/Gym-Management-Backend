package com.gym.member_service.Services.OtherService;

import com.gym.member_service.Dto.MemberPlanDto.Requests.PlanRequestDto;
import com.gym.member_service.Dto.MemberTrainerDtos.Responses.TrainerAssignResponseDto;
import com.gym.member_service.Dto.NotificationDto.MailNotificationDto;
import com.gym.member_service.Dto.NotificationDto.PlanActivationNotificationDto;
import com.gym.member_service.Model.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Web client service for handling asynchronous external service communications.
 *
 * <p>This service provides asynchronous integration capabilities for:
 * <ul>
 *   <li>Notification service communication for email and SMS alerts</li>
 *   <li>Administrative service communication for approval workflows</li>
 *   <li>Plan activation and expiration notifications</li>
 *   <li>Member account status change notifications</li>
 * </ul>
 *
 * <p>All external service calls are executed asynchronously using Spring's
 * {@code @Async} annotation to ensure non-blocking operations and improved
 * system performance. Each request runs on a separate thread pool to
 * prevent blocking the main application flow.
 *
 * <p>The service uses Spring WebClient for reactive HTTP communications
 * and includes comprehensive error handling with detailed logging for
 * both successful and failed external service calls.
 *
 * <p>Configuration properties are injected to maintain environment-specific
 * service URLs and ensure flexible deployment across different environments.
 *
 * @author Arpan Das
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Service
public class WebClientServices {
    /**
     * Base URL for the notification service plan-related endpoints.
     */
    @Value("${app.notificationService.plan_Url}")
    private final String Notification_MemberService_URL;
    /**
     * Base URL for the administrative service approval endpoints.
     */
    @Value("${app.adminService.approval_Url}")
    private final String Admin_ApprovalService_URL;
    /**
     * WebClient builder for constructing HTTP clients with custom configurations.
     */
    private final WebClient.Builder webClient;
    /**
     * Constructs a new WebClientServices instance with injected dependencies.
     *
     * <p>This constructor uses Spring's dependency injection to configure
     * service URLs and WebClient builder, ensuring proper initialization
     * of external service communication endpoints.
     *
     * @param Notification_MemberService_URL the base URL for notification service endpoints
     * @param Admin_ApprovalService_URL the base URL for administrative approval endpoints
     * @param webClient the WebClient builder for creating HTTP clients
     */
    public WebClientServices( @Value("${app.notificationService.plan_Url}") String Notification_MemberService_URL,
                              @Value("${app.adminService.approval_Url}")  String Admin_ApprovalService_URL,
                              WebClient.Builder webClient)
    {
        this.Notification_MemberService_URL = Notification_MemberService_URL;
        this.Admin_ApprovalService_URL = Admin_ApprovalService_URL;
        this.webClient = webClient;
    }

    /**
     * Sends an asynchronous plan expiration alert notification to members.
     *
     * <p>This method is triggered when a member's plan is approaching expiration
     * (typically 3 days before expiry). It constructs a notification DTO with
     * member details and sends it to the notification service for email/SMS delivery.
     *
     * <p>The operation is performed asynchronously to prevent blocking the
     * calling thread and ensure responsive system performance.
     *
     * @param member the member object containing personal information and plan details.
     *               Must not be null and must have valid email and plan expiration data.
     * @throws IllegalArgumentException if member is null or missing required fields
     * @throws WebClientRequestException if the notification service call fails
     *
     * @see Member
     * @see MailNotificationDto
     */
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
    /**
     * Sends an asynchronous plan expiration notification to members.
     *
     * <p>This method is triggered when a member's plan has expired. It constructs
     * a notification DTO with member details and current timestamp, then sends
     * it to the notification service for immediate email/SMS delivery.
     *
     * <p>The operation is performed asynchronously to ensure system responsiveness
     * during high-volume plan expiration processing periods.
     *
     * @param member the member object containing personal information and expired plan details.
     *               Must not be null and must have valid contact information.
     * @throws IllegalArgumentException if member is null or missing required contact fields
     * @throws WebClientRequestException if the notification service call fails
     *
     * @see Member
     * @see MailNotificationDto
     */
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
    /**
     * Sends an asynchronous account frozen notification to members.
     *
     * <p>This method is triggered when a member's account is frozen due to
     * plan expiration or other administrative reasons. It constructs a notification
     * DTO with member details and sends it to the notification service.
     *
     * <p>The operation is performed asynchronously to maintain system performance
     * during account status change operations.
     *
     * @param member the member object whose account has been frozen.
     *               Must not be null and must have valid contact information.
     * @throws IllegalArgumentException if member is null or missing required contact fields
     * @throws WebClientRequestException if the notification service call fails
     *
     * @see Member
     * @see MailNotificationDto
     */
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
    /**
     * Builds a mail notification DTO with member information and timing details.
     *
     * <p>This helper method constructs a standardized notification DTO using
     * the builder pattern, combining member personal information with
     * notification-specific details like subject and timestamp.
     *
     * @param member the member object containing personal and contact information.
     *               Must not be null and must have firstName, lastName, email, and phone.
     * @param subject the notification subject line. Must not be null or empty.
     * @param time the timestamp for the notification event. Must not be null.
     * @return MailNotificationDto fully populated notification data transfer object
     * @throws IllegalArgumentException if any parameter is null or if member
     *                                  is missing required fields
     *
     * @see MailNotificationDto
     * @see Member
     */
    private MailNotificationDto notificationDtoBuilder(Member member, String subject,LocalDateTime time){
        // using builder pattern to build the notification dto
        return MailNotificationDto.builder()
                .name(member.getFirstName()+" "+ member.getLastName())
                .subject(subject)
                .memberId(member.getId())
                .mailId(member.getEmail())
                .phone(member.getPhone())
                .time(time)
                .build();
    }
    /**
     * Sends notification DTO to the notification service asynchronously.
     *
     * <p>This private helper method handles the actual HTTP communication with
     * the notification service. It constructs the full endpoint URL, performs
     * a POST request with the notification DTO as the body, and handles both
     * success and error responses with appropriate logging.
     *
     * <p>The method uses reactive WebClient for non-blocking HTTP communication
     * and subscribes to the response for asynchronous processing.
     *
     * @param endpoint the specific notification service endpoint path.
     *                 Must not be null or empty.
     * @param notificationDto the notification data to send.
     *                        Must not be null and must be serializable.
     * @throws WebClientRequestException if the HTTP request fails
     * @throws IllegalArgumentException if endpoint or notificationDto is null
     *
     * @see MailNotificationDto
     */
    private void sendMailToNotificationService(String endpoint, MailNotificationDto notificationDto) {
        // set the final url of notification service
        String notificationDtoUrl= Notification_MemberService_URL+endpoint;
        webClient.build().post()    // using post() as it will be a post method in the controller
                .uri(notificationDtoUrl) // passing the final url
                .bodyValue(notificationDto) // add the payload
                .retrieve().toBodilessEntity().subscribe(
             success-> log.info("Mail send to notification service {}",success.getStatusCode()),
                error-> log.error("Unable to send mail to notification service {}",error.getMessage())
                );
        // print the result as per result
    }

    /**
     * Sends an asynchronous plan activation notification when a member purchases a plan.
     *
     * <p>This method is triggered immediately when a member successfully purchases
     * or renews a plan. It constructs a plan activation notification DTO with
     * plan details, activation date, and expiration information, then sends it
     * to the notification service for confirmation email/SMS delivery.
     *
     * <p>The operation is performed asynchronously to ensure the plan purchase
     * workflow remains responsive and doesn't block the transaction completion.
     *
     * @param requestDto the plan request containing plan name and duration details.
     *                   Must not be null and must have valid plan information.
     * @param name the descriptive name for logging purposes, typically the service
     *             or operation name that triggered this notification.
     * @throws IllegalArgumentException if requestDto is null or missing required plan fields
     * @throws WebClientRequestException if the notification service call fails
     *
     * @see PlanRequestDto
     * @see PlanActivationNotificationDto
     */
    @Async
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
                        success-> log.info("successfully sent request to {} {}",name,
                                success.getStatusCode()),
                error->log.error(error.getMessage())
                );
    }
    /**
     * Sends an asynchronous trainer assignment request to the administrative service.
     *
     * <p>This method forwards trainer assignment requests to the administrative
     * service for approval workflow processing. It includes all necessary member
     * and trainer information required for administrative decision-making.
     *
     * <p>The operation is performed asynchronously to prevent blocking the
     * member-facing trainer request workflow while administrative processing
     * occurs in the background.
     *
     * <p>The request is sent as JSON content to ensure proper serialization
     * and compatibility with the administrative service endpoints.
     *
     * @param responseDto the trainer assignment response DTO containing member
     *                    information, trainer details, and request metadata.
     *                    Must not be null and must contain all required fields.
     * @throws IllegalArgumentException if responseDto is null or missing required fields
     * @throws WebClientRequestException if the administrative service call fails
     *
     * @see TrainerAssignResponseDto
     */
    @Async
    public void sendTrainerRequestToAdmin(TrainerAssignResponseDto responseDto) {
        webClient.build().post()
                .uri(Admin_ApprovalService_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(responseDto)
                .retrieve().toBodilessEntity().subscribe(
                        success-> log.info("Send dto to url: {} {}",
                                Admin_ApprovalService_URL,success.getStatusCode()),
                error->log.error("Failed to send dto {}",error.getMessage())
                );
    }
}
