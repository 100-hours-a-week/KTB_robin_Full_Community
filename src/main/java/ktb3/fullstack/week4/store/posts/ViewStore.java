package ktb3.fullstack.week4.store.posts;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class ViewStore implements PostSocialInfoStore {
    private final Map<Long, AtomicLong> viewsByPostId = new ConcurrentHashMap<>();

    @Override
    public long increment(long postId) {
        return viewsByPostId.computeIfAbsent(postId, k -> new AtomicLong(0)).incrementAndGet();
    }

    @Override
    public long getCount(long postId) {
        return viewsByPostId.getOrDefault(postId, new AtomicLong(0)).get();
    }
}
