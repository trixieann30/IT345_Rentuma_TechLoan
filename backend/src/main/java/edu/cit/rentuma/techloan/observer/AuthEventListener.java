package edu.cit.rentuma.techloan.observer;

/**
 * Refactoring 4 – Observer Pattern (Behavioral):
 *
 * Observer interface. Any component interested in authentication events
 * implements this interface and registers itself with AuthEventPublisher.
 *
 * This keeps authentication services fully decoupled from cross-cutting
 * concerns such as audit logging, welcome emails, or analytics.
 */
public interface AuthEventListener {

    /**
     * Called when an authentication event occurs.
     *
     * @param event the event containing the type and affected user
     */
    void onAuthEvent(AuthEvent event);
}