package ktb3.fullstack.week4.store.posts;

import ktb3.fullstack.week4.domain.posts.Post;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class PostStore {
    private final Map<Long, Post> postMap = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(0);

    // 아이디 증가 후 반환
    public long nextId() {
        return seq.incrementAndGet();
    }
    // 게시물 반환
    public Post get(Long id) {
        return postMap.get(id);
    }
    // 게시물 추가
    public void put(Post entity) {
        postMap.put(entity.getId(), entity);
    }
    // 게시물 삭제
    public Post remove(Long id) {
        return postMap.remove(id);
    }
    // 전체 게시물 리스트 반환
    public List<Post> values() {
        return new ArrayList<>(postMap.values());
    }
}
