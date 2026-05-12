package edu.cit.rentuma.techloan.features.auth.email;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    private final SendGrid sendGrid;
    private final String fromEmail;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    public EmailService(
            @Value("${app.sendgrid.api-key}") String apiKey,
            @Value("${app.sendgrid.from-email}") String fromEmail) {
        this.sendGrid = new SendGrid(apiKey);
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
        Email from = new Email(this.fromEmail, "TechLoan System");
        Email toEmail = new Email(to);
        Content content = new Content("text/plain", body);
        Mail mail = new Mail(from, subject, toEmail, content);

        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sendGrid.api(request);
            if (response.getStatusCode() >= 400) {
                System.err.println("[EmailService] Failed to send email via SendGrid to " + to + ". Status: " + response.getStatusCode() + " Body: " + response.getBody());
            } else {
                System.out.println("[EmailService] Successfully sent email to " + to);
            }
        } catch (IOException ex) {
            System.err.println("[EmailService] IO Exception when sending email to " + to + ": " + ex.getMessage());
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.charAt(0) + s.substring(1).toLowerCase();
    }
}
