package ktb3.fullstack.week4.repository.auth;

import ktb3.fullstack.week4.domain.auth.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    RefreshToken save(RefreshToken refreshToken);

    RefreshToken findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
