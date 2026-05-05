package edu.cit.rentuma.techloan.features.auth.email;

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
        if (event.getType() == AuthEvent.Type.REGISTER_SUCCESS) {
            emailService.sendWelcome(
                event.getUser().getEmail(),
                event.getUser().getFullName()
            );
        }
    }
}
