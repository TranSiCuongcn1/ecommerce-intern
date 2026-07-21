package com.trancuong.ecommerce.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trancuong.ecommerce.config.MomoProperties;
import com.trancuong.ecommerce.order.domain.Order;
import com.trancuong.ecommerce.order.domain.Payment;
import com.trancuong.ecommerce.order.dto.MomoIpnRequest;
import com.trancuong.ecommerce.order.dto.MomoInitiateResponse;
import com.trancuong.ecommerce.order.dto.MomoPaymentInitiateRequest;
import com.trancuong.ecommerce.order.repository.OrderRepository;
import com.trancuong.ecommerce.order.repository.PaymentRepository;
import com.trancuong.ecommerce.order.util.MomoCryptoUtil;
import com.trancuong.ecommerce.user.domain.Role;
import com.trancuong.ecommerce.user.domain.User;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class MomoServiceTest {

    @Mock
    private MomoProperties momoProperties;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RestClient.Builder restClientBuilder;

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private MomoService momoService;

    @BeforeEach
    void setUp() {
        when(restClientBuilder.build()).thenReturn(restClient);
        momoService = new MomoService(momoProperties, orderRepository, paymentRepository, restClientBuilder);
    }

    @Test
    void initiatePayment_success() {
        User user = user();
        Order order = order(user, "PENDING");
        MomoPaymentInitiateRequest request = new MomoPaymentInitiateRequest(order.getId(), null);

        when(orderRepository.findByIdAndUserId(order.getId(), user.getId()))
                .thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(order.getId()))
                .thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock MoMo properties
        when(momoProperties.partnerCode()).thenReturn("MOMOBKUN20180529");
        when(momoProperties.accessKey()).thenReturn("klm05TvNBzhg7h7j");
        when(momoProperties.secretKey()).thenReturn("at67qH6mk8w5Y1nAyMoYKMWACiEi2bsa");
        when(momoProperties.apiUrl()).thenReturn("https://test-payment.momo.vn/v2/gateway/api/create");
        when(momoProperties.redirectUrl()).thenReturn("http://localhost:3000/payment-result");
        when(momoProperties.ipnUrl()).thenReturn("http://localhost:8080/api/payments/momo-ipn");

        // Mock RestClient response
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("resultCode", 0);
        mockResponse.put("message", "Success");
        mockResponse.put("payUrl", "https://momo.vn/pay-link");
        mockResponse.put("qrCodeUrl", "https://momo.vn/qr-link");
        mockResponse.put("deeplink", "momo://deeplink");

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(anyMap())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Map.class)).thenReturn(mockResponse);

        MomoInitiateResponse response = momoService.initiatePayment(request, user);

        assertThat(response.payUrl()).isEqualTo("https://momo.vn/pay-link");
        assertThat(response.qrCodeUrl()).isEqualTo("https://momo.vn/qr-link");
        assertThat(response.deeplink()).isEqualTo("momo://deeplink");
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void initiatePayment_throwsExceptionWhenNotPending() {
        User user = user();
        Order order = order(user, "COMPLETED");
        MomoPaymentInitiateRequest request = new MomoPaymentInitiateRequest(order.getId(), null);

        when(orderRepository.findByIdAndUserId(order.getId(), user.getId()))
                .thenReturn(Optional.of(order));

        assertThatThrownBy(() -> momoService.initiatePayment(request, user))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only PENDING orders can be paid");
    }

    @Test
    void processIpn_success() {
        User user = user();
        Order order = order(user, "PENDING");
        Payment payment = new Payment(order, order.getTotalAmount(), "MOMO", "PENDING");

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(order.getId())).thenReturn(Optional.of(payment));

        when(momoProperties.accessKey()).thenReturn("klm05TvNBzhg7h7j");
        when(momoProperties.secretKey()).thenReturn("at67qH6mk8w5Y1nAyMoYKMWACiEi2bsa");

        // Generate valid signature
        long amount = order.getTotalAmount().longValue();
        String rawSignature = "accessKey=klm05TvNBzhg7h7j" +
                "&amount=" + amount +
                "&extraData=" +
                "&message=Success" +
                "&orderId=" + order.getId().toString() +
                "&orderInfo=Thanh toan don hang" +
                "&orderType=momo_wallet" +
                "&partnerCode=MOMOBKUN20180529" +
                "&payType=qr" +
                "&requestId=req-123" +
                "&responseTime=1625477810" +
                "&resultCode=0" +
                "&transId=999888777";
        String validSignature = MomoCryptoUtil.signHmacSHA256(rawSignature, "at67qH6mk8w5Y1nAyMoYKMWACiEi2bsa");

        MomoIpnRequest ipnRequest = new MomoIpnRequest(
                "MOMOBKUN20180529",
                order.getId().toString(),
                "req-123",
                amount,
                "Thanh toan don hang",
                "momo_wallet",
                999888777L,
                0,
                "Success",
                "qr",
                1625477810L,
                "",
                validSignature
        );

        momoService.processIpn(ipnRequest);

        assertThat(order.getPaymentStatus()).isEqualTo("PAID");
        assertThat(payment.getStatus()).isEqualTo("PAID");
        assertThat(payment.getProviderTransactionId()).isEqualTo("999888777");
        assertThat(payment.getPaidAt()).isNotNull();

        verify(paymentRepository).save(payment);
        verify(orderRepository).save(order);
    }

    @Test
    void processIpn_throwsExceptionWhenSignatureInvalid() {
        MomoIpnRequest ipnRequest = new MomoIpnRequest(
                "MOMOBKUN20180529",
                UUID.randomUUID().toString(),
                "req-123",
                100000L,
                "Thanh toan don hang",
                "momo_wallet",
                999888777L,
                0,
                "Success",
                "qr",
                1625477810L,
                "",
                "invalid_signature"
        );

        when(momoProperties.accessKey()).thenReturn("klm05TvNBzhg7h7j");
        when(momoProperties.secretKey()).thenReturn("at67qH6mk8w5Y1nAyMoYKMWACiEi2bsa");

        assertThatThrownBy(() -> momoService.processIpn(ipnRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid signature");
    }

    private User user() {
        User user = new User(
                "Test User",
                "customer@example.com",
                "password-hash",
                Role.CUSTOMER
        );
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        return user;
    }

    private Order order(User user, String status) {
        Order order = new Order(
                user,
                new BigDecimal("100000.00"),
                status,
                "UNPAID",
                "123 Nguyen Hue, Ben Nghe, District 1, Ho Chi Minh",
                "Test Customer",
                "0900000000",
                BigDecimal.ZERO,
                "MOMO"
        );
        ReflectionTestUtils.setField(order, "id", UUID.randomUUID());
        return order;
    }
}
