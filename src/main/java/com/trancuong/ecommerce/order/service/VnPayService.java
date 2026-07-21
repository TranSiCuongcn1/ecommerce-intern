package com.trancuong.ecommerce.order.service;

import com.trancuong.ecommerce.config.VnPayProperties;
import com.trancuong.ecommerce.order.domain.Order;
import com.trancuong.ecommerce.order.domain.Payment;
import com.trancuong.ecommerce.order.dto.VnPayInitiateResponse;
import com.trancuong.ecommerce.order.dto.VnPayPaymentInitiateRequest;
import com.trancuong.ecommerce.order.exception.OrderNotFoundException;
import com.trancuong.ecommerce.order.repository.OrderRepository;
import com.trancuong.ecommerce.order.repository.PaymentRepository;
import com.trancuong.ecommerce.order.util.VnPayCryptoUtil;
import com.trancuong.ecommerce.user.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class VnPayService {

    private static final String PROVIDER = "VNPAY";
    private static final String SUCCESS_CODE = "00";

    private final VnPayProperties vnPayProperties;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public VnPayInitiateResponse initiatePayment(
            VnPayPaymentInitiateRequest request,
            User user,
            HttpServletRequest httpServletRequest
    ) {
        Order order = orderRepository.findByIdAndUserId(request.orderId(), user.getId())
                .orElseThrow(() -> new OrderNotFoundException(request.orderId()));

        if (!"PENDING".equalsIgnoreCase(order.getStatus())) {
            throw new IllegalStateException("Only PENDING orders can be paid");
        }

        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElseGet(() -> paymentRepository.save(new Payment(
                        order,
                        order.getTotalAmount(),
                        PROVIDER,
                        "PENDING"
                )));
        payment.prepareForProvider(PROVIDER, order.getTotalAmount());
        order.updatePaymentMethod(PROVIDER);

        long amount = order.getTotalAmount().longValue();
        String orderIdStr = order.getId().toString();
        String orderInfo = "Thanh toan don hang " + orderIdStr;
        String returnUrl = request.returnUrl() != null && !request.returnUrl().isBlank()
                ? request.returnUrl().trim()
                : vnPayProperties.returnUrl();

        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", vnPayProperties.tmnCode());
        params.put("vnp_Amount", String.valueOf(amount * 100));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", orderIdStr);
        params.put("vnp_OrderInfo", orderInfo);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", getIpAddress(httpServletRequest));

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(calendar.getTimeZone());
        params.put("vnp_CreateDate", formatter.format(calendar.getTime()));

        calendar.add(Calendar.MINUTE, 15);
        params.put("vnp_ExpireDate", formatter.format(calendar.getTime()));

        String hashData = buildHashData(params);
        String secureHash = VnPayCryptoUtil.signHmacSHA512(hashData, vnPayProperties.hashSecret());
        String paymentUrl = vnPayProperties.payUrl() + "?" + buildQuery(params) + "&vnp_SecureHash=" + secureHash;

        log.info("Created VNPay payment URL for order: {}", orderIdStr);

        return new VnPayInitiateResponse(paymentUrl);
    }

    @Transactional
    public void processIpn(Map<String, String> params) {
        log.info("Received VNPay IPN with params: {}", params);
        processVerifiedCallback(params);
    }

    @Transactional
    public void processReturn(Map<String, String> params) {
        log.info("Received VNPay return with params: {}", params);
        processVerifiedCallback(params);
    }

    private void processVerifiedCallback(Map<String, String> params) {
        Map<String, String> verifiedParams = verifySignature(params);
        UUID orderId = parseOrderId(verifiedParams.get("vnp_TxnRef"));
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        String vnpAmount = verifiedParams.get("vnp_Amount");
        if (order.getTotalAmount().longValue() * 100 != Long.parseLong(vnpAmount)) {
            log.error("VNPay amount mismatch. Order amount: {}, VNPay amount: {}", order.getTotalAmount(), vnpAmount);
            throw new IllegalArgumentException("Amount mismatch");
        }

        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new IllegalStateException("Payment record not found for order: " + order.getId()));

        if (!"PENDING".equalsIgnoreCase(payment.getStatus())) {
            return;
        }

        String responseCode = verifiedParams.get("vnp_ResponseCode");
        String transactionStatus = verifiedParams.get("vnp_TransactionStatus");
        if (SUCCESS_CODE.equals(responseCode) && SUCCESS_CODE.equals(transactionStatus)) {
            log.info("VNPay payment successful for order: {}", order.getId());
            payment.markPaid(verifiedParams.get("vnp_TransactionNo"), LocalDateTime.now());
            order.updatePaymentStatus("PAID");
        } else {
            log.warn(
                    "VNPay payment failed for order: {}, responseCode: {}, transactionStatus: {}",
                    order.getId(),
                    responseCode,
                    transactionStatus
            );
            payment.markFailed("Response code: " + responseCode + ", transaction status: " + transactionStatus);
            order.updatePaymentStatus("FAILED");
        }

        paymentRepository.save(payment);
        orderRepository.save(order);
    }

    private Map<String, String> verifySignature(Map<String, String> params) {
        String secureHash = params.get("vnp_SecureHash");
        if (secureHash == null || secureHash.isBlank()) {
            log.error("Missing VNPay signature");
            throw new IllegalArgumentException("Missing signature");
        }

        Map<String, String> signedParams = new HashMap<>(params);
        signedParams.remove("vnp_SecureHash");
        signedParams.remove("vnp_SecureHashType");

        String expectedSignature = VnPayCryptoUtil.signHmacSHA512(buildHashData(signedParams), vnPayProperties.hashSecret());
        if (!expectedSignature.equalsIgnoreCase(secureHash)) {
            log.error("Invalid VNPay signature");
            throw new IllegalArgumentException("Invalid signature");
        }
        return signedParams;
    }

    private UUID parseOrderId(String orderIdStr) {
        try {
            return UUID.fromString(orderIdStr);
        } catch (IllegalArgumentException e) {
            log.error("Invalid VNPay order ID format: {}", orderIdStr);
            throw new IllegalArgumentException("Invalid Order ID");
        }
    }

    private String buildHashData(Map<String, String> params) {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = params.get(fieldName);
            if (fieldValue == null || fieldValue.isEmpty()) {
                continue;
            }
            if (!hashData.isEmpty()) {
                hashData.append('&');
            }
            hashData.append(fieldName)
                    .append('=')
                    .append(urlEncode(fieldValue));
        }
        return hashData.toString();
    }

    private String buildQuery(Map<String, String> params) {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder query = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = params.get(fieldName);
            if (fieldValue == null || fieldValue.isEmpty()) {
                continue;
            }
            if (!query.isEmpty()) {
                query.append('&');
            }
            query.append(urlEncode(fieldName))
                    .append('=')
                    .append(urlEncode(fieldValue));
        }
        return query.toString();
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.US_ASCII);
    }

    private String getIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null || ipAddress.isBlank()) {
            ipAddress = request.getRemoteAddr();
        }
        if (ipAddress != null && ipAddress.contains(",")) {
            return ipAddress.split(",")[0].trim();
        }
        return ipAddress;
    }
}
