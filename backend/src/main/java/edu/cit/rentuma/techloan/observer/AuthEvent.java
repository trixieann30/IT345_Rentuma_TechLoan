package edu.cit.rentuma.techloan.observer;

import edu.cit.rentuma.techloan.model.User;

import java.time.LocalDateTime;

/**
 * Refactoring 4 – Observer Pattern (Behavioral):
 *
 * Represents an authentication event that observers can react to.
 * Carrying the affected User and the type of event keeps the observer
 * decoupled from the service layer that produced the event.
 */
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