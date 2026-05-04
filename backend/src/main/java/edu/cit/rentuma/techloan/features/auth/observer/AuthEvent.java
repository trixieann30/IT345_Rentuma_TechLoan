package edu.cit.rentuma.techloan.features.auth.observer;

import edu.cit.rentuma.techloan.features.auth.model.User;

import java.time.LocalDateTime;

public class AuthEvent {

    public enum Type {
        GOOGLE_AUTH_SUCCESS,
        LOGIN_SUCCESS,
        REGISTER_SUCCESS
    }

    private final Type type;
    private final User user;
    private final LocalDateTime occurredAt;

    public AuthEvent(Type type, User user) {
        this.type       = type;
        this.user       = user;
        this.occurredAt = LocalDateTime.now();
    }

    public Type getType()                   { return type; }
    public User getUser()                   { return user; }
    public LocalDateTime getOccurredAt()    { return occurredAt; }
}
