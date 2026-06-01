package be.freenote.repository;

import be.freenote.entity.Donation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {
    // Sorted by id desc — donations are append-only and id is monotonic, so newest first.
    Page<Donation> findAllByOrderByIdDesc(Pageable pageable);

    /** Idempotency guard: a Ko-fi (or manual) transaction id is processed at most once. */
    boolean existsByKofiTransactionId(String kofiTransactionId);
}
