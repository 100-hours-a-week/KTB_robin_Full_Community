package ktb3.fullstack.week4.repository.likes;

import ktb3.fullstack.week4.domain.likes.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    Like save(Like like);

    Optional<Like> findById(Long id);

    // 해당 게시글의 사용자 좋아요 여부 확인
    boolean existsByPostIdAndUserIdAndIsLikedTrue(Long postId, Long userId);

    long countByPostId(Long postId);
}
