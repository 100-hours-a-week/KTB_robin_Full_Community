package ktb3.fullstack.week4.repository.posts;

import ktb3.fullstack.week4.store.posts.LikeStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LikeRepository implements PostSocialInfoRepository {
    private final LikeStore likeStore;

    @Override
    public long plusCount(long postId) {
        return likeStore.increment(postId);
    }

    @Override
    public long countByPostId(long postId) {
        return likeStore.getCount(postId);
    }

    public long minusCount(long postId) {
        return likeStore.decrement(postId);
    }

}
