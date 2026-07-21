package com.trancuong.ecommerce.order.controller;

import com.trancuong.ecommerce.common.api.ApiResponse;
import com.trancuong.ecommerce.order.dto.VnPayInitiateResponse;
import com.trancuong.ecommerce.order.dto.VnPayPaymentInitiateRequest;
import com.trancuong.ecommerce.order.service.VnPayService;
import com.trancuong.ecommerce.user.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class VnPayPaymentController {

    private final VnPayService vnPayService;

    @PostMapping("/vnpay/initiate")
    public ApiResponse<VnPayInitiateResponse> initiateVnPayPayment(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody VnPayPaymentInitiateRequest request,
            HttpServletRequest httpServletRequest
    ) {
        VnPayInitiateResponse response = vnPayService.initiatePayment(request, user, httpServletRequest);
        return ApiResponse.success(HttpStatus.OK.value(), "Payment initiated successfully", response);
    }

    @GetMapping("/vnpay-ipn")
    public ResponseEntity<String> handleVnPayIpn(@RequestParam Map<String, String> params) {
        try {
            vnPayService.processIpn(params);
            return ResponseEntity.ok("{\"RspCode\":\"00\",\"Message\":\"Confirm Success\"}");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok("{\"RspCode\":\"97\",\"Message\":\"Invalid Checksum\"}");
        } catch (Exception e) {
            return ResponseEntity.ok("{\"RspCode\":\"99\",\"Message\":\"Unknown Error\"}");
        }
    }

    @GetMapping("/vnpay/redirect")
    public ResponseEntity<String> handleVnPayRedirect(@RequestParam Map<String, String> params) {
        try {
            vnPayService.processReturn(params);
        } catch (Exception e) {
            // The redirect page is informational; IPN remains the source of truth for automated confirmation.
        }

        String responseCode = params.get("vnp_ResponseCode");
        String transactionStatus = params.get("vnp_TransactionStatus");
        String orderId = params.get("vnp_TxnRef");

        String htmlResponse;
        if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
            htmlResponse = "<html><body style='font-family: Arial, sans-serif; text-align: center; margin-top: 50px;'>" +
                    "<h2 style='color: #2e7d32;'>VNPay payment completed</h2>" +
                    "<p>Order: <strong>" + orderId + "</strong></p>" +
                    "<p>You can close this tab and check the order status in Swagger.</p>" +
                    "</body></html>";
        } else {
            htmlResponse = "<html><body style='font-family: Arial, sans-serif; text-align: center; margin-top: 50px;'>" +
                    "<h2 style='color: #c62828;'>VNPay payment failed</h2>" +
                    "<p>Order: <strong>" + orderId + "</strong></p>" +
                    "<p>Response code: " + responseCode + "</p>" +
                    "<p>Transaction status: " + transactionStatus + "</p>" +
                    "</body></html>";
        }
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(htmlResponse);
    }
}
