package edu.cit.rentuma.techloan.features.auth.email;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
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
            System.err.println("[EmailService] Failed to send email to " + message.getTo() + ": " + e.getMessage());
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.charAt(0) + s.substring(1).toLowerCase();
    }
}
