package com.gym.planService.Services.OtherServices;

import com.gym.planService.Models.MonthlyRevenue;
import com.gym.planService.Models.PlanPayment;
import com.gym.planService.Repositories.PlanPaymentRepository;
import com.gym.planService.Repositories.RevenueRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final RevenueRepository revenueRepository;
    private final PlanPaymentRepository paymentRepository;
    private final AwsService awsService;
    private final ReceiptGenerator receiptGenerator;
    private final WebClientService webClientService;

    @Transactional
    @Scheduled(cron = "0 59 23 L * ?")
    @CacheEvict(value = "allRevenue", allEntries = true)
    public void schedularMonthlyRevenueGenerator() {
        long startTime = System.currentTimeMillis();
        String month = LocalDate.now().getMonth().toString();
        int year = LocalDate.now().getYear();
        String paymentIdKey = month + "::" + year;

        log.info("SCHEDULER_START Triggered for {} {}", month, year);
        log.info("DB_FETCH_INIT Fetching payment records from database");

        List<PlanPayment> currentMonthRevenue =
                paymentRepository.findByCurrentMonthAndYear(month, year);

        if (currentMonthRevenue.isEmpty()) {
            log.warn("DB_FETCH_WARN No payments found for {} {}, terminating scheduler run", month, year);
            log.info("SCHEDULER_END Total time: {} ms", System.currentTimeMillis() - startTime);
            return;
        }

        log.info("DB_FETCH_SUCCESS Fetched {} successful payments", currentMonthRevenue.size());

        double totalRevenue = currentMonthRevenue.parallelStream()
                .mapToDouble(PlanPayment::getPaidPrice)
                .sum();

        log.info("REVENUE_CALC_DONE Total calculated revenue: {}", totalRevenue);

        MonthlyRevenue revenue = MonthlyRevenue.builder()
                .month(LocalDate.now())
                .monthlyRevenue(totalRevenue)
                .currentMonth(month)
                .currentYear(year)
                .build();

        log.info("ASYNC_FLOW_INIT Starting PDF generation and S3 upload in background");

        try {
            String finalReceiptUrl = Mono.fromFuture(
                            receiptGenerator.generateMonthlyReviewReceipt(month, year, totalRevenue)
                    )
                    .doOnNext(byteArray -> log.debug("PDF_GEN_COMPLETE Generated PDF successfully of size {}", byteArray.length))
                    .flatMap(byteArray -> {
                        log.debug("S3_UPLOAD_INIT Starting upload to S3 for payment key: {}", paymentIdKey);
                        return Mono.fromFuture(awsService.uploadPaymentReceipt(byteArray, paymentIdKey));
                    })
                    .doOnSuccess(suc -> log.info("S3_UPLOAD_SUCCESS Uploaded receipt to URL: {}", suc))
                    .doOnError(err -> log.error("S3_UPLOAD_FAIL Failed to upload receipt: {}", err.getMessage()))
                    .block();

            if (finalReceiptUrl != null) {
                revenue.setReceiptUrl(finalReceiptUrl);
                log.info("DB_UPDATE_URL Receipt URL successfully assigned to revenue entity");
            } else {
                log.warn("DB_UPDATE_WARN Receipt URL was null, saving record without URL");
            }
            revenueRepository.save(revenue);
            log.info("DB_SAVE_SUCCESS Monthly revenue record saved to database");

        } catch (Exception e) {
            log.error("ASYNC_FLOW_FATAL A critical error occurred during asynchronous process: {}", e.getMessage());
            log.error("ASYNC_FLOW_FALLBACK Saving revenue entity without receipt URL due to previous failure");
            revenueRepository.save(revenue);
        }

        log.info("SCHEDULER_END Completed successfully at {}", LocalDateTime.now());
        log.info("SCHEDULER_END Total time: {} ms", System.currentTimeMillis() - startTime);

    }


    /**
     * Scheduled job to generate the yearly revenue matrices and store the receipt.
     * Runs automatically at year-end and summarizes monthly revenues with % change.
     */
    @Transactional
    @Scheduled(cron = "0 0 0 1 1 *")
