package edu.cit.rentuma.techloan.features.auth.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.sender:noreply@techloan.app}")
    private String senderEmail;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

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
        CompletableFuture.runAsync(() -> {
            try {
                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setFrom(senderEmail);
                msg.setTo(to);
                msg.setSubject(subject);
                msg.setText(body);
                mailSender.send(msg);
                System.out.println("[EmailService] Sent email to " + to);
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
