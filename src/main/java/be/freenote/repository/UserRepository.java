package be.freenote.repository;

import be.freenote.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailHash(String emailHash);
    Optional<User> findByUsername(String username);
    List<User> findAllByOrderByXpDesc(Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.profile p WHERE p.showInCarousel = true")
    List<User> findFeaturedProfiles();

    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%')) ORDER BY u.username")
    List<User> searchByUsername(String q, Pageable pageable);
}
