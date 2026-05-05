package edu.cit.rentuma.techloan.features.auth.observer;

import edu.cit.rentuma.techloan.features.auth.model.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AuthEventPublisher {

    private final List<AuthEventListener> listeners = new ArrayList<>();

    public void subscribe(AuthEventListener listener)   { listeners.add(listener); }
    public void unsubscribe(AuthEventListener listener) { listeners.remove(listener); }

    public void publishGoogleAuthSuccess(User user) {
        notify(new AuthEvent(AuthEvent.Type.GOOGLE_AUTH_SUCCESS, user));
    }

    public void publishLoginSuccess(User user) {
        notify(new AuthEvent(AuthEvent.Type.LOGIN_SUCCESS, user));
    }

    public void publishRegisterSuccess(User user) {
        notify(new AuthEvent(AuthEvent.Type.REGISTER_SUCCESS, user));
    }

    private void notify(AuthEvent event) {
        for (AuthEventListener listener : listeners) {
            listener.onAuthEvent(event);
        }
    }
}
