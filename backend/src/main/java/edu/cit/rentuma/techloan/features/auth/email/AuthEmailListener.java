package edu.cit.rentuma.techloan.features.auth.email;

import edu.cit.rentuma.techloan.features.auth.model.User;
import edu.cit.rentuma.techloan.features.auth.observer.AuthEvent;
import edu.cit.rentuma.techloan.features.auth.observer.AuthEventListener;
import edu.cit.rentuma.techloan.features.auth.observer.AuthEventPublisher;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class AuthEmailListener implements AuthEventListener {

    private final EmailService emailService;
    private final AuthEventPublisher publisher;

    public AuthEmailListener(EmailService emailService, AuthEventPublisher publisher) {
        this.emailService = emailService;
        this.publisher    = publisher;
    }

    @PostConstruct
    public void register() {
        publisher.subscribe(this);
    }

    @Override
    public void onAuthEvent(AuthEvent event) {
        if (event.getType() == AuthEvent.Type.REGISTER_SUCCESS
                || event.getType() == AuthEvent.Type.GOOGLE_AUTH_SUCCESS) {
            User user = event.getUser();
            if (user.getRole() == User.Role.CUSTODIAN) {
                emailService.sendWelcome(user.getEmail(), user.getFullName());
            } else if (user.getVerificationToken() != null) {
                String targetEmail = user.getInstitutionalEmail() != null
                        ? user.getInstitutionalEmail()
                        : user.getPersonalEmail() != null
                            ? user.getPersonalEmail()
                            : user.getEmail();
                emailService.sendVerificationEmail(
                    targetEmail, user.getFullName(), user.getVerificationToken());
            }
        }
    }
}
