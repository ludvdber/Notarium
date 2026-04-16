package be.freenote.repository;

import be.freenote.entity.Document;
import be.freenote.enums.Category;
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
    List<Document> findTop10ByVerifiedTrueOrderByCreatedAtDesc();
    List<Document> findTop10ByVerifiedTrueOrderByDownloadCountDesc();
    Page<Document> findByVerifiedTrueAndCourseIdAndCategory(Long courseId, Category category, Pageable pageable);
    Page<Document> findByVerifiedTrueAndCourseId(Long courseId, Pageable pageable);
    Page<Document> findByVerifiedTrueAndCategory(Category category, Pageable pageable);
    Page<Document> findByVerifiedTrue(Pageable pageable);
    long countByCreatedAtAfter(LocalDateTime dateTime);
    Page<Document> findByUserId(Long userId, Pageable pageable);
    List<Document> findByVerifiedFalse();
    long countByUserId(Long userId);
    long countByCourseId(Long courseId);

    @Query("SELECT COUNT(d) FROM Document d WHERE d.course.section.id = :sectionId")
    long countBySectionId(@Param("sectionId") Long sectionId);

    /** Batch count: returns a map of userId → documentCount for all given user IDs in one query. */
    @Query("SELECT d.user.id, COUNT(d) FROM Document d WHERE d.user.id IN :userIds GROUP BY d.user.id")
    List<Object[]> countByUserIds(@Param("userIds") List<Long> userIds);

    Page<Document> findByUserIdAndVerifiedTrue(Long userId, Pageable pageable);

    @Modifying
    @Query("UPDATE Document d SET d.anonymous = true, d.user = null WHERE d.user.id = :userId")
    void anonymizeByUserId(@Param("userId") Long userId);

    List<Document> findTop10ByOrderByDownloadCountDesc();

    @Modifying
    @Query("UPDATE Document d SET d.downloadCount = d.downloadCount + :increment WHERE d.id = :docId")
    void incrementDownloadCount(@Param("docId") Long docId, @Param("increment") int increment);

    @Query("SELECT COALESCE(SUM(d.downloadCount), 0) FROM Document d")
    long sumDownloadCount();

    @Query("SELECT DISTINCT d FROM Document d " +
           "LEFT JOIN FETCH d.course c " +
           "LEFT JOIN FETCH c.section " +
           "LEFT JOIN FETCH d.professor " +
           "LEFT JOIN FETCH d.tags")
    List<Document> findAllWithAssociations();
}
