package com.gym.planService.Services.OtherServices;

import com.gym.planService.Models.MonthlyRevenue;
import com.gym.planService.Models.PlanPayment;
import com.gym.planService.Repositories.PlanPaymentRepository;
import com.gym.planService.Repositories.RevenueRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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
    @CacheEvict(value = "allRevenue",key = "*")
    public void schedularMonthlyRevenueGenerator() {
        String month = LocalDate.now().getMonth().toString();
        int year = LocalDate.now().getYear();
        String paymentId = month + "::" + year;
        double totalRevenue = 0.0;

        log.info("🧾 [MONTHLY REVENUE SCHEDULER] Triggered for {} {}", month, year);

        List<PlanPayment> currentMonthRevenue =
                paymentRepository.findByCurrentMonthAndYear(month, year);

        if (currentMonthRevenue.isEmpty()) {
            log.warn("⚠️ No payments found for {} {}, skipping revenue generation.", month, year);
            return;
        }

        log.info("✅ Fetched {} successful payments from DB", currentMonthRevenue.size());

        for (PlanPayment payment : currentMonthRevenue) {
            totalRevenue += payment.getPaidPrice();
        }

        // ✅ Build revenue entity
        MonthlyRevenue revenue = MonthlyRevenue.builder()
                .month(LocalDate.now())
                .monthlyRevenue(totalRevenue)
                .currentMonth(month)
                .currentYear(year)
                .build();

        // ✅ Generate elegant PDF receipt
        byte[] pdfReceiptArray = receiptGenerator.generateMonthlyReviewReceipt(month, year, totalRevenue);
        log.info("📄 Generated revenue report PDF of size {} bytes", pdfReceiptArray.length);

        // ✅ Upload to AWS S3
        String receiptUrl = awsService.uploadPaymentReceipt(pdfReceiptArray, paymentId);
        log.info("☁️ Receipt uploaded to AWS S3: {}", receiptUrl);

        revenue.setReceiptUrl(receiptUrl);
        revenueRepository.save(revenue);

        log.info("💾 Monthly revenue record saved for {} {}", month, year);
        log.info("🎯 Scheduler completed successfully at {}", LocalDateTime.now());
    }


    /**
     * Scheduled job to generate the yearly revenue matrices and store the receipt.
     * Runs automatically at year-end and summarizes monthly revenues with % change.
     */
    @Transactional
    @Scheduled(cron = "0 0 0 1 1 *")
    public void schedularYearlyMatricesGenerator() {
        LocalDate currentDate = LocalDate.now();
        Integer year = currentDate.getYear();

        log.info("🕒 [SCHEDULER] Starting yearly revenue generation for year: {}", year);

        List<MonthlyRevenue> currentYearPayments = revenueRepository.findByCurrentYear(year);
        if (currentYearPayments.isEmpty()) {
            log.warn("⚠️ [SCHEDULER] No monthly revenue records found for year: {}", year);
            return;
        }

        log.info("✅ [SCHEDULER] Retrieved {} monthly revenue records from DB for year: {}",
                currentYearPayments.size(), year);

        Double income = 0.00;
        Map<String, Double> revenueMapper = new LinkedHashMap<>(currentYearPayments.size());

        for (MonthlyRevenue revenue : currentYearPayments) {
            Double increment = revenueIncrement(income, revenue.getMonthlyRevenue());
            log.debug("📈 Processing month={} | revenue={} | change={}% ",
                    revenue.getCurrentMonth(), revenue.getMonthlyRevenue(), String.format("%.2f", increment));

            income = revenue.getMonthlyRevenue();
            revenueMapper.put(revenue.getCurrentMonth() + "::" + income, increment);
        }

        log.info("🧾 Generating yearly revenue receipt PDF for year: {}", year);
        byte[] pdfArray = receiptGenerator.generateYearlyReceipt(revenueMapper);

        log.info("📤 Uploading generated yearly receipt to AWS S3...");
        String url = awsService.uploadPaymentReceipt(pdfArray, year.toString());
        log.info("✅ Yearly receipt uploaded successfully: {}", url);

        log.info("📧 Sending yearly revenue report attachment via WebClient...");
        webClientService.sendReviewAttachment(pdfArray);

        log.info("🎉 [SCHEDULER SUCCESS] Yearly revenue generation completed for year: {}", year);
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
