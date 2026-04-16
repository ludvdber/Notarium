package be.freenote.event;

import be.freenote.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Single source of truth for all XP reward rules.
 * Changing XP amounts or adding new rules only requires editing this class.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class XpEventListener {

    private static final int XP_DOCUMENT_VERIFIED = 10;
    private static final int XP_DOCUMENT_DOWNLOADED = 1;
    private static final int XP_PER_RATING_STAR = 2;

    private final UserService userService;

    @EventListener
    public void onDocumentVerified(XpEvent.DocumentVerified event) {
        userService.addXp(event.authorId(), XP_DOCUMENT_VERIFIED);
        log.debug("XP +{} to user {} (document {} verified)", XP_DOCUMENT_VERIFIED, event.authorId(), event.documentId());
    }

    @EventListener
    public void onDocumentDownloaded(XpEvent.DocumentDownloaded event) {
        userService.addXp(event.authorId(), XP_DOCUMENT_DOWNLOADED);
        log.debug("XP +{} to user {} (document {} downloaded)", XP_DOCUMENT_DOWNLOADED, event.authorId(), event.documentId());
    }

    @EventListener
    public void onDocumentRated(XpEvent.DocumentRated event) {
        int xp = XP_PER_RATING_STAR * event.score();
        userService.addXp(event.authorId(), xp);
        log.debug("XP +{} to user {} (document {} rated {}★)", xp, event.authorId(), event.documentId(), event.score());
    }
}
