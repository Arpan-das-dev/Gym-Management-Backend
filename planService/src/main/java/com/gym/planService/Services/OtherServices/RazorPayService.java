package com.gym.planService.Services.PaymentService;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RazorPayService {

    private final RazorpayClient razorpayClient;

    public Order makePayment(Long amount, String currency,String receipt) throws RazorpayException {
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount);
        orderRequest.put("currency", currency);
        orderRequest.put("receipt", receipt);

        return razorpayClient.orders.create(orderRequest);
    }
}
