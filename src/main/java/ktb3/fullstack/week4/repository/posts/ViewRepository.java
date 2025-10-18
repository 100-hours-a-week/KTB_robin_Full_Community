package ktb3.fullstack.week4.repository.posts;

import ktb3.fullstack.week4.store.posts.ViewStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ViewRepository implements PostSocialInfoRepository {
    private final ViewStore viewStore;

    @Override
    public long countByPostId(long postId) {
        return viewStore.getCount(postId);
    }

    public long plusViewCount(long postId) {
        return viewStore.increment(postId);
    }
}
