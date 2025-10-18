package ktb3.fullstack.week4.store.posts;

import ktb3.fullstack.week4.domain.posts.Comment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class CommentStore {
    private final Map<Long, List<Comment>> commentsByPostIdMap = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(0);

    public long nextId() {
        return seq.incrementAndGet();
    }

    public Comment add(long postId, long authorId, String content, LocalDateTime now) {
        Comment comment = new Comment(
                nextId(),
                postId,
                authorId,
                content,
                now,
                now
        );
        commentsByPostIdMap
                .computeIfAbsent(postId, k -> new ArrayList<>())
                .add(comment);
        return comment;
    }

    public boolean edit(long postId, long commentId, long authorId, String newContent, LocalDateTime now) {
        List<Comment> list = commentsByPostIdMap.getOrDefault(postId, null);
        if (list == null) return false;
        for (Comment c : list) {
            if (c.getId() == commentId) {
                if (c.getAuthorId() != authorId) return false;
                c.setContent(newContent);
                c.setModifiedAt(now);
                return true;
            }
        }
        return false;
    }

    public boolean remove(long postId, long commentId, long authorId) {
        List<Comment> list = commentsByPostIdMap.getOrDefault(postId, null);
        if (list == null) return false;
        boolean removed = list.removeIf(c -> c.getId() == commentId && c.getAuthorId() == authorId);
        if (list.isEmpty()) {
            commentsByPostIdMap.remove(postId);
        }
        return removed;
    }

    public List<Comment> findByPostId(long postId) {
        List<Comment> list = commentsByPostIdMap.getOrDefault(postId, null);
        return list == null ? List.of() : new ArrayList<>(list);
    }

    public long getCount(long postId) {
        List<Comment> list = commentsByPostIdMap.getOrDefault(postId, null);
        return list == null ? 0L : list.size();
    }
}
