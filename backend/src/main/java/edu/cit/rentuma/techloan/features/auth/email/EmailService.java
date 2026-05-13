package edu.cit.rentuma.techloan.features.auth.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class EmailService {

    private final String apiKey;
    private final String fromEmail;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    public EmailService(
            @Value("${app.sendgrid.api-key:}") String apiKey,
            @Value("${app.sendgrid.from-email:trixieann750@gmail.com}") String fromEmail,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.fromEmail = fromEmail;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    public void sendVerificationEmail(String to, String name, String token) {
        String subject = "TechLoan — Verify Your Email";
        String body = "Hi " + name + ",\n\n" +
                "Please verify your TechLoan account by clicking the link below:\n\n" +
                frontendUrl + "/verify-email?token=" + token + "\n\n" +
                "If you did not create this account, you can ignore this email.\n\n" +
                "TechLoan — CIT-U Lab Equipment System";
        trySend(to, subject, body);
    }

    public void sendWelcome(String to, String name) {
        String subject = "Welcome to TechLoan!";
        String body = "Hi " + name + ",\n\n" +
                "Your TechLoan account has been created successfully.\n" +
                "You can now browse and borrow lab equipment.\n\n" +
                "TechLoan — CIT-U Lab Equipment System";
        trySend(to, subject, body);
    }

    public void sendPasswordResetEmail(String to, String name, String token) {
        String subject = "TechLoan — Reset Your Password";
        String body = "Hi " + name + ",\n\n" +
                "You requested to reset your TechLoan password. Click the link below:\n\n" +
                frontendUrl + "/reset-password?token=" + token + "\n\n" +
                "This link expires in 1 hour. If you did not request this, you can safely ignore this email.\n\n" +
                "TechLoan — CIT-U Lab Equipment System";
        trySend(to, subject, body);
    }

    public void sendStatusUpdate(String to, String name, String status, String itemName) {
        String subject = "TechLoan — Reservation " + capitalize(status);
        String body = "Hi " + name + ",\n\n" +
                "Your reservation for \"" + itemName + "\" has been " + status.toLowerCase() + ".\n\n" +
                "Log in to TechLoan to view the details.\n\n" +
                "TechLoan — CIT-U Lab Equipment System";
        trySend(to, subject, body);
    }

    private void trySend(String to, String subject, String body) {
        CompletableFuture.runAsync(() -> {
            try {
                if (apiKey == null || apiKey.isBlank()) {
                    System.err.println("[EmailService] SENDGRID_API_KEY is not set. Skipping email to " + to);
                    return;
                }

                Map<String, Object> payload = Map.of(
                        "personalizations", List.of(Map.of("to", List.of(Map.of("email", to)))),
                        "from", Map.of("email", this.fromEmail, "name", "TechLoan System"),
                        "subject", subject,
                        "content", List.of(Map.of("type", "text/plain", "value", body))
                );

                String json = objectMapper.writeValueAsString(payload);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.sendgrid.com/v3/mail/send"))
                        .header("Authorization", "Bearer " + this.apiKey)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() >= 400) {
                    System.err.println("[EmailService] Failed to send email via SendGrid to " + to + ". Status: " + response.statusCode() + " Body: " + response.body());
                } else {
                    System.out.println("[EmailService] Successfully sent email to " + to);
                }
            } catch (Exception ex) {
                System.err.println("[EmailService] Exception when sending email to " + to + ": " + ex.getMessage());
            }
        });
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.charAt(0) + s.substring(1).toLowerCase();
    }
}
