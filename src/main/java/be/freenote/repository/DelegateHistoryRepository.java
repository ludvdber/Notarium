package be.freenote.repository;

import be.freenote.entity.DelegateHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DelegateHistoryRepository extends JpaRepository<DelegateHistory, Long> {
    List<DelegateHistory> findByEndDateIsNull();
    List<DelegateHistory> findByUserId(Long userId);
    Optional<DelegateHistory> findByUserIdAndEndDateIsNull(Long userId);
}
