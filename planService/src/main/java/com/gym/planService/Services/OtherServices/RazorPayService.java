package com.gym.planService.Services.OtherServices;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Refund;
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
        orderRequest.put("amount", amount*100);
        orderRequest.put("currency", currency);
        orderRequest.put("receipt", receipt);
        log.info("price::===> {}",orderRequest.get("amount"));
        return razorpayClient.orders.create(orderRequest);
    }

    public String refundPayment(String paymentId, double amount, String reason) throws RazorpayException {
        JSONObject refundRequest = new JSONObject();
        refundRequest.put("amount", (int)(amount * 100)); // amount in paise
        refundRequest.put("speed", "normal");
        refundRequest.put("notes", new JSONObject().put("reason", reason));

        Refund refund = razorpayClient.payments.refund(paymentId, refundRequest);
        log.info("Refund initiated for paymentId: {} | RefundId: {}", paymentId, refund.get("id"));
        return refund.get("id");
    }
}
