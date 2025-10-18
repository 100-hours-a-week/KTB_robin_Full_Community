package ktb3.fullstack.week4.repository.posts;

import ktb3.fullstack.week4.domain.posts.Comment;
import ktb3.fullstack.week4.store.posts.CommentStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CommentRepository implements PostSocialInfoRepository {
    private final CommentStore commentStore;

    @Override
    public long countByPostId(long postId) {
        return commentStore.getCount(postId);
    }

    public Comment addComment(long postId, long authorId, String content) {
        return commentStore.add(postId, authorId, content, LocalDateTime.now());
    }

    public boolean editComment(long postId, long commentId, long authorId, String newContent) {
        return commentStore.edit(postId, commentId, authorId, newContent, LocalDateTime.now());
    }

    public boolean deleteComment(long postId, long commentId, long authorId) {
        return commentStore.remove(postId, commentId, authorId);
    }

    public List<Comment> findByPostId(long postId) {
        return commentStore.findByPostId(postId);
    }
}
