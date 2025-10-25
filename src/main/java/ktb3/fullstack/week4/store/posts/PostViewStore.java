package ktb3.fullstack.week4.store.posts;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class PostViewStore implements PostSocialInfoStore {
    private final Map<Long, AtomicLong> postViewsByPostId = new ConcurrentHashMap<>();

    @Override
    public long increment(long postId) {
        return postViewsByPostId.computeIfAbsent(postId, k -> new AtomicLong(0)).incrementAndGet();
    }

    @Override
    public long getCount(long postId) {
        return postViewsByPostId.getOrDefault(postId, new AtomicLong(0)).get();
    }
}
