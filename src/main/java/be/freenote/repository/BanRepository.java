package be.freenote.repository;

import be.freenote.entity.Ban;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BanRepository extends JpaRepository<Ban, Long> {
    boolean existsByEmailHash(String emailHash);
    boolean existsByOauthProviderAndOauthId(String oauthProvider, String oauthId);
}
