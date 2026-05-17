package edu.cit.rentuma.techloan.features.auth.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class EmailService {

    @Value("${mailjet.api-key:}")
    private String apiKey;

    @Value("${mailjet.secret-key:}")
    private String secretKey;

    @Value("${app.email.sender:techloan.citu@gmail.com}")
    private String senderEmail;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public void sendVerificationEmail(String to, String name, String token) {
        trySend(to,
                "TechLoan — Verify Your Email",
                "Hi " + name + ",\n\n" +
                "Please verify your TechLoan account by clicking the link below:\n\n" +
                frontendUrl + "/verify-email?token=" + token + "\n\n" +
                "If you did not create this account, you can ignore this email.\n\n" +
                "TechLoan — CIT-U Lab Equipment System");
    }

    public void sendWelcome(String to, String name) {
        trySend(to,
                "Welcome to TechLoan!",
                "Hi " + name + ",\n\n" +
                "Your TechLoan account has been created successfully.\n" +
                "You can now browse and borrow lab equipment.\n\n" +
                "TechLoan — CIT-U Lab Equipment System");
    }

    public void sendPasswordResetEmail(String to, String name, String token) {
        trySend(to,
                "TechLoan — Reset Your Password",
                "Hi " + name + ",\n\n" +
                "You requested to reset your TechLoan password. Click the link below:\n\n" +
                frontendUrl + "/reset-password?token=" + token + "\n\n" +
                "This link expires in 1 hour. If you did not request this, you can safely ignore this email.\n\n" +
                "TechLoan — CIT-U Lab Equipment System");
    }

    public void sendStatusUpdate(String to, String name, String status, String itemName) {
        trySend(to,
                "TechLoan — Reservation " + capitalize(status),
                "Hi " + name + ",\n\n" +
                "Your reservation for \"" + itemName + "\" has been " + status.toLowerCase() + ".\n\n" +
                "Log in to TechLoan to view the details.\n\n" +
                "TechLoan — CIT-U Lab Equipment System");
    }

    private void trySend(String to, String subject, String body) {
        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("[EmailService] MAILJET_API_KEY not set — skipping email to " + to);
            return;
        }
        CompletableFuture.runAsync(() -> {
            try {
                Map<String, Object> payload = Map.of(
                        "Messages", List.of(Map.of(
                                "From", Map.of("Email", senderEmail, "Name", "TechLoan"),
                                "To", List.of(Map.of("Email", to)),
                                "Subject", subject,
                                "TextPart", body
                        ))
                );
                String json = objectMapper.writeValueAsString(payload);
                String credentials = Base64.getEncoder()
                        .encodeToString((apiKey + ":" + secretKey).getBytes());

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.mailjet.com/v3.1/send"))
                        .header("Authorization", "Basic " + credentials)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    System.out.println("[EmailService] Sent email to " + to);
                } else {
                    System.out.println("[EmailService] Failed to send email to " + to
                            + ": HTTP " + response.statusCode() + " — " + response.body());
                }
            } catch (Exception e) {
                System.out.println("[EmailService] Failed to send email to " + to + ": " + e.getMessage());
            }
        });
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.charAt(0) + s.substring(1).toLowerCase();
    }
}
