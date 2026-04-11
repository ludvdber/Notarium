package be.notarium.repository;

import be.notarium.entity.DelegateHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DelegateHistoryRepository extends JpaRepository<DelegateHistory, Long> {
    List<DelegateHistory> findByEndDateIsNull();
    List<DelegateHistory> findByUserId(Long userId);
}
