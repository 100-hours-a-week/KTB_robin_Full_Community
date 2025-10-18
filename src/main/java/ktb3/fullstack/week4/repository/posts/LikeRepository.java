package ktb3.fullstack.week4.repository.posts;

import ktb3.fullstack.week4.store.posts.LikeStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LikeRepository implements PostSocialInfoRepository {
    private final LikeStore likeStore;

    @Override
    public long countByPostId(long postId) {
        return likeStore.getCount(postId);
    }

    // 사용자별 좋아요 여부 확인
    public boolean isLiked(long postId, long userId) {
        return likeStore.isLiked(postId, userId);
    }

    // 선택: 실제 좋아요/취소 처리 (컨트롤러/서비스에서 호출)
    public void like(long postId, long userId) {
        likeStore.markLiked(postId, userId);
    }

    public void unlike(long postId, long userId) {
        likeStore.unmarkLiked(postId, userId);
    }
}
