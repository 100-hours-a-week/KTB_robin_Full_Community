package ktb3.fullstack.week4.store.posts;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class CommentStore implements PostSocialInfoStore {
    private final Map<Long, AtomicLong> commentsByPostId = new ConcurrentHashMap<>();

    @Override
    public long increment(long postId) {
        return commentsByPostId.computeIfAbsent(postId, k -> new AtomicLong(0)).incrementAndGet();
    }

    public long decrement(long postId) {
        return commentsByPostId.computeIfAbsent(postId, k -> new AtomicLong(0)).updateAndGet(v -> v > 0 ? v - 1 : 0);
    }

    @Override
    public long getCount(long postId) {
        return commentsByPostId.getOrDefault(postId, new AtomicLong(0)).get();
    }

}
