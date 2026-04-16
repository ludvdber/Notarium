package be.freenote.service;

import be.freenote.entity.Document;
import be.freenote.entity.Rating;
import be.freenote.entity.User;
import be.freenote.enums.Category;
import be.freenote.exception.ResourceNotFoundException;
import be.freenote.repository.DocumentRepository;
import be.freenote.repository.RatingRepository;
import be.freenote.repository.UserRepository;
import be.freenote.service.impl.RatingServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatingServiceImplTest {

    @Mock private RatingRepository ratingRepository;
    @Mock private DocumentRepository documentRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserService userService;

    @InjectMocks private RatingServiceImpl ratingService;

    private User testUser(Long id) {
        return User.builder().id(id).username("user" + id).build();
    }

    private Document testDocument(User author) {
        return Document.builder()
                .id(100L).title("Doc").category(Category.SYNTHESE).fileKey("k").fileSize(100L)
                .user(author).averageRating(BigDecimal.ZERO).ratingCount(0)
                .build();
    }

    // ---- New rating ----

    @Test
    void shouldCreateRatingAndUpdateDenormalizedCounters() {
        User voter = testUser(2L);
        User author = testUser(1L);
        Document doc = testDocument(author);

        when(userRepository.findById(2L)).thenReturn(Optional.of(voter));
        when(documentRepository.findById(100L)).thenReturn(Optional.of(doc));
        when(ratingRepository.findByDocumentIdAndUserId(100L, 2L)).thenReturn(Optional.empty());

        ratingService.rate(2L, 100L, 4);

        verify(ratingRepository).save(any(Rating.class));
        // Average: (0 * 0 + 4) / 1 = 4.00
        assertThat(doc.getAverageRating()).isEqualByComparingTo(new BigDecimal("4.00"));
        assertThat(doc.getRatingCount()).isEqualTo(1);
        verify(documentRepository).save(doc);
    }

    @Test
    void shouldAddXpToDocumentAuthorOnNewRating() {
        User voter = testUser(2L);
        User author = testUser(1L);
        Document doc = testDocument(author);

        when(userRepository.findById(2L)).thenReturn(Optional.of(voter));
        when(documentRepository.findById(100L)).thenReturn(Optional.of(doc));
        when(ratingRepository.findByDocumentIdAndUserId(100L, 2L)).thenReturn(Optional.empty());

        ratingService.rate(2L, 100L, 4);

        // XP = 2 * score = 8
        verify(userService).addXp(1L, 8);
    }

    @Test
    void shouldNotAddXpWhenDocumentHasNoAuthor() {
        User voter = testUser(2L);
        Document doc = testDocument(null);

        when(userRepository.findById(2L)).thenReturn(Optional.of(voter));
        when(documentRepository.findById(100L)).thenReturn(Optional.of(doc));
        when(ratingRepository.findByDocumentIdAndUserId(100L, 2L)).thenReturn(Optional.empty());

        ratingService.rate(2L, 100L, 3);

        verify(userService, never()).addXp(anyLong(), anyInt());
    }

    // ---- Update existing rating ----

    @Test
    void shouldUpdateExistingRatingAndRecalculate() {
        User voter = testUser(2L);
        Document doc = testDocument(testUser(1L));
        doc.setAverageRating(new BigDecimal("3.00"));
        doc.setRatingCount(2);

        Rating existing = Rating.builder().id(10L).document(doc).user(voter).score(2).build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(voter));
        when(documentRepository.findById(100L)).thenReturn(Optional.of(doc));
        when(ratingRepository.findByDocumentIdAndUserId(100L, 2L)).thenReturn(Optional.of(existing));

        ratingService.rate(2L, 100L, 5);

        assertThat(existing.getScore()).isEqualTo(5);
        verify(ratingRepository).save(existing);
        // newAvg = (3.00 * 2 - 2 + 5) / 2 = (6 - 2 + 5) / 2 = 4.50
        assertThat(doc.getAverageRating()).isEqualByComparingTo(new BigDecimal("4.50"));
    }

    @Test
    void shouldNotAddXpOnUpdatedRating() {
        User voter = testUser(2L);
        Document doc = testDocument(testUser(1L));
        doc.setAverageRating(new BigDecimal("3.00"));
        doc.setRatingCount(1);

        Rating existing = Rating.builder().id(10L).document(doc).user(voter).score(3).build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(voter));
        when(documentRepository.findById(100L)).thenReturn(Optional.of(doc));
        when(ratingRepository.findByDocumentIdAndUserId(100L, 2L)).thenReturn(Optional.of(existing));

        ratingService.rate(2L, 100L, 5);

        verify(userService, never()).addXp(anyLong(), anyInt());
    }

    // ---- Average calculation precision ----

    @Test
    void shouldCalculateAverageWithBigDecimalPrecision() {
        User voter = testUser(2L);
        Document doc = testDocument(testUser(1L));
        doc.setAverageRating(new BigDecimal("4.00"));
        doc.setRatingCount(2);

        when(userRepository.findById(2L)).thenReturn(Optional.of(voter));
        when(documentRepository.findById(100L)).thenReturn(Optional.of(doc));
        when(ratingRepository.findByDocumentIdAndUserId(100L, 2L)).thenReturn(Optional.empty());

        ratingService.rate(2L, 100L, 1);

        // newAvg = (4.00 * 2 + 1) / 3 = 9 / 3 = 3.00
        assertThat(doc.getAverageRating()).isEqualByComparingTo(new BigDecimal("3.00"));
        assertThat(doc.getRatingCount()).isEqualTo(3);
    }

    // ---- getAverageRating ----

    @Test
    void shouldReturnDenormalizedAverageRating() {
        Document doc = testDocument(null);
        doc.setAverageRating(new BigDecimal("4.25"));

        when(documentRepository.findById(100L)).thenReturn(Optional.of(doc));

        Double avg = ratingService.getAverageRating(100L);

        assertThat(avg).isEqualTo(4.25);
    }

    @Test
    void shouldThrowNotFoundWhenGettingRatingForMissingDoc() {
        when(documentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ratingService.getAverageRating(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
