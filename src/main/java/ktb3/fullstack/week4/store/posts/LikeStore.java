package ktb3.fullstack.week4.store.posts;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class LikeStore {

    // key: postId
    // value: 좋아요 누른 userId 집합
    private final Map<Long, Set<Long>> likedUsersByPostId = new ConcurrentHashMap<>();

    @Override
    public long increment(long postId) {
        return likesByPostId.computeIfAbsent(postId, k -> new AtomicLong(0)).incrementAndGet();
    }

    public long decrement(long postId) {
        return likesByPostId.computeIfAbsent(postId, k -> new AtomicLong(0)).updateAndGet(v -> v > 0 ? v - 1 : 0);
    }

    @Override
    public long getCount(long postId) {
        return likesByPostId.getOrDefault(postId, new AtomicLong(0)).get();
    }

}
