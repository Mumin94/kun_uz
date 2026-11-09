package dasturlash.uz.event;

import dasturlash.uz.service.EmailHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProfileActivatedListener {
    private final EmailHistoryService emailHistoryService;

    // Send after commit without joining the completed activation transaction.
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void onProfileActivated(ProfileActivatedEvent event) {
        try {
            emailHistoryService.sendWelcomeEmail(event.email(), event.name());
        } catch (Exception e) {
            log.error("Could not send welcome email.", e);
        }

    }

}
