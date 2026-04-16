package be.freenote.repository;

import be.freenote.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    @Query("SELECT DISTINCT t.label FROM Tag t ORDER BY t.label")
    List<String> findDistinctLabels();
}
