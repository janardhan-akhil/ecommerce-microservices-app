package com.payment_service.Payment.Service.config;


import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

    @Configuration
    @Slf4j
    public class RazorpayConfig {

        @Value("${razorpay.key-id}")
        private String keyId;

        @Value("${razorpay.key-secret}")
        private String keySecret;

        /**
         * RazorpayClient is the entry point for all Razorpay API calls.
         * It is thread-safe and should be a singleton bean.
         */
        @Bean
        public RazorpayClient razorpayClient() throws RazorpayException {
            log.info("Initialising Razorpay client with keyId: {}...", keyId.substring(0, 8));
            return new RazorpayClient(keyId, keySecret);
        }
    }
