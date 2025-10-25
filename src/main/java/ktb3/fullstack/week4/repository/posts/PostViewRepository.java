package ktb3.fullstack.week4.repository.posts;

import ktb3.fullstack.week4.store.posts.PostViewStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostViewRepository implements PostSocialInfoRepository {
    private final PostViewStore postViewStore;

    @Override
    public long countByPostId(long postId) {
        return postViewStore.getCount(postId);
    }

    public long plusViewCount(long postId) {
        return postViewStore.increment(postId);
    }
}
