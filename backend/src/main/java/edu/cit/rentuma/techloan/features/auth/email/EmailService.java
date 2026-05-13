package edu.cit.rentuma.techloan.features.auth.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String fromEmail;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String fromEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
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
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(this.fromEmail);
                message.setTo(to);
                message.setSubject(subject);
                message.setText(body);

                mailSender.send(message);
                System.out.println("[EmailService] Successfully sent email to " + to);
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
