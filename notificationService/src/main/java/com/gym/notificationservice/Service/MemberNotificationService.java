package com.gym.notificationservice.Service;

import com.gym.notificationservice.Dto.MemberNotificationRequests.MailNotificationRequestDto;
import com.gym.notificationservice.Dto.MemberNotificationRequests.PlanActivationNotificationRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
/*
 * This service class is responsible to send notifications
 * to member's mail id or to the phone no.
 */
public class MemberNotificationService {
    //adding all the dependencies through constructor
    private final TemplateEngine templateEngine;
    private final MailService mailService;
    private final SmsService smsService;

    /*
     * This method send thyme leaf mail and sms to
     * the member's mail account and phone no when plan left for 3 days
     * using context(thyme leaf) to create context and
     * using template engine it sends email to the member
     * using member service where we used send grid for sms.
     */
    public void sendAlertMail(MailNotificationRequestDto requestDto) {
        // creating a context using a private generic method(defined below)
        Context context = contextBuilder(requestDto);
        // using template engine to stringify the context with html template
        String body = templateEngine.process("plan-alert", context); // "plan-alert" is present
                                                                             // in src/main/resources/templates
        // sending mail by using mail service.
        mailService.sendMail(requestDto.getMailId(), requestDto.getSubject(), body);
        // using date formatter to format the date.
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
        // using string concatenation to write the final message.
        String smsMessage = "Hello " + requestDto.getName() + ", your FIT STUDIO gym plan will expire on "
                + requestDto.getTime().format(formatter) + " (only 3 days left). "
                + "Renew now to avoid account freeze after 10 days of expiry. "
                + "Ignore if already renewed.";
        // sends sms using sms service.
        smsService.sendSms(requestDto.getPhone(), smsMessage);
    }

    /*
     * This method send thyme leaf mail and sms to
     * the member's mail account and phone no when plan is expired
     * using context(thyme leaf) to create context and
     * using template engine it sends email to the member
     * using member service where we used send grid for sms.
     */
    public void sendPlanExpirationMail( MailNotificationRequestDto requestDto) {
        // creating a context using a private generic method(defined below)
        Context context = contextBuilder(requestDto);
        // using template engine to stringify the context with html template
        String body = templateEngine.process("plan-expired",context); // "plan-expired" is present
                                                                              // in src/main/resources/templates
        // sending mail by using mail service.
        mailService.sendMail(requestDto.getMailId(), requestDto.getSubject(), body);
        // using date formatter to format the date.
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
        // using string concatenation to write the final message.
        String smsMessage = "Hello " + requestDto.getName()
                + ", your FIT STUDIO gym plan has expired on "
                + requestDto.getTime().format(formatter) + ". "
                + "Please renew immediately to continue access. "
                + "Your account will be frozen in 10 days if not renewed.";
        // sends sms using sms service.
        smsService.sendSms(requestDto.getPhone(), smsMessage);
    }

    /*
     * This method send thyme leaf mail and sms to
     * the member's mail account and phone no when account is frozen
     * using context(thyme leaf) to create context and
     * using template engine it sends email to the member
     * using member service where we used send grid for sms.
     */
    public void sendFrozenMail(MailNotificationRequestDto requestDto) {
        // creating a context using a private generic method(defined below)
        Context context = contextBuilder(requestDto);
        // using template engine to stringify the context with html template
        String body = templateEngine.process("account-frozen",context); // "account-frozen" is present
                                                                                // in src/main/resources/templates
        // sending mail by using mail service.
        mailService.sendMail(requestDto.getMailId(), requestDto.getSubject(), body);
        // using date formatter to format the date.
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
        // using string concatenation to write the final message.
        String smsMessage = "Hello " + requestDto.getName()
                + ", your FIT STUDIO account has been FROZEN. "
                + "Your plan expired on " + requestDto.getTime().format(formatter)
                + " and was not renewed in 10 days. "
                + "Please renew immediately to reactivate your account.";
        // sends sms using sms service.
        smsService.sendSms(requestDto.getPhone(), smsMessage);
    }

    /*
     * This method send thyme leaf mail and sms to
     * the member's mail account and phone no when member bought a new plan
     * using context(thyme leaf) to create context and
     * using template engine it sends email to the member
     * using member service where we used send grid for sms.
     */
    public void sendPlanUpdateEmail( PlanActivationNotificationRequestDto requestDto) {
        // using date formatter to format the date.
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
        // creating a context
        Context context = new Context();
        // setting all variables present in the html template
        context.setVariable("subject",requestDto.getSubject());
        context.setVariable("name",requestDto.getPlanName());
        context.setVariable("duration",requestDto.getDuration());
        context.setVariable("activationDate", requestDto.getActivationDate().format(formatter));
        context.setVariable("planExpiration",requestDto.getPlanExpiration().format(formatter));
        int activationYear = requestDto.getActivationDate().getYear();
        context.setVariable("activationYear", activationYear);
        // using template engine to stringify the context with html template
        String body = templateEngine.process("plan-renewed",context);
        // sending mail by using mail service.
        mailService.sendMail(requestDto.getMailId(),requestDto.getSubject(),body);
        // using string concatenation to write the final message.
        String smsMessage = "Hello " + requestDto.getPlanName()
                + " plan has been activated! "
                + "Start: " + requestDto.getActivationDate().format(formatter)
                + ", Expiry: " + requestDto.getPlanExpiration().format(formatter)
                + ", Duration: " + requestDto.getDuration() + " days. "
                + "Welcome back to FIT STUDIO 💪";
        // sends sms using sms service.
        smsService.sendSms(requestDto.getPhone(),smsMessage);
    }

    // this is the private method which returns context for sending sms
    private Context contextBuilder(MailNotificationRequestDto requestDto) {
        Context context = new Context();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

        context.setVariable("name",requestDto.getName());
        context.setVariable("subject",requestDto.getSubject());
        context.setVariable("expired",requestDto.getTime().format(formatter));
        context.setVariable("time",requestDto.getTime().format(formatter));

        return context;
    }
}
