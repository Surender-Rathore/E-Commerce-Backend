package com.jtspringproject.JtSpringProject.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.jtspringproject.JtSpringProject.models.User;
import com.jtspringproject.JtSpringProject.services.orderService;
import com.jtspringproject.JtSpringProject.services.userService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

@RestController
public class PaymentController {

    private final orderService orderService;
    private final userService userService;

    @Value("${stripe.currency:usd}")
    private String currency;

    @Autowired
    public PaymentController(orderService orderService, userService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    @PostMapping("/api/payment/create-intent")
    @ResponseBody
    public ResponseEntity<?> createPaymentIntent() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByUsername(username);

        int total = orderService.calculateTotal(user);
        if (total <= 0) {
            return ResponseEntity.badRequest().body("Cart is empty");
        }

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(total * 100L) // Stripe amount smallest unit (cents) me leta hai
                    .setCurrency(currency)
                    .addPaymentMethodType("card")
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);

            Map<String, Object> response = new HashMap<>();
            response.put("clientSecret", intent.getClientSecret());
            response.put("amount", total);
            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            return ResponseEntity.status(500).body("Could not create payment");
        }
    }
}