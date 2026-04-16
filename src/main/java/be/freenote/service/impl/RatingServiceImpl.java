package be.freenote.service.impl;

import be.freenote.entity.Document;
import be.freenote.entity.Rating;
import be.freenote.entity.User;
import be.freenote.exception.ResourceNotFoundException;
import be.freenote.event.XpEvent;
import be.freenote.repository.DocumentRepository;
import be.freenote.repository.RatingRepository;
import be.freenote.repository.UserRepository;
import be.freenote.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void rate(Long userId, Long documentId, int score) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", documentId));

        Optional<Rating> existing = ratingRepository.findByDocumentIdAndUserId(documentId, userId);

        if (existing.isPresent()) {
            int oldScore = existing.get().getScore();
            existing.get().setScore(score);
            ratingRepository.save(existing.get());

            // Update denormalized average: recalculate with replaced score
            updateAverageOnChange(document, oldScore, score);
        } else {
            Rating rating = Rating.builder()
                    .document(document)
                    .user(user)
                    .score(score)
                    .build();
            ratingRepository.save(rating);

            // Update denormalized counters
            updateAverageOnNew(document, score);

            // Award XP to document author: proportional to rating score
            if (document.getUser() != null) {
                eventPublisher.publishEvent(new XpEvent.DocumentRated(document.getUser().getId(), documentId, score));
            }
        }
    }

    @Override
    public Double getAverageRating(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", documentId));
        return document.getAverageRating().doubleValue();
    }

    private void updateAverageOnNew(Document doc, int newScore) {
        int count = doc.getRatingCount();
        BigDecimal currentAvg = doc.getAverageRating();
        BigDecimal newAvg = currentAvg
                .multiply(BigDecimal.valueOf(count))
                .add(BigDecimal.valueOf(newScore))
                .divide(BigDecimal.valueOf(count + 1), 2, RoundingMode.HALF_UP);
        doc.setRatingCount(count + 1);
        doc.setAverageRating(newAvg);
        documentRepository.save(doc);
    }

    private void updateAverageOnChange(Document doc, int oldScore, int newScore) {
        int count = doc.getRatingCount();
        if (count == 0) return;
        BigDecimal currentAvg = doc.getAverageRating();
        BigDecimal newAvg = currentAvg
                .multiply(BigDecimal.valueOf(count))
                .subtract(BigDecimal.valueOf(oldScore))
                .add(BigDecimal.valueOf(newScore))
                .divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
        doc.setAverageRating(newAvg);
        documentRepository.save(doc);
    }
}
