package edu.cit.rentuma.techloan.observer;

import edu.cit.rentuma.techloan.model.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Refactoring 4 – Observer Pattern (Behavioral):
 *
 * The Subject/Publisher that manages a list of AuthEventListeners and
 * notifies them when authentication events occur.
 *
 * Why this matters for TechLoan:
 *   - Logging who logged in via Google without modifying GoogleAuthService
 *   - Sending welcome emails on first registration
 *   - Tracking user activity for penalty-point auditing
 *
 * Spring's own ApplicationEventPublisher could be used instead; this
 * hand-rolled version is included to make the Observer pattern explicit
 * and visible for the assignment.
 */
@Component
public class AuthEventPublisher {

    private final List<AuthEventListener> listeners = new ArrayList<>();

    /** Register a listener (called by Spring when beans are initialised). */
    public void subscribe(AuthEventListener listener) {
        listeners.add(listener);
    }

    /** Remove a listener. */
    public void unsubscribe(AuthEventListener listener) {
        listeners.remove(listener);
    }

    /** Publish a Google-auth-success event to all registered listeners. */
    public void publishGoogleAuthSuccess(User user) {
        notify(new AuthEvent(AuthEvent.Type.GOOGLE_AUTH_SUCCESS, user));
    }

    /** Publish a login-success event. */
    public void publishLoginSuccess(User user) {
        notify(new AuthEvent(AuthEvent.Type.LOGIN_SUCCESS, user));
    }

    /** Publish a register-success event. */
    public void publishRegisterSuccess(User user) {
        notify(new AuthEvent(AuthEvent.Type.REGISTER_SUCCESS, user));
    }

    // ── Internal broadcast ───────────────────────────────────────────

    private void notify(AuthEvent event) {
        for (AuthEventListener listener : listeners) {
            listener.onAuthEvent(event);
        }
    }
}