package be.freenote.integration;

import be.freenote.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests cascading behavior when a user is deleted.
 * Verifies ON DELETE SET NULL for documents, courses, donations, delegate_history,
 * and CascadeType.ALL + orphanRemoval for UserProfile, Rating, Favorite, Badge.
 */
@Tag("integration")
class CascadeTest extends AbstractIntegrationTest {

    // Not used here, but required by Spring context (AuthServiceImpl depends on it)
    @MockitoBean private JavaMailSender mailSender;

    private User userA;
    private User userB;
    private Section section;
    private Course courseByA;
    private Document docByA;
    private Document docByB;

    @BeforeEach
    void setUp() {
        ratingRepository.deleteAll();
        favoriteRepository.deleteAll();
        badgeRepository.deleteAll();
        donationRepository.deleteAll();
        documentRepository.deleteAll();
        courseRepository.deleteAll();
        sectionRepository.deleteAll();
        userRepository.deleteAll();

        section = createSection("IT-cascade");

        userA = createVerifiedUser("cascade-userA");
        userB = createVerifiedUser("cascade-userB");

        courseByA = createCourse("Java OO", section, userA);

        docByA = createDocument("UserA Synthese", courseByA, userA);
        docByB = createDocument("UserB Notes", courseByA, userB);

        // UserB rates docByA with 5 stars
        Rating rating = Rating.builder()
                .document(docByA)
                .user(userB)
                .score(5)
                .build();
        ratingRepository.save(rating);
        docByA.setAverageRating(new BigDecimal("5.00"));
        docByA.setRatingCount(1);
        documentRepository.save(docByA);

        // Give userA a badge
        Badge badge = Badge.builder()
                .user(userA)
                .badgeType("FIRST_UPLOAD")
                .build();
        badgeRepository.save(badge);
    }

    @Test
    void shouldPreserveDataWhenUserADeleted() throws Exception {
        Long userAId = userA.getId();
        Long userBId = userB.getId();
        Long docByAId = docByA.getId();
        Long docByBId = docByB.getId();
        Long courseId = courseByA.getId();

        String jwtA = jwtFor(userA);

        // Delete userA via API
        mockMvc.perform(delete("/api/users/me")
                        .header("Authorization", "Bearer " + jwtA)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        // 1. Course exists, createdBy is null (ON DELETE SET NULL)
        Course refreshedCourse = courseRepository.findById(courseId).orElseThrow();
        assertThat(refreshedCourse.getCreatedBy()).isNull();
        assertThat(refreshedCourse.getName()).isEqualTo("Java OO");

        // 2. DocByA exists, user is null (anonymized)
        Document refreshedDocA = documentRepository.findById(docByAId).orElseThrow();
        assertThat(refreshedDocA.getUser()).isNull();
        assertThat(refreshedDocA.isAnonymous()).isTrue();
        assertThat(refreshedDocA.getTitle()).isEqualTo("UserA Synthese");

        // 3. DocByB is completely unaffected
        Document refreshedDocB = documentRepository.findById(docByBId).orElseThrow();
        assertThat(refreshedDocB.getUser()).isNotNull();
        assertThat(refreshedDocB.getUser().getId()).isEqualTo(userBId);

        // 4. UserB's rating on docByA still exists (rating belongs to userB, not deleted)
        var ratings = ratingRepository.findAll();
        assertThat(ratings).hasSize(1);
        assertThat(ratings.getFirst().getUser().getId()).isEqualTo(userBId);
        assertThat(ratings.getFirst().getDocument().getId()).isEqualTo(docByAId);

        // 5. UserA's badges are deleted (CascadeType.ALL + orphanRemoval)
        assertThat(badgeRepository.findAll().stream()
                .filter(b -> userAId.equals(b.getUser().getId()))
                .toList()).isEmpty();

        // 6. UserA's profile is deleted (CascadeType.ALL + orphanRemoval)
        assertThat(userRepository.findById(userAId)).isEmpty();
    }

    @Test
    void shouldCascadeDeleteRatingsWhenDocumentDeleted() {
        // UserB rates docByA — rating exists
        assertThat(ratingRepository.findAll()).hasSize(1);

        // Delete docByA directly
        documentRepository.deleteById(docByA.getId());
        documentRepository.flush();

        // Rating should be cascade-deleted with the document
        assertThat(ratingRepository.findAll()).isEmpty();
    }

    @Test
    void shouldNotDeleteDocumentsWhenDeletingUserWithFavorites() throws Exception {
        // UserA favorites docByB
        Favorite fav = Favorite.builder()
                .user(userA)
                .document(docByB)
                .build();
        favoriteRepository.save(fav);

        String jwtA = jwtFor(userA);
        Long docByBId = docByB.getId();

        // Delete userA
        mockMvc.perform(delete("/api/users/me")
                        .header("Authorization", "Bearer " + jwtA)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        // DocByB still exists (favorite was cascade-deleted, not the document)
        assertThat(documentRepository.findById(docByBId)).isPresent();
        assertThat(favoriteRepository.findAll()).isEmpty();
    }
}
