package com.gym.notificationservice.Services;

import com.gym.notificationservice.Dto.PaymentNotificationDtos.Requests.PaymentFailedDto;
import com.gym.notificationservice.Dto.PaymentNotificationDtos.Requests.PlanNotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

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

    public String sendFailedPaymentMail(PaymentFailedDto failedDto) {
        long start = System.currentTimeMillis();
        if(failedDto.getEmailId().contains("@example")){
            return "Send mail To fake mail id";
        }
        log.info("®️®️ PaymentNotificationService :: Request reached service class to send failed payment mail");
        Context context = new Context();
        context.setVariable("userName",failedDto.getUserName());
        context.setVariable("paymentId",failedDto.getPaymentId());
        context.setVariable("amount",failedDto.getAmount());

        String body = templateEngine.process("payment-failed",context);
        CompletableFuture<String> response = mailjetService
                .sendMail(failedDto.getEmailId(),failedDto.getSubject(),body);
        Mono.fromFuture(response)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(
                        s -> log.info("Async mail success: {}", s),
                        e -> log.error("Async mail failed: {}", e.getMessage())
                );
        log.info("PaymentNotificationService -> completed request in {} ms",System.currentTimeMillis()-start);
        return "Successfully send Mail to "+failedDto.getEmailId();
    }

    public String sendRefundFailedMail(PaymentFailedDto failedDto) {
        long start = System.currentTimeMillis();
        if(failedDto.getEmailId().contains("@example")){
            return "Send mail To fake mail id";
        }
        log.info("®️®️ PaymentNotificationService :: Request reached service class to send failed refund mail");
        Context context = new Context();
        context.setVariable("userName",failedDto.getUserName());
        context.setVariable("paymentId",failedDto.getPaymentId());
        context.setVariable("amount",failedDto.getAmount());

        String body = templateEngine.process("refund-failed",context);
        CompletableFuture<String> response = mailjetService
                .sendMail(failedDto.getEmailId(),failedDto.getSubject(),body);
        Mono.fromFuture(response)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(
                        s -> log.info("Async mail success : {}", s),
                        e -> log.error("Async mail failed : {}", e.getMessage())
                );
        log.info("line 92::PaymentNotificationService -> completed request in {} ms",System.currentTimeMillis()-start);
        return "Successfully send Mail to "+failedDto.getEmailId();
    }

}
