package ktb3.fullstack.week4.store.posts;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LikeStore {

    // key: postId
    // value: 좋아요 누른 userId 집합
    private final Map<Long, Set<Long>> likedUsersByPostId = new ConcurrentHashMap<>();


    // 특정 사용자 -> 특정 게시물에 좋아요
    public void markLiked(long postId, long userId) {
        Set<Long> users = likedUsersByPostId.computeIfAbsent(postId, k -> ConcurrentHashMap.newKeySet());
        users.add(userId);
    }

    // 특정 사용자 -> 특정 게시물에 좋아요 취소
    public void unmarkLiked(long postId, long userId) {
        Set<Long> users = likedUsersByPostId.getOrDefault(postId, null);
        if (users == null) return;
        users.remove(userId);
        if (users.isEmpty()) {
            likedUsersByPostId.remove(postId);
        }
    }

    // 특정 사용자가 특정 게시글에 좋아요를 눌렀는지 여부 확인
    public boolean isLiked(long postId, long userId) {
        Set<Long> users = likedUsersByPostId.getOrDefault(postId, null);
        return users != null && users.contains(userId);
    }

    // 게시글별 좋아요 개수 조회
    public long getCount(long postId) {
        Set<Long> users = likedUsersByPostId.getOrDefault(postId, null);
        return users == null ? 0L : users.size();
    }
}
