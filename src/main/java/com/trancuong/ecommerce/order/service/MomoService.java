package com.trancuong.ecommerce.order.service;

import com.trancuong.ecommerce.config.MomoProperties;
import com.trancuong.ecommerce.order.domain.Order;
import com.trancuong.ecommerce.order.domain.Payment;
import com.trancuong.ecommerce.order.dto.MomoIpnRequest;
import com.trancuong.ecommerce.order.dto.MomoInitiateResponse;
import com.trancuong.ecommerce.order.dto.MomoPaymentInitiateRequest;
import com.trancuong.ecommerce.order.exception.OrderNotFoundException;
import com.trancuong.ecommerce.order.repository.OrderRepository;
import com.trancuong.ecommerce.order.repository.PaymentRepository;
import com.trancuong.ecommerce.order.util.MomoCryptoUtil;
import com.trancuong.ecommerce.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class MomoService {

    private final MomoProperties momoProperties;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final RestClient restClient;

    public MomoService(
            MomoProperties momoProperties,
            OrderRepository orderRepository,
            PaymentRepository paymentRepository,
            RestClient.Builder restClientBuilder
    ) {
        this.momoProperties = momoProperties;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.restClient = restClientBuilder.build();
    }

    @Transactional
    public MomoInitiateResponse initiatePayment(MomoPaymentInitiateRequest request, User user) {
        Order order = orderRepository.findByIdAndUserId(request.orderId(), user.getId())
                .orElseThrow(() -> new OrderNotFoundException(request.orderId()));

        if (!"PENDING".equalsIgnoreCase(order.getStatus())) {
            throw new IllegalStateException("Only PENDING orders can be paid");
        }

        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElseGet(() -> {
                    Payment newPayment = new Payment(
                            order,
                            order.getTotalAmount(),
                            "MOMO",
                            "PENDING"
                    );
                    return paymentRepository.save(newPayment);
                });
        payment.prepareForProvider("MOMO", order.getTotalAmount());
        order.updatePaymentMethod("MOMO");

        long amount = order.getTotalAmount().longValue();
        String requestId = UUID.randomUUID().toString();
        String orderIdStr = order.getId().toString();
        String orderInfo = "Thanh toan don hang " + orderIdStr;
        String extraData = "";
        String requestType = "captureWallet";

        String redirectUrl = request.redirectUrl() != null && !request.redirectUrl().isBlank()
                ? request.redirectUrl().trim()
                : momoProperties.redirectUrl();

        // accessKey=$accessKey&amount=$amount&extraData=$extraData&ipnUrl=$ipnUrl&orderId=$orderId&orderInfo=$orderInfo&partnerCode=$partnerCode&redirectUrl=$redirectUrl&requestId=$requestId&requestType=$requestType
        String rawSignature = "accessKey=" + momoProperties.accessKey() +
                "&amount=" + amount +
                "&extraData=" + extraData +
                "&ipnUrl=" + momoProperties.ipnUrl() +
                "&orderId=" + orderIdStr +
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + momoProperties.partnerCode() +
                "&redirectUrl=" + redirectUrl +
                "&requestId=" + requestId +
                "&requestType=" + requestType;

        String signature = MomoCryptoUtil.signHmacSHA256(rawSignature, momoProperties.secretKey());

        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("partnerCode", momoProperties.partnerCode());
        requestPayload.put("partnerName", "Ecommerce Monolith");
        requestPayload.put("storeId", "Main Store");
        requestPayload.put("requestId", requestId);
        requestPayload.put("amount", amount);
        requestPayload.put("orderId", orderIdStr);
        requestPayload.put("orderInfo", orderInfo);
        requestPayload.put("redirectUrl", redirectUrl);
        requestPayload.put("ipnUrl", momoProperties.ipnUrl());
        requestPayload.put("requestType", requestType);
        requestPayload.put("extraData", extraData);
        requestPayload.put("lang", "vi");
        requestPayload.put("signature", signature);

        log.info("Sending payment request to MoMo for order: {}", orderIdStr);

        try {
            @SuppressWarnings("rawtypes")
            Map response = restClient.post()
                    .uri(momoProperties.apiUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestPayload)
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                throw new RuntimeException("Empty response from MoMo Gateway");
            }

            Integer resultCode = (Integer) response.get("resultCode");
            String message = (String) response.get("message");

            if (resultCode == null || resultCode != 0) {
                log.error("MoMo payment initiation failed: {}", message);
                throw new RuntimeException("MoMo gateway error: " + message);
            }

            String payUrl = (String) response.get("payUrl");
            String qrCodeUrl = (String) response.get("qrCodeUrl");
            String deeplink = (String) response.get("deeplink");

            return new MomoInitiateResponse(payUrl, qrCodeUrl, deeplink);

        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            log.error("Error calling MoMo API. HTTP Status: {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new RuntimeException("Failed to contact MoMo payment gateway: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Error calling MoMo API", e);
            throw new RuntimeException("Failed to contact MoMo payment gateway: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void processIpn(MomoIpnRequest ipn) {
        log.info("Received MoMo IPN callback for order ID: {}", ipn.orderId());

        // Validate signature
        // accessKey=$accessKey&amount=$amount&extraData=$extraData&message=$message&orderId=$orderId&orderInfo=$orderInfo&orderType=$orderType&partnerCode=$partnerCode&payType=$payType&requestId=$requestId&responseTime=$responseTime&resultCode=$resultCode&transId=$transId
        String extraData = ipn.extraData() == null ? "" : ipn.extraData();
        String rawSignature = "accessKey=" + momoProperties.accessKey() +
                "&amount=" + ipn.amount() +
                "&extraData=" + extraData +
                "&message=" + ipn.message() +
                "&orderId=" + ipn.orderId() +
                "&orderInfo=" + ipn.orderInfo() +
                "&orderType=" + ipn.orderType() +
                "&partnerCode=" + ipn.partnerCode() +
                "&payType=" + ipn.payType() +
                "&requestId=" + ipn.requestId() +
                "&responseTime=" + ipn.responseTime() +
                "&resultCode=" + ipn.resultCode() +
                "&transId=" + ipn.transId();

        String expectedSignature = MomoCryptoUtil.signHmacSHA256(rawSignature, momoProperties.secretKey());

        if (!expectedSignature.equals(ipn.signature())) {
            log.error("Invalid MoMo IPN signature!");
            throw new IllegalArgumentException("Invalid signature");
        }

        UUID orderId = UUID.fromString(ipn.orderId());
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // Check if amount matches
        if (order.getTotalAmount().longValue() != ipn.amount()) {
            log.error("MoMo IPN amount mismatch. Order amount: {}, IPN amount: {}", order.getTotalAmount(), ipn.amount());
            throw new IllegalArgumentException("Amount mismatch");
        }

        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new IllegalStateException("Payment record not found for order: " + order.getId()));

        if (ipn.resultCode() == 0) {
            log.info("MoMo IPN: Payment successful for order: {}", order.getId());
            payment.markPaid(ipn.transId().toString(), LocalDateTime.now());
            order.updatePaymentStatus("PAID");
        } else {
            log.warn("MoMo IPN: Payment failed for order: {}, code: {}, message: {}", order.getId(), ipn.resultCode(), ipn.message());
            payment.markFailed(ipn.message());
            order.updatePaymentStatus("FAILED");
        }

        paymentRepository.save(payment);
        orderRepository.save(order);
    }
}
