package edu.cit.rentuma.techloan.features.auth.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class EmailService {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    // Hardcoded URL since this is specific to this workaround
    private final String scriptUrl = "https://script.google.com/macros/s/AKfycbxFHx4GopFpv4DllgsTivChXZy2A-6ZTE4HqCtGqE9OtSDmvjUhn-SlrWm8I0fBjLzAAQ/exec";

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    public EmailService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS) // Google Script requires following redirects!
                .build();
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
                Map<String, String> payload = Map.of(
                        "to", to,
                        "subject", subject,
                        "body", body
                );

                String json = objectMapper.writeValueAsString(payload);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(scriptUrl))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() >= 400) {
                    System.err.println("[EmailService] Failed to send email via Google Apps Script to " + to + ". Status: " + response.statusCode() + " Body: " + response.body());
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
