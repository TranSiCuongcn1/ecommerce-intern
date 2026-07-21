package com.trancuong.ecommerce.order.controller;

import com.trancuong.ecommerce.common.api.ApiResponse;
import com.trancuong.ecommerce.order.dto.MomoIpnRequest;
import com.trancuong.ecommerce.order.dto.MomoInitiateResponse;
import com.trancuong.ecommerce.order.dto.MomoPaymentInitiateRequest;
import com.trancuong.ecommerce.order.service.MomoService;
import com.trancuong.ecommerce.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class MomoPaymentController {

    private final MomoService momoService;

    @PostMapping("/momo/initiate")
    public ApiResponse<MomoInitiateResponse> initiateMomoPayment(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody MomoPaymentInitiateRequest request
    ) {
        MomoInitiateResponse response = momoService.initiatePayment(request, user);
        return ApiResponse.success(HttpStatus.OK.value(), "Payment initiated successfully", response);
    }

    @PostMapping("/momo-ipn")
    public ResponseEntity<Void> handleMomoIpn(@RequestBody MomoIpnRequest request) {
        momoService.processIpn(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/momo/redirect")
    public ResponseEntity<String> handleMomoRedirect(
            @RequestParam(name = "resultCode") Integer resultCode,
            @RequestParam(name = "message") String message,
            @RequestParam(name = "orderId") String orderId
    ) {
        String htmlResponse;
        if (resultCode == 0) {
            htmlResponse = "<html><body style='font-family: Arial, sans-serif; text-align: center; margin-top: 50px;'>" +
                    "<h2 style='color: #2e7d32;'>MoMo payment completed</h2>" +
                    "<p>Order: <strong>" + orderId + "</strong></p>" +
                    "<p>The final order status is updated by the signed MoMo IPN callback.</p>" +
                    "</body></html>";
        } else {
            htmlResponse = "<html><body style='font-family: Arial, sans-serif; text-align: center; margin-top: 50px;'>" +
                    "<h2 style='color: #c62828;'>MoMo payment failed</h2>" +
                    "<p>Order: <strong>" + orderId + "</strong></p>" +
                    "<p>Reason: " + message + "</p>" +
                    "</body></html>";
        }
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(htmlResponse);
    }
}
