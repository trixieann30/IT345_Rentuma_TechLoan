package edu.cit.rentuma.techloan.features.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Integrates with the PayMongo Checkout Sessions API (sandbox).
 * Supports GCash and PayMaya as payment methods.
 * Docs: https://developers.paymongo.com/docs/checkout-integration
 */
@Service
public class PayMongoService {

    @Value("${paymongo.secret-key:}")
    private String secretKey;

    @Value("${paymongo.base-url:https://api.paymongo.com}")
    private String baseUrl;

    @Value("${paymongo.success-url:http://localhost:5173/payment/success}")
    private String successUrl;

    @Value("${paymongo.cancel-url:http://localhost:5173/penalties}")
    private String cancelUrl;

    private final RestClient restClient;

    public PayMongoService(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    public record CheckoutResult(String sessionId, String checkoutUrl) {}

    public CheckoutResult createCheckoutSession(int penaltyPoints, Long paymentId,
                                                 String userEmail, String userName) {
        String auth = "Basic " + Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes());

        int amountCentavos = penaltyPoints * 50 * 100; // 50 PHP per point, in centavos

        Map<String, Object> attributes = Map.of(
            "billing", Map.of("name", userName, "email", userEmail),
            "line_items", List.of(Map.of(
                "amount", amountCentavos,
                "currency", "PHP",
                "name", "TechLoan Penalty Fee",
                "description", penaltyPoints + " overdue point(s) x ₱50 each",
                "quantity", 1
            )),
            "payment_method_types", List.of("gcash", "paymaya"),
            "success_url", successUrl + "?id=" + paymentId,
            "cancel_url", cancelUrl,
            "statement_descriptor", "TechLoan"
        );

        Map<String, Object> body = Map.of("data", Map.of("attributes", attributes));

        Map<?, ?> response = restClient.post()
                .uri(baseUrl + "/v1/checkout_sessions")
                .header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);

        Map<?, ?> data = (Map<?, ?>) response.get("data");
        String sessionId = (String) data.get("id");
        Map<?, ?> attrs = (Map<?, ?>) data.get("attributes");
        String checkoutUrl = (String) attrs.get("checkout_url");

        return new CheckoutResult(sessionId, checkoutUrl);
    }

    public boolean verifyPayment(String sessionId) {
        String auth = "Basic " + Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes());

        Map<?, ?> response = restClient.get()
                .uri(baseUrl + "/v1/checkout_sessions/{id}", sessionId)
                .header("Authorization", auth)
                .retrieve()
                .body(Map.class);

        Map<?, ?> data = (Map<?, ?>) response.get("data");
        Map<?, ?> attrs = (Map<?, ?>) data.get("attributes");
        String paymentStatus = (String) attrs.get("payment_status");

        return "paid".equals(paymentStatus);
    }
}
