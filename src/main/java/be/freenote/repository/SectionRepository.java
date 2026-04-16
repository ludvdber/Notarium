package be.freenote.repository;

import be.freenote.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {

    @Query("""
        SELECT s.id AS id, s.name AS name, s.icon AS icon, COUNT(d.id) AS documentCount
        FROM Section s
        LEFT JOIN s.courses c
        LEFT JOIN c.documents d
        WHERE s.approved = true
        GROUP BY s.id, s.name, s.icon
        """)
    List<SectionWithDocCount> findAllApprovedWithDocCount();

    interface SectionWithDocCount {
        Long getId();
        String getName();
        String getIcon();
        Long getDocumentCount();
    }
}
