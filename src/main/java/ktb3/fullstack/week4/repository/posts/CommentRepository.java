package ktb3.fullstack.week4.repository.posts;

import ktb3.fullstack.week4.store.posts.CommentStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentRepository implements PostSocialInfoRepository {
    private final CommentStore commentStore;

    @Override
    public long plusCount(long postId) {
        return commentStore.increment(postId);
    }

    @Override
    public long countByPostId(long postId) {
        return commentStore.getCount(postId);
    }

    public long minusComment(long postId) {
        return commentStore.decrement(postId);
    }

}
