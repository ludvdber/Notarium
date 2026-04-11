package be.notarium.service;

public interface RatingService {
    void rate(Long userId, Long documentId, int score);
    Double getAverageRating(Long documentId);
}
