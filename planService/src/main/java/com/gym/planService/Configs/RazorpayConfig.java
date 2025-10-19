package com.gym.planService.Configs;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RazorpayConfig {

    private final String razorpay_KEY;
    private final String razorpay_SECRET;

    public RazorpayConfig(
            @Value("${razorpay.api.key}") String razorpay_KEY,
            @Value("${razorpay.api.secret}") String razorpay_SECRET) {
        this.razorpay_KEY = razorpay_KEY;
        this.razorpay_SECRET = razorpay_SECRET;
    }

    @Bean
    public RazorpayClient razorpayClient() throws RazorpayException {
        return new RazorpayClient(razorpay_KEY,razorpay_SECRET);
    }
}
