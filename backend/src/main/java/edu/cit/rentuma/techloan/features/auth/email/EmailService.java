package edu.cit.rentuma.techloan.features.auth.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String to, String name, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("TechLoan — Verify Your Email");
        message.setText(
            "Hi " + name + ",\n\n" +
            "Please verify your TechLoan account by clicking the link below:\n\n" +
            frontendUrl + "/verify-email?token=" + token + "\n\n" +
            "If you did not create this account, you can ignore this email.\n\n" +
            "TechLoan — CIT-U Lab Equipment System"
        );
        trySend(message);
    }

    public void sendWelcome(String to, String name) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Welcome to TechLoan!");
        message.setText(
            "Hi " + name + ",\n\n" +
            "Your TechLoan account has been created successfully.\n" +
            "You can now browse and borrow lab equipment.\n\n" +
            "TechLoan — CIT-U Lab Equipment System"
        );
        trySend(message);
    }

    public void sendPasswordResetEmail(String to, String name, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("TechLoan — Reset Your Password");
        message.setText(
            "Hi " + name + ",\n\n" +
            "You requested to reset your TechLoan password. Click the link below:\n\n" +
            frontendUrl + "/reset-password?token=" + token + "\n\n" +
            "This link expires in 1 hour. If you did not request this, you can safely ignore this email.\n\n" +
            "TechLoan — CIT-U Lab Equipment System"
        );
        trySend(message);
    }

    public void sendStatusUpdate(String to, String name, String status, String itemName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("TechLoan — Reservation " + capitalize(status));
        message.setText(
            "Hi " + name + ",\n\n" +
            "Your reservation for \"" + itemName + "\" has been " + status.toLowerCase() + ".\n\n" +
            "Log in to TechLoan to view the details.\n\n" +
            "TechLoan — CIT-U Lab Equipment System"
        );
        trySend(message);
    }

    private void trySend(SimpleMailMessage message) {
        try {
            mailSender.send(message);
        } catch (Exception e) {
            String recipient = message.getTo() != null && message.getTo().length > 0 
                ? message.getTo()[0] 
                : "unknown";
            System.err.println("[EmailService] Failed to send email to " + recipient + ": " + e.getMessage());
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.charAt(0) + s.substring(1).toLowerCase();
    }
}