//    @Scheduled(cron = "0 8 19 * * ?")
    public void schedularYearlyMatricesGenerator() {
        long start = System.currentTimeMillis();
        LocalDate currentDate = LocalDate.now();
        Integer year = currentDate.getYear();

        log.info("SCHEDULER_START Starting yearly revenue generation for year: {}", year);
        log.info("DB_FETCH_INIT Fetching monthly revenue records for year: {}", year);

        List<MonthlyRevenue> currentYearPayments = revenueRepository.findByCurrentYear(year);

        if (currentYearPayments.isEmpty()) {
            log.warn("DB_FETCH_WARN No monthly revenue records found for year: {}", year);
            log.info("SCHEDULER_END took Total time: {} ms", System.currentTimeMillis() - start);
            return;
        }

        log.info("DB_FETCH_SUCCESS Retrieved {} monthly revenue records", currentYearPayments.size());

        Double income = 0.00;
        Map<String, Double> revenueMapper = new LinkedHashMap<>(currentYearPayments.size());

        for (MonthlyRevenue revenue : currentYearPayments) {
            // Sequential calculation necessary here
            Double increment = revenueIncrement(income, revenue.getMonthlyRevenue());
            log.debug("CALC_DETAIL Processing month={} | revenue={} | change={}%",
                    revenue.getCurrentMonth(), revenue.getMonthlyRevenue(), String.format("%.2f", increment));

            income = revenue.getMonthlyRevenue();
            revenueMapper.put(revenue.getCurrentMonth() + "::" + income, increment);
        }

        log.info("ASYNC_FLOW_INIT Starting PDF generation, S3 upload, and email in background");

        try {
            // 1. Generate PDF (Async)
            Mono.fromFuture(receiptGenerator.generateYearlyReceipt(revenueMapper))
                    .doOnNext(pdfBytes -> log.debug("PDF_GEN_COMPLETE Generated yearly PDF successfully of size {}", pdfBytes.length))
                    .flatMap(pdfArray -> {
                        // 2. Upload to S3 (Async)
                        Mono<String> uploadMono = Mono.fromFuture(awsService.uploadPaymentReceipt(pdfArray, year.toString()))
                                .doOnSuccess(url -> log.info("S3_UPLOAD_SUCCESS Yearly receipt uploaded successfully to URL: {}", url))
                                .doOnError(err -> log.error("S3_UPLOAD_FAIL Failed to upload yearly receipt: {}", err.getMessage()));

                        // 3. Chain PDF array and URL for WebClient call
                        return Mono.zip(Mono.just(pdfArray), uploadMono);
                    })
                    .flatMap(tuple -> {
                        byte[] pdfArray = tuple.getT1();
                        String receiptUrl = tuple.getT2();

                        // 4. Send Email (Reactive Fire-and-Forget)
                        log.info("EMAIL_SEND_INIT Sending yearly revenue report attachment via WebClient");
                        Mono<String> emailMono = Mono.fromFuture( webClientService.sendReviewAttachment(pdfArray))
                                .doOnSuccess(v -> log.info("EMAIL_SEND_SUCCESS Yearly revenue report email initiated successfully"))
                                .doOnError(err -> log.error("EMAIL_SEND_FAIL WebClient email sending failed: {}", err.getMessage()));

                        // Wait for email initiation to be done, returning the URL as the final value
                        return emailMono.thenReturn(receiptUrl);
                    })
                    .block(); // CRITICAL: Wait for the entire async chain to complete

            log.info("ASYNC_FLOW_SUCCESS All asynchronous processes completed successfully");

        } catch (Exception e) {
            log.error("ASYNC_FLOW_FATAL A critical error occurred during yearly asynchronous process: {}", e.getMessage(), e);
        }

        log.info("SCHEDULER_END Yearly revenue generation completed for year: {}", year);
        log.info("SCHEDULER_END Total  in time: {} ms", System.currentTimeMillis() - start);
    }

    /**
     * Calculates the percentage change between two consecutive months' revenue.
     */
    private Double revenueIncrement(Double previousIncome, Double currentRevenue) {
        if (previousIncome == 0.00) return 0.00;
        Double difference = currentRevenue - previousIncome;
        Double percentage = (difference / previousIncome) * 100;
        log.trace("Calculated revenue change: previous={} | current={} | percent={}% ",
                previousIncome, currentRevenue, String.format("%.2f", percentage));
        return percentage;
    }
}
