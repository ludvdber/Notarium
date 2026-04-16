package be.freenote.repository;

import be.freenote.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByApprovedFalse();

    @Query("""
        SELECT c, COUNT(d.id)
        FROM Course c
        LEFT JOIN c.documents d
        WHERE c.section.id = :sectionId AND c.approved = true
        GROUP BY c
        """)
    List<Object[]> findApprovedBySectionIdWithDocCount(@Param("sectionId") Long sectionId);
}
