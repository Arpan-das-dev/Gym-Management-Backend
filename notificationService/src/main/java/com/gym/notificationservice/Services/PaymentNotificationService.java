package com.gym.notificationservice.Services;

import com.gym.notificationservice.Dto.PaymentNotificationDtos.Requests.PlanNotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentNotificationService {

    private final MailjetService mailjetService;
    private final TemplateEngine templateEngine;

    public String sendAttachedMail(PlanNotificationRequest request, MultipartFile attachment) {
        long start = System.currentTimeMillis();
        log.info("PaymentNotificationService :: Request reached service class ");
        Context context = new Context();

        String subject = request.getUserName()+"Plan Purchase Receipt";
        context.setVariable("subject",subject );

        context.setVariable("userName", request.getUserName());
        context.setVariable("userMail", request.getUserMail());
        context.setVariable("planName", request.getPlanName());
        context.setVariable("planDuration", request.getPlanDuration());
        context.setVariable("planPrice", request.getPlanPrice());

        context.setVariable("purchaseDate", LocalDate.now().toString());
        context.setVariable("expirationDate",
                LocalDate.now().plusMonths(request.getPlanDuration()).toString());

        context.setVariable("year", LocalDate.now().getYear());
        String body = templateEngine.process("plan-payment",context);
        mailjetService.sendMailWithAttachment(request.getUserMail(), subject,body,attachment);
        log.info("PaymentNotificationService :: completed request in {} ms",System.currentTimeMillis()-start);
        return "Mail sent successfully to user "+request.getUserName();
    }
}
