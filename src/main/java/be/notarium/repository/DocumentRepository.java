package be.notarium.repository;

import be.notarium.entity.Document;
import be.notarium.enums.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    Page<Document> findByCourseIdAndCategory(Long courseId, Category category, Pageable pageable);
    List<Document> findTop10ByOrderByCreatedAtDesc();
    long countByCreatedAtAfter(LocalDateTime dateTime);
    Page<Document> findByUserId(Long userId, Pageable pageable);
    List<Document> findByVerifiedFalse();
    long countByUserId(Long userId);
    long countByCourseId(Long courseId);

    @Modifying
    @Query("UPDATE Document d SET d.anonymous = true, d.user = null WHERE d.user.id = :userId")
    void anonymizeByUserId(@Param("userId") Long userId);

    List<Document> findTop10ByOrderByDownloadCountDesc();

    @Modifying
    @Query("UPDATE Document d SET d.downloadCount = d.downloadCount + :increment WHERE d.id = :docId")
    void incrementDownloadCount(@Param("docId") Long docId, @Param("increment") int increment);

    @Query("SELECT COALESCE(SUM(d.downloadCount), 0) FROM Document d")
    long sumDownloadCount();
}
