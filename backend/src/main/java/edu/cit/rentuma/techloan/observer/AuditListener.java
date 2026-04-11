package edu.cit.rentuma.techloan.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Refactoring 4 – Observer Pattern (Behavioral):
 *
 * Listens for {@link BorrowStatusChangedEvent} and writes an audit log
 * entry for every status transition. This covers the full lifecycle:
 * PENDING → APPROVED → RETURNED / OVERDUE.
 *
 * Keeping audit logic here (instead of inside LoanService) means the
 * loan service stays focused on its core responsibility, and the audit
 * strategy can be changed or extended without touching LoanService.
 */
@Component
public class AuditListener {

    private static final Logger log = LoggerFactory.getLogger(AuditListener.class);

    /**
     * Logs every borrow-status change to the application audit trail.
     * Replace the log call with a database write (AuditLogRepository)
     * when the audit_log table is available.
     *
     * @param event the event carrying the request ID, new status, and user email
     */
    @EventListener
    public void onStatusChange(BorrowStatusChangedEvent event) {
        // TODO: persist to audit_log table via AuditLogRepository
        log.info("[AUDIT] {} | BorrowRequest#{} transitioned to {} | user={}",
                LocalDateTime.now(),
                event.getBorrowRequestId(),
                event.getNewStatus(),
                event.getUserEmail());
    }
}