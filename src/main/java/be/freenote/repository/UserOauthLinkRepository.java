package be.freenote.repository;

import be.freenote.entity.UserOauthLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserOauthLinkRepository extends JpaRepository<UserOauthLink, Long> {
    Optional<UserOauthLink> findByProviderAndOauthId(String provider, String oauthId);
    List<UserOauthLink> findByUserId(Long userId);
    Optional<UserOauthLink> findByUserIdAndProvider(Long userId, String provider);
    long countByUserId(Long userId);
}
