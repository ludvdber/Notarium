package be.notarium.repository;

import be.notarium.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByOauthProviderAndOauthId(String oauthProvider, String oauthId);
    Optional<User> findByEmailHash(String emailHash);
    Optional<User> findByUsername(String username);
    List<User> findTop10ByOrderByXpDesc();

    @Query("SELECT u FROM User u JOIN u.profile p WHERE p.showInCarousel = true")
    List<User> findFeaturedProfiles();
}
