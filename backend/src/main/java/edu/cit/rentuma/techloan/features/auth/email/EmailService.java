package edu.cit.rentuma.techloan.features.auth.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * Sends email via Brevo (formerly Sendinblue) HTTP API.
 * Works on Railway — no SMTP ports needed.
 *
 * Setup (free, 5 minutes, no IT department):
 *  1. Sign up at https://www.brevo.com (free tier = 300 emails/day)
 *  2. Go to: top-right menu → SMTP & API → API Keys → Generate a new API key
 *  3. (Optional) Senders & Domains → Add a sender email you own → verify it
 *  4. Set these Railway environment variables:
 *       BREVO_API_KEY    = your API key (starts with "xkeysib-...")
 *       BREVO_SENDER_EMAIL = e.g. yourname@gmail.com or noreply@techloan.app
 *       BREVO_SENDER_NAME  = TechLoan CIT-U   (shows in inbox as the display name)
 */
@Service
public class EmailService {

    private static final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";

    @Value("${app.email.brevo.api-key:}")
    private String apiKey;

    @Value("${app.email.brevo.sender-email:noreply@techloan.app}")
    private String senderEmail;

    @Value("${app.email.brevo.sender-name:TechLoan CIT-U}")
    private String senderName;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    private final HttpClient httpClient;

    public EmailService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    // ── Public send methods ────────────────────────────────────────────────

    public void sendVerificationEmail(String to, String name, String token) {
        trySend(to, name,
                "TechLoan — Verify Your Email",
                "Hi " + name + ",\n\n" +
                "Please verify your TechLoan account by clicking the link below:\n\n" +
                frontendUrl + "/verify-email?token=" + token + "\n\n" +
                "If you did not create this account, you can ignore this email.\n\n" +
                "TechLoan — CIT-U Lab Equipment System");
    }

    public void sendWelcome(String to, String name) {
        trySend(to, name,
                "Welcome to TechLoan!",
                "Hi " + name + ",\n\n" +
                "Your TechLoan account has been created successfully.\n" +
                "You can now browse and borrow lab equipment.\n\n" +
                "TechLoan — CIT-U Lab Equipment System");
    }

    public void sendPasswordResetEmail(String to, String name, String token) {
        trySend(to, name,
                "TechLoan — Reset Your Password",
                "Hi " + name + ",\n\n" +
                "You requested to reset your TechLoan password. Click the link below:\n\n" +
                frontendUrl + "/reset-password?token=" + token + "\n\n" +
                "This link expires in 1 hour. If you did not request this, you can safely ignore this email.\n\n" +
                "TechLoan — CIT-U Lab Equipment System");
    }

    public void sendStatusUpdate(String to, String name, String status, String itemName) {
        trySend(to, name,
                "TechLoan — Reservation " + capitalize(status),
                "Hi " + name + ",\n\n" +
                "Your reservation for \"" + itemName + "\" has been " + status.toLowerCase() + ".\n\n" +
                "Log in to TechLoan to view the details.\n\n" +
                "TechLoan — CIT-U Lab Equipment System");
    }

    // ── Internal ───────────────────────────────────────────────────────────

    private void trySend(String to, String toName, String subject, String body) {
        if (apiKey.isBlank()) {
            System.err.println("[EmailService] BREVO_API_KEY not set. Skipping email to: " + to);
            return;
        }
        CompletableFuture.runAsync(() -> {
            try {
                String payload = "{"
                        + "\"sender\":{\"name\":" + q(senderName) + ",\"email\":" + q(senderEmail) + "},"
                        + "\"to\":[{\"email\":" + q(to) + ",\"name\":" + q(toName) + "}],"
                        + "\"subject\":" + q(subject) + ","
                        + "\"textContent\":" + q(body)
                        + "}";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BREVO_URL))
                        .header("api-key", apiKey)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 201) {
                    System.out.println("[EmailService] Sent email to " + to);
                } else {
                    System.err.println("[EmailService] Brevo returned " + response.statusCode() + ": " + response.body());
                }
            } catch (Exception e) {
                System.err.println("[EmailService] Failed to send email to " + to + ": " + e.getMessage());
            }
        });
    }

    /** JSON-escape and quote a string value. */
    private String q(String s) {
        if (s == null) return "\"\"";
        return "\"" + s.replace("\\", "\\\\")
                       .replace("\"", "\\\"")
                       .replace("\n", "\\n")
                       .replace("\r", "") + "\"";
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.charAt(0) + s.substring(1).toLowerCase();
    }
}
