package be.freenote.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "ratings", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"document_id", "user_id"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private int score;

    /**
     * Keeps Document.averageRating and ratingCount consistent when a rating is
     * deleted by cascade (e.g. document deletion or orphan removal).
     */
    @PreRemove
    void adjustDocumentAverageOnRemove() {
        if (document == null) return;
        int count = document.getRatingCount();
        if (count <= 1) {
            document.setRatingCount(0);
            document.setAverageRating(BigDecimal.ZERO);
        } else {
            BigDecimal newAvg = document.getAverageRating()
                    .multiply(BigDecimal.valueOf(count))
                    .subtract(BigDecimal.valueOf(score))
                    .divide(BigDecimal.valueOf(count - 1), 2, RoundingMode.HALF_UP);
            document.setRatingCount(count - 1);
            document.setAverageRating(newAvg);
        }
    }
}
